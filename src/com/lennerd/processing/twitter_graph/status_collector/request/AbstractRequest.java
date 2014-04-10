package com.lennerd.processing.twitter_graph.status_collector.request;

import com.lennerd.processing.twitter_graph.status_collector.DataCollector;

abstract public class AbstractRequest implements Request {

    protected DataCollector dataCollector;

    public AbstractRequest(DataCollector dataCollector) {
        this.dataCollector = dataCollector;
    }

    @Override
    public String getName() {
        return this.getClass().getName();
    }
}
