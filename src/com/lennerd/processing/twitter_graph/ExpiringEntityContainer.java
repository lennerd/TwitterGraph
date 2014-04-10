package com.lennerd.processing.twitter_graph;

import twitter4j.Status;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

abstract public class ExpiringEntityContainer implements Serializable {

    private static final long serialVersionUID = Sketch.SERIALIZATION_ID;

    protected final List<SkyObject> drawbales;

    protected Date expirationDate;

    public ExpiringEntityContainer() {
        this.drawbales = new ArrayList<SkyObject>();
    }

    public void updateExpirationDate(MyStatus status) {
        Date createdAt = status.getCreatedAt();

        if (this.expirationDate == null || this.expirationDate.before(createdAt)) {
            this.expirationDate = createdAt;
        }

        for (SkyObject drawable : this.drawbales) {
            drawable.addStatus(status);
        }
    }

    public void updateExpirationDate(Status status) {
        this.updateExpirationDate(new MyStatus(status));
    }

    public boolean addDrawable(SkyObject drawable) {
        if (this.drawbales.contains(drawable)) {
            return false;
        }

        this.drawbales.add(drawable);

        return true;
    }

    public List<SkyObject> getDrawables() {
        return this.drawbales;
    }

    public boolean isExpired(Date limit) {
        return this.expirationDate == null || this.expirationDate.before(limit);
    }

    public void focus() {
        if (this.drawbales.size() == 0) {
            return;
        }

        for (SkyObject drawable : this.drawbales) {
            drawable.focus();
        }
    }

}
