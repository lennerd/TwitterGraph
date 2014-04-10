package com.lennerd.processing.twitter_graph.status_collector;

import twitter4j.HashtagEntity;
import twitter4j.Place;
import twitter4j.Status;
import twitter4j.User;

public interface Storage {

    public boolean addStatus(Status status);

    public boolean addUser(Status status, User user);

    public boolean addPlace(Status status, Place place);

    public boolean addHashtag(Status status, HashtagEntity hashtag);

}