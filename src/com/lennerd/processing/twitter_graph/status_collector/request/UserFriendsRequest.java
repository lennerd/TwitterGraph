package com.lennerd.processing.twitter_graph.status_collector.request;

import com.lennerd.processing.twitter_graph.status_collector.DataCollector;
import twitter4j.*;

public class UserFriendsRequest extends AbstractRequest {

    private Status status;
    private User user;

    public UserFriendsRequest(DataCollector dataCollector, Status status, User user) {
        super(dataCollector);

        this.status = status;
        this.user = user;
    }

    @Override
    public void run(Twitter twitter) throws TwitterException {
        PagableResponseList<User> friends = twitter.getFriendsList(user.getId(), -1);

        for (User user : friends) {
            this.dataCollector.addUser(this.status, user);
        }
    }

}
