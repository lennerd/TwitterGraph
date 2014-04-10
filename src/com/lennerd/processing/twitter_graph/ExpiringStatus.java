package com.lennerd.processing.twitter_graph;

import twitter4j.Status;

public final class ExpiringStatus extends ExpiringEntityContainer {

    private static final long serialVersionUID = Sketch.SERIALIZATION_ID;

    private MyStatus status;

    public ExpiringStatus(Status status) {
        super();

        this.status = new MyStatus(status);
        this.expirationDate = status.getCreatedAt();
    }

    public MyStatus getStatus() {
        return this.status;
    }

    @Override
    public void focus() {
        if (this.drawbales.size() == 0) {
            return;
        }

        for (SkyObject drawable : this.drawbales) {
            if (drawable instanceof Point) {
                drawable.focus();
            }
        }
    }

}
