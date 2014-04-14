package com.lennerd.processing.twitter_graph.status_collector.request;

import com.lennerd.processing.twitter_graph.status_collector.DataCollector;
import twitter4j.*;

public class UserFollowersRequest extends AbstractRequest {

    private Status status;
    private User user;

    public UserFollowersRequest(DataCollector dataCollector, Status status, User user) {
        super(dataCollector);

        this.status = status;
        this.user = user;
    }

    @Override
    public void run(Twitter twitter) throws TwitterException {
        PagableResponseList<User> followers = twitter.getFollowersList(user.getId(), 1);

        for (User user : followers) {
            this.dataCollector.addUser(this.status, user);
        }
    }

}
