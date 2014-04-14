package com.lennerd.processing.twitter_graph.status_collector.request;

import com.lennerd.processing.twitter_graph.status_collector.DataCollector;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;

public class RepliedStatusRequest extends AbstractRequest {

    private Status status;

    public RepliedStatusRequest(DataCollector dataCollector, Status status) {
        super(dataCollector);

        this.status = status;
    }

    @Override
    public void run(Twitter twitter) throws TwitterException {
        Status repliedStatus = twitter.showStatus(this.status.getInReplyToStatusId());

        this.dataCollector.addStatus(repliedStatus);
    }
}
