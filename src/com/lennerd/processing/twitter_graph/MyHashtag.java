package com.lennerd.processing.twitter_graph;

import twitter4j.HashtagEntity;

import java.io.Serializable;

public final class MyHashtag implements Serializable {

    private static final long serialVersionUID = Sketch.SERIALIZATION_ID;

    private final String text;

    public MyHashtag(HashtagEntity entity) {
        this.text = entity.getText();
    }

    public String getText() {
        // TODO Auto-generated method stub
        return this.text;
    }

}
