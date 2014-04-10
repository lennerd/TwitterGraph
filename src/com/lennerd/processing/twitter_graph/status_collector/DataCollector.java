package com.lennerd.processing.twitter_graph.status_collector;

import com.lennerd.processing.twitter_graph.status_collector.request.*;
import twitter4j.*;

/**
 * The data collection as its name implies collects data from twitter.
 *
 * This implies related data for each entity.
 */
public class DataCollector {

    private Storage storage;
    private RequestProcessor requestProcessor;

    public DataCollector(Storage storage, RequestProcessor requestProcessor) {
        this.storage = storage;
        this.requestProcessor = requestProcessor;
    }

    public void addStatus(Status status) {
        if (!this.storage.addStatus(status)) {
            return;
        }

        this.addUser(status, status.getUser());

        Status retweetedStatus = status.getRetweetedStatus();

        if (retweetedStatus != null) {
            this.addStatus(retweetedStatus);
        }

        if (status.getInReplyToStatusId() > -1) {
            this.addRequest(new RepliedStatusRequest(this, status));
        }

        if (status.getRetweetCount() > 0) {
            this.addRequest(new StatusRetweetsRequest(this, status));
        }

        Place place = status.getPlace();

        if (place != null) {
            this.addPlace(status, place);
        }

        UserMentionEntity[] userMentioned = status.getUserMentionEntities();

        for (UserMentionEntity entity : userMentioned) {
            this.addRequest(new UserRequest(this, status, entity));
        }

        HashtagEntity[] hashtags = status.getHashtagEntities();

        for (HashtagEntity entity : hashtags) {
            this.addHashtag(status, entity);
        }
    }

    public void addUser(Status status, User user) {
        if (!this.storage.addUser(status, user)) {
            return;
        }

        if (user.getStatusesCount() > 0) {
            this.addRequest(new UserStatusesRequest(this, user));
        }

        if (user.getFollowersCount() > 0) {
            this.addRequest(new UserFollowersRequest(this, status, user));
        }

        if (user.getFriendsCount() > 0) {
            this.addRequest(new UserFriendsRequest(this, status, user));
        }
    }

    public void addPlace(Status status, Place place) {
        if (this.storage.addPlace(status, place)) {
            this.addRequest(new PlaceStatusesRequest(this, place));
        }
    }

    public void addHashtag(Status status, HashtagEntity entity) {
        if (this.storage.addHashtag(status, entity)) {
            this.addRequest(new HashtaggedStatusesRequest(this, entity));
        }
    }

    private void addRequest(Request request) {
        this.requestProcessor.getRequestQueue().add(request);
    }

    public class StreamListener extends StatusAdapter {

        public void onStatus(Status status) {
            DataCollector.this.addStatus(status);
        }

    }

}
