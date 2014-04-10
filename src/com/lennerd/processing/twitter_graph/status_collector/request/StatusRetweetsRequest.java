package com.lennerd.processing.twitter_graph.status_collector.request;

import twitter4j.ResponseList;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import com.lennerd.processing.twitter_graph.status_collector.DataCollector;

public class StatusRetweetsRequest extends AbstractRequest {

    private Status status;

    public StatusRetweetsRequest(DataCollector dataCollector, Status status) {
        super(dataCollector);

        this.status = status;
    }

    @Override
    public void run(Twitter twitter) throws TwitterException {
        ResponseList<Status> retweets = twitter.getRetweets(status.getId());

        for (Status status : retweets) {
            this.dataCollector.addStatus(status);
        }
    }

}
