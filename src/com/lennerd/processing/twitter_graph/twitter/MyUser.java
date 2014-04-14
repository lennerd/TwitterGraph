package com.lennerd.processing.twitter_graph.twitter;

import twitter4j.User;

public class MyUser extends ExpiringEntity {

    private final long id;

    public MyUser(User user) {
        super();

        this.id = user.getId();
    }

    public long getId() {
        // TODO Auto-generated method stub
        return this.id;
    }

}
