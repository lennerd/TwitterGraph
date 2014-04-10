package com.lennerd.processing.twitter_graph;

import twitter4j.User;

public final class ExpiringUser extends ExpiringEntityContainer {

    private static final long serialVersionUID = Sketch.SERIALIZATION_ID;

    private MyUser user;

    public ExpiringUser(User user) {
        super();

        this.user = new MyUser(user);
    }

    public MyUser getUser() {
        // TODO Auto-generated method stub
        return this.user;
    }

}
