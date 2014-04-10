package com.lennerd.processing.twitter_graph;

import twitter4j.GeoLocation;
import twitter4j.Place;

import java.io.Serializable;

public final class MyPlace implements Serializable {

    private static final long serialVersionUID = Sketch.SERIALIZATION_ID;

    private final String id;
    private final GeoLocation[][] boundingBoxCoordinates;
    private final String name;

    public MyPlace(Place place) {
        this.id = place.getId();
        this.boundingBoxCoordinates = place.getBoundingBoxCoordinates();
        this.name = place.getName();
    }

    public String getId() {
        // TODO Auto-generated method stub
        return this.id;
    }

    public GeoLocation[][] getBoundingBoxCoordinates() {
        return this.boundingBoxCoordinates;
    }

    public String getName() {
        return this.name;
    }

}
