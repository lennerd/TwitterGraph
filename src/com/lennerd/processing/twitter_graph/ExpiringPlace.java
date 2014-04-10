package com.lennerd.processing.twitter_graph;

import twitter4j.Place;

public final class ExpiringPlace extends ExpiringEntityContainer {

    private static final long serialVersionUID = Sketch.SERIALIZATION_ID;

    private final MyPlace place;

    public ExpiringPlace(Place place) {
        super();

        this.place = new MyPlace(place);
    }

    public MyPlace getPlace() {
        // TODO Auto-generated method stub
        return this.place;
    }

}
