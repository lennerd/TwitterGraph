package com.lennerd.processing.twitter_graph.twitter;

import twitter4j.GeoLocation;
import twitter4j.Place;

public class MyPlace extends ExpiringEntity {

    private final String id;
    private final GeoLocation[][] boundingBoxCoordinates;
    private final String name;

    public MyPlace(Place place) {
        super();

        this.id = place.getId();
        this.boundingBoxCoordinates = place.getBoundingBoxCoordinates();
        this.name = place.getName();
    }

    public String getId() {
        return this.id;
    }

    public GeoLocation[][] getBoundingBoxCoordinates() {
        return this.boundingBoxCoordinates;
    }

    public String getName() {
        return this.name;
    }

}
