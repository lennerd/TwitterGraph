package com.lennerd.processing.twitter_graph;

import twitter4j.User;

import java.io.Serializable;

public final class MyUser implements Serializable {

    private static final long serialVersionUID = Sketch.SERIALIZATION_ID;

    private final long id;

    public MyUser(User user) {
        this.id = user.getId();
    }

    public long getId() {
        // TODO Auto-generated method stub
        return this.id;
    }

}
