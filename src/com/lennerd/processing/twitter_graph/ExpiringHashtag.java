package com.lennerd.processing.twitter_graph;

import twitter4j.HashtagEntity;
import twitter4j.Status;

import java.util.ArrayList;
import java.util.List;

public final class ExpiringHashtag extends ExpiringEntityContainer {

    private static final long serialVersionUID = Sketch.SERIALIZATION_ID;

    private final List<MyStatus> statuses;
    private final MyHashtag hashtag;

    public ExpiringHashtag(HashtagEntity entity) {
        super();

        this.statuses = new ArrayList<MyStatus>();
        this.hashtag = new MyHashtag(entity);
    }

    public boolean addStatus(Status status) {
        return this.addStatus(new MyStatus(status));
    }

    public boolean addStatus(MyStatus status) {
        if (!this.statuses.contains(status)) {
            return this.statuses.add(status);
        }

        return false;
    }

    public List<MyStatus> getStatuses() {
        return this.statuses;
    }

    public MyHashtag getHashtag() {
        // TODO Auto-generated method stub
        return this.hashtag;
    }
}
