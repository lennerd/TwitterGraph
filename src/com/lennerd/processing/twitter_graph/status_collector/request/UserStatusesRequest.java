package com.lennerd.processing.twitter_graph.status_collector.request;

import com.lennerd.processing.twitter_graph.status_collector.DataCollector;
import twitter4j.*;

public class UserStatusesRequest extends AbstractRequest {

    private long userId;

    public UserStatusesRequest(DataCollector dataCollector, long userId) {
        super(dataCollector);

        this.userId = userId;
    }

    public UserStatusesRequest(DataCollector dataCollector, User user) {
        this(dataCollector, user.getId());
    }

    @Override
    public void run(Twitter twitter) throws TwitterException {
        ResponseList<Status> statuses = twitter.getUserTimeline(this.userId);

        for (Status status : statuses) {
            this.dataCollector.addStatus(status);
        }
    }

}
