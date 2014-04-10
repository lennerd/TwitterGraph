package com.lennerd.processing.twitter_graph.status_collector.request;

import com.lennerd.processing.twitter_graph.status_collector.DataCollector;
import twitter4j.*;

public class PlaceStatusesRequest extends AbstractRequest {

    private Place place;

    public PlaceStatusesRequest(DataCollector dataCollector, Place place) {
        super(dataCollector);

        this.place = place;
    }

    @Override
    public void run(Twitter twitter) throws TwitterException {
        Query query = new Query("place:" + place.getId());
        QueryResult result = twitter.search(query);

        for (Status status : result.getTweets()) {
            this.dataCollector.addStatus(status);
        }
    }

}
