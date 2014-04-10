package com.lennerd.processing.twitter_graph;

import twitter4j.GeoLocation;
import twitter4j.Status;

import java.io.Serializable;
import java.util.Date;

public final class MyStatus implements Serializable {

    private static final long serialVersionUID = Sketch.SERIALIZATION_ID;

    private final long id, inReplyToStatusId;
    private final GeoLocation geoLocation;
    private final Date createdAt;
    private final MyStatus retweetedStatus;
    private final String text;
    private final String userName;

    public MyStatus(Status status) {
        this.id = status.getId();
        this.geoLocation = status.getGeoLocation();
        this.createdAt = status.getCreatedAt();
        this.inReplyToStatusId = status.getInReplyToStatusId();
        this.text = status.getText();
        this.userName = status.getUser().getScreenName();

        Status retweetedStatus = status.getRetweetedStatus();

        if (retweetedStatus != null) {
            this.retweetedStatus = new MyStatus(retweetedStatus);
        } else {
            this.retweetedStatus = null;
        }
    }

    public long getId() {
        return this.id;
    }

    public GeoLocation getGeoLocation() {
        return this.geoLocation;
    }

    public Date getCreatedAt() {
        return this.createdAt;
    }

    public long getInReplyToStatusId() {
        return this.inReplyToStatusId;
    }

    public MyStatus getRetweetedStatus() {
        return this.retweetedStatus;
    }

    @Override
    public boolean equals(Object obj) {
        if (null == obj) {
            return false;
        }
        if (this == obj) {
            return true;
        }
        return obj instanceof MyStatus && ((MyStatus) obj).getId() == this.id;
    }

    public String getText() {
        return this.text;
    }

    public String getUserName() {
        return this.userName;
    }

}
