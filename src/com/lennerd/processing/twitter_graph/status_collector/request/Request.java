package com.lennerd.processing.twitter_graph.status_collector.request;

import twitter4j.Twitter;
import twitter4j.TwitterException;

public interface Request {

    public String getName();

    public void run(Twitter twitter) throws TwitterException;

}