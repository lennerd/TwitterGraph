package com.lennerd.processing.twitter_graph.twitter;

import twitter4j.HashtagEntity;

import java.util.ArrayList;
import java.util.List;

public class MyHashtag extends ExpiringEntity {

    private final List<MyStatus> statuses;
    private String text;

    public MyHashtag(HashtagEntity entity) {
        super();

        this.statuses = new ArrayList<MyStatus>();
        this.text = entity.getText();
    }

    public boolean addStatus(MyStatus status) {
        return super.addStatus(status) && !this.statuses.contains(status) && this.statuses.add(status);
    }

    public List<MyStatus> getStatuses() {
        return this.statuses;
    }

    public String getText() {
        return this.text;
    }

}
