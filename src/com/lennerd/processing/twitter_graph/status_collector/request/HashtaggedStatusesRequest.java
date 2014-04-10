package com.lennerd.processing.twitter_graph.status_collector.request;

import com.lennerd.processing.twitter_graph.status_collector.DataCollector;
import twitter4j.*;

public class HashtaggedStatusesRequest extends AbstractRequest {

    private HashtagEntity entity;

    public HashtaggedStatusesRequest(DataCollector dataCollector, HashtagEntity entity) {
        super(dataCollector);

        this.entity = entity;
    }

    @Override
    public void run(Twitter twitter) throws TwitterException {
        Query query = new Query("#" + entity.getText());
        QueryResult result = twitter.search(query);

        for (Status status : result.getTweets()) {
            this.dataCollector.addStatus(status);
        }
    }

}