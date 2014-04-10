package com.lennerd.processing.twitter_graph.status_collector;

import com.lennerd.processing.twitter_graph.status_collector.request.Request;

import java.util.ArrayDeque;

/**
 * Request queue.
 *
 * It adds a max size constraint and deletes elements, when this size gets exceeded.
 */
public class RequestQueue extends ArrayDeque<Request> {

    private long maxSize;

    public RequestQueue(long maxSize) {
        super();

        this.maxSize = maxSize;
    }

    /**
     * Adds a request to the queue.
     *
     * @param request The new request for the queue.
     * @return Weather elements was added or not.
     */
    @Override
    public boolean add(Request request) {
        if (this.maxSize > -1) {
            while (this.size() >= this.maxSize) {
                this.remove();
            }
        }

        return super.add(request);
    }

}
