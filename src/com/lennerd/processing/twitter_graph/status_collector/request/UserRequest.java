package com.lennerd.processing.twitter_graph.status_collector.request;

import com.lennerd.processing.twitter_graph.status_collector.DataCollector;
import twitter4j.*;

public class UserRequest extends AbstractRequest {

    private Status status;
    private UserMentionEntity entity;

    public UserRequest(DataCollector dataCollector, Status status, UserMentionEntity entity) {
        super(dataCollector);

        this.status = status;
        this.entity = entity;
    }

    @Override
    public void run(Twitter twitter) throws TwitterException {
        User user = twitter.showUser(entity.getId());

        this.dataCollector.addUser(this.status, user);
    }

}
