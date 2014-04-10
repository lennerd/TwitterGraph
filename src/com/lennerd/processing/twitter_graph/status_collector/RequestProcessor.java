package com.lennerd.processing.twitter_graph.status_collector;

import com.lennerd.processing.twitter_graph.status_collector.request.Request;
import twitter4j.*;

import java.util.*;

/**
 * Request processor class.
 *
 * It makes requests to Twitter and considers the give rate limits for the current request.
 * So it calls Twitter only when enough allowed requests remains. This results in a regular call of requests to the API.
 *
 * @see //dev.twitter.com/docs/rate-limiting/1.1
 */
public class RequestProcessor extends TimerTask implements RateLimitStatusListener {

    private Twitter twitter;
    private Queue<Request> requestQueue;
    private HashMap<String, RequestFunnel> requestFunnelStorage;
    private RequestFunnel recentRequestFunnel;
    private Timer timer;

    /**
     * Constructor.
     *
     * @param twitter The Twitter access API instance.
     * @param requestQueue The request queue.
     */
    public RequestProcessor(Twitter twitter, Queue<Request> requestQueue) {
        this.twitter = twitter;
        this.requestQueue = requestQueue;

        this.timer = new Timer("RequestProcessor");
        this.requestFunnelStorage = new HashMap<String, RequestFunnel>();

        this.twitter.addRateLimitStatusListener(this);
    }

    /**
     * Starts the request processor and underlying request call timer with the given period.
     *
     * @param period The period for trying to process requests.
     */
    public void start(long period) {
        this.timer.schedule(this, 0, period);
    }

    /**
     * Tries to work through the request queue.
     *
     * @throws twitter4j.TwitterException
     */
    @Override
    public void run() {
        Request request;
        RequestFunnel funnel;
        int maxSkips = this.requestQueue.size();

        if (maxSkips == 0) {
            return;
        }

        // Run through the queue of requests
        for (int i = 1; i <= maxSkips; i++) {
            request = this.requestQueue.remove();

            String requestName = request.getName();
            funnel = this.requestFunnelStorage.get(requestName);

            if (funnel == null) {
                // No funnel for the given request type exists.
                funnel = new RequestFunnel();
                this.requestFunnelStorage.put(requestName, funnel);
            }

            if (!funnel.isAllowed()) {
                // Current request type has no remaining API calls, so add it to the queue again.
                // By removing the request at the first place, we now add it at the end, so it won't be called twice.
                this.requestQueue.add(request);

                continue;
            }

            // Request can be done without risking request limit.
            // Save current funnel so that we can calculate the next time the given request type is allowed to be called
            // again.
            this.recentRequestFunnel = funnel;

            try {
                request.run(this.twitter);
            } catch (TwitterException exception) {
                if (exception.getStatusCode() == 429) {
                    // Update request limit status if something went wrong
                    this.updateRateLimitStatus(exception.getRateLimitStatus());
                }
            }
        }
    }

    public Queue<Request> getRequestQueue() {
        return requestQueue;
    }

    @Override
    public void onRateLimitReached(RateLimitStatusEvent event) {
        this.updateRateLimitStatus(event.getRateLimitStatus());
    }

    @Override
    public void onRateLimitStatus(RateLimitStatusEvent event) {
        this.updateRateLimitStatus(event.getRateLimitStatus());
    }

    private void updateRateLimitStatus(RateLimitStatus status) {
        if (this.recentRequestFunnel != null) {
            this.recentRequestFunnel.update(status);
        }
    }

    /**
     * Request funnel class.
     *
     * Calculates the the next time in milliseconds when the request is allowed to call again.
     */
    private class RequestFunnel {

        private long nextRun = -1;

        public boolean isAllowed() {
            return System.currentTimeMillis() >= this.nextRun;
        }

        public void update(RateLimitStatus status) {
            int remaining = status.getRemaining();

            if (remaining == 0) {
                // No calls remain. Wait for the next rate limit reset.
                this.nextRun = status.getResetTimeInSeconds() * 1000;
                return;
            }

            // Calculate the next possible run.
            long period = status.getSecondsUntilReset() * 1000 / remaining;
            this.nextRun = Math.max(System.currentTimeMillis() + period, this.nextRun);
        }

    }
}
