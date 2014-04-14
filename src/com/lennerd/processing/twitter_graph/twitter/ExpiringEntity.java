package com.lennerd.processing.twitter_graph.twitter;

import twitter4j.Status;

import java.util.Date;

abstract public class ExpiringEntity extends Entity {

    protected Date expirationDate;

    @Override
    public boolean addStatus(MyStatus status) {
        this.updateExpirationDate(status);

        return super.addStatus(status);
    }

    public void updateExpirationDate(Date expirationDate) {
        if (this.expirationDate == null || this.expirationDate.before(expirationDate)) {
            this.expirationDate = expirationDate;
        }
    }

    public void updateExpirationDate(MyStatus status) {
        this.updateExpirationDate(status.getCreatedAt());
    }

    public void updateExpirationDate(Status status) {
        this.updateExpirationDate(status.getCreatedAt());
    }

    public boolean isExpired(Date limit) {
        return this.expirationDate == null || this.expirationDate.before(limit);
    }



}
