package com.lennerd.processing.twitter_graph;

import com.lennerd.processing.twitter_graph.status_collector.Storage;
import twitter4j.HashtagEntity;
import twitter4j.Place;
import twitter4j.Status;
import twitter4j.User;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public final class Sky implements Storage, Serializable {

    private static final long serialVersionUID = 1L;

    private final int field, amount;
    private final CopyOnWriteArrayList<SkyObject> lines, points, circles;
    private final CopyOnWriteArrayList<SkyObject> removeDrawable;
    private final ExpiringHashMap<Long, ExpiringStatus> statusStorage;
    private final ExpiringHashMap<Long, ExpiringUser> userStorage;
    private final ExpiringHashMap<String, ExpiringPlace> placeStorage;
    private final ExpiringHashMap<String, ExpiringHashtag> hashtagStorage;
    private final Map<Long, List<ExpiringStatus>> replyStorage;
    private final Map<Long, List<ExpiringStatus>> retweetStorage;

    public Sky(int field, int amount) {
        this.field = field;
        this.amount = amount;

        this.lines = new CopyOnWriteArrayList<SkyObject>();
        this.points = new CopyOnWriteArrayList<SkyObject>();
        this.circles = new CopyOnWriteArrayList<SkyObject>();
        this.removeDrawable = new CopyOnWriteArrayList<SkyObject>();

        this.statusStorage = new ExpiringHashMap<Long, ExpiringStatus>(field, amount);
        this.userStorage = new ExpiringHashMap<Long, ExpiringUser>(field, amount);
        this.placeStorage = new ExpiringHashMap<String, ExpiringPlace>(field, amount);
        this.hashtagStorage = new ExpiringHashMap<String, ExpiringHashtag>(field, amount);
        this.replyStorage = new HashMap<Long, List<ExpiringStatus>>();
        this.retweetStorage = new HashMap<Long, List<ExpiringStatus>>();
    }

    @Override
    public boolean addStatus(Status status) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(this.field, this.amount);

        if (status.getCreatedAt().before(calendar.getTime())) {
            return false;
        }

        boolean added = false;
        long id = status.getId();
        ExpiringStatus expiringStatus = this.statusStorage.get(id);

        if (expiringStatus == null) {
            expiringStatus = new ExpiringStatus(status);

            if (status.getGeoLocation() != null) {
                SkyObject point = new Point(expiringStatus.getStatus());

                this.points.addIfAbsent(point);
                expiringStatus.addDrawable(point);

                this.statusStorage.put(id, expiringStatus);

                added = true;
            }
        } else {
            expiringStatus.focus();
        }

        this.checkReply(expiringStatus);
        this.checkRetweet(expiringStatus);

        return added;
    }

    private void checkReply(ExpiringStatus expiringStatus) {
        // TODO cleanup!
        MyStatus status = expiringStatus.getStatus();
        long statusId = status.getId();
        List<ExpiringStatus> replyList = this.replyStorage.remove(statusId);

        if (status.getGeoLocation() == null) {
            return;
        }

        if (replyList != null) {
            for (ExpiringStatus expiringReply : replyList) {
                MyStatus reply = expiringReply.getStatus();
                SkyObject line = new Line(reply, status);

                this.lines.addIfAbsent(line);

                expiringReply.addDrawable(line);
                expiringStatus.addDrawable(line);

                expiringReply.updateExpirationDate(status);
                expiringStatus.updateExpirationDate(reply);
            }
        }

        long repliedStatusId = status.getInReplyToStatusId();

        if (repliedStatusId == -1) {
            return;
        }

        ExpiringStatus expiringRepliedStatus = this.statusStorage.get(repliedStatusId);

        if (expiringRepliedStatus != null) {
            MyStatus repliedStatus = expiringRepliedStatus.getStatus();
            SkyObject line = new Line(status, repliedStatus);

            this.lines.addIfAbsent(line);

            expiringStatus.addDrawable(line);
            expiringRepliedStatus.addDrawable(line);

            expiringStatus.updateExpirationDate(repliedStatus);
            expiringRepliedStatus.updateExpirationDate(status);

            return;
        }

        replyList = this.replyStorage.get(repliedStatusId);

        if (replyList == null) {
            replyList = new ArrayList<ExpiringStatus>();

            this.replyStorage.put(repliedStatusId, replyList);
        }

        replyList.add(expiringStatus);
    }

    private void checkRetweet(ExpiringStatus expiringStatus) {
        MyStatus status = expiringStatus.getStatus();
        long statusId = status.getId();

        // Check for tweets that are retweets of the given status
        List<ExpiringStatus> retweetList = this.retweetStorage.remove(statusId);

        if (status.getGeoLocation() == null) {
            // No geolocation, so forget that status
            return;
        }


        // Is retweeted?
        if (retweetList != null) {
            // Yeah! We have a waiting list for this status. Draw the lines!
            for (ExpiringStatus expiringRetweet : retweetList) {
                MyStatus retweet = expiringRetweet.getStatus();
                SkyObject line = new Line(retweet, status);

                this.lines.addIfAbsent(line);

                expiringRetweet.addDrawable(line);
                expiringStatus.addDrawable(line);

                expiringRetweet.updateExpirationDate(status);
                expiringStatus.updateExpirationDate(retweet);
            }
        }

        // Is a retweet?
        MyStatus retweetedStatus = status.getRetweetedStatus();

        if (retweetedStatus == null) {
            // No retweeted status
            return;
        }

        long retweetedStatusId = retweetedStatus.getId();
        ExpiringStatus expiringRetweetedStatus = this.statusStorage.get(retweetedStatusId);

        if (expiringRetweetedStatus != null) {
            // Retweeted status allready in storage, so add line and dependencies
            retweetedStatus = expiringRetweetedStatus.getStatus();
            SkyObject line = new Line(status, retweetedStatus);

            this.lines.addIfAbsent(line);

            expiringStatus.addDrawable(line);
            expiringRetweetedStatus.addDrawable(line);

            expiringStatus.updateExpirationDate(retweetedStatus);
            expiringRetweetedStatus.updateExpirationDate(status);

            return;
        }

        // Save to waiting list
        retweetList = this.retweetStorage.get(retweetedStatusId);

        if (retweetList == null) {
            retweetList = new ArrayList<ExpiringStatus>();

            this.replyStorage.put(retweetedStatusId, retweetList);
        }

        retweetList.add(expiringStatus);
    }

    @Override
    public boolean addUser(Status status, User user) {
        if (!user.isGeoEnabled()) {
            return false;
        }

        boolean added = false;
        long id = user.getId();
        ExpiringUser expiringUser = this.userStorage.get(id);

        if (expiringUser == null) {
            expiringUser = new ExpiringUser(user);

            this.userStorage.put(id, expiringUser);

            added = true;
        } else {
            expiringUser.focus();
        }

        expiringUser.updateExpirationDate(status);

        return added;
    }

    @Override
    public boolean addPlace(Status status, Place place) {
        boolean added = false;
        String id = place.getId();
        ExpiringPlace expiringPlace = this.placeStorage.get(id);

        if (expiringPlace == null) {
            expiringPlace = new ExpiringPlace(place);

            if (place.getBoundingBoxCoordinates() != null) {
                SkyObject circle = new Circle(expiringPlace.getPlace());

                this.circles.addIfAbsent(circle);
                expiringPlace.addDrawable(circle);
            }

            this.placeStorage.put(id, expiringPlace);

            added = true;
        } else {
            expiringPlace.focus();
        }

        expiringPlace.updateExpirationDate(status);

        return added;
    }

    @Override
    public boolean addHashtag(Status status, HashtagEntity entity) {
        return this.addHashtag(new MyStatus(status), entity);
    }

    public boolean addHashtag(MyStatus status, HashtagEntity entity) {
        if (status.getGeoLocation() == null) {
            return false;
        }

        boolean added = false;
        String text = entity.getText();
        ExpiringHashtag expiringHashtag = this.hashtagStorage.get(text);

        if (expiringHashtag == null) {
            expiringHashtag = new ExpiringHashtag(entity);

            this.hashtagStorage.put(text, expiringHashtag);

            added = true;
        }

        if (expiringHashtag.addStatus(status)) {
            List<MyStatus> hashtaggedStatuses = expiringHashtag.getStatuses();
            ExpiringStatus expiringStatus = this.statusStorage.get(status.getId());

            if (hashtaggedStatuses.size() > 1) {
                for (MyStatus hashtaggedStatus : hashtaggedStatuses) {
                    long hashtaggedStatusId = hashtaggedStatus.getId();

                    if (hashtaggedStatusId == status.getId()) {
                        continue;
                    }

                    ExpiringStatus expiringHashtaggedStatus = this.statusStorage.get(hashtaggedStatusId);

                    if (expiringHashtaggedStatus != null) {
                        Line line = new Line(status, hashtaggedStatus);

                        this.lines.addIfAbsent(line);

                        expiringStatus.addDrawable(line);
                        expiringHashtag.addDrawable(line);
                        expiringHashtaggedStatus.addDrawable(line);

                        //expiringHashtaggedStatus.updateExpirationDate(status);
                    }
                }
            }
        }

        expiringHashtag.updateExpirationDate(status);

        if (!added) {
            expiringHashtag.focus();
        }

        return added;
    }

    public List<SkyObject> getRemoveDrawable() {
        return this.removeDrawable;
    }

    public List<SkyObject> getPoints() {
        return this.points;
    }

    public List<SkyObject> getLines() {
        return this.lines;
    }

    public List<SkyObject> getCircles() {
        return this.circles;
    }

    public List<SkyObject> getDrawables() {
        List<SkyObject> drawables = new ArrayList<SkyObject>(this.points);
        drawables.addAll(this.lines);
        drawables.addAll(this.circles);

        return drawables;
    }

    private class ExpiringHashMap<K, V extends ExpiringEntityContainer> extends ConcurrentHashMap<K, V> {

        private static final long serialVersionUID = 1L;
        private int field;
        private int amount;
        private Executor executor;
        private Runnable expirationChecker;

        public ExpiringHashMap(int field, int amount) {
            this.field = field;
            this.amount = amount;

            this.executor = Executors.newCachedThreadPool();
            this.expirationChecker = new Runnable() {
                public void run() {
                    ExpiringHashMap.this.expire();
                }
            };
        }

        private V putWithoutCheck(K key, V value) {
            CopyOnWriteArrayList<SkyObject> list = null;

            for (SkyObject drawable : value.getDrawables()) {
                if (drawable instanceof Point) {
                    list = Sky.this.points;
                } else if (drawable instanceof Line) {
                    list = Sky.this.lines;
                } else if (drawable instanceof Circle) {
                    list = Sky.this.circles;
                } else {
                    throw new IllegalArgumentException("Unkown drawable.");
                }

                if (!list.contains(drawable)) {
                    list.addIfAbsent(drawable);
                }
            }

            return super.put(key, value);
        }

        @Override
        public V put(K key, V value) {
            this.executor.execute(this.expirationChecker);

            return this.putWithoutCheck(key, value);
        }

        private void expire() {
            Calendar calendar = Calendar.getInstance();
            calendar.add(this.field, this.amount);

            Date limit = calendar.getTime();

            for (Map.Entry<K, V> entry : this.entrySet()) {
                ExpiringEntityContainer expiringObject = entry.getValue();

                if (expiringObject.isExpired(limit)) {
                    this.remove(entry.getKey());

                    for (SkyObject drawable : expiringObject.getDrawables()) {
                        if (drawable != null && !Sky.this.removeDrawable.contains(drawable)) {
                            Sky.this.removeDrawable.add(drawable);
                        }
                    }
                }
            }
        }

    }

    public boolean addEntityContainer(ExpiringEntityContainer entityContainer) {
        if (entityContainer instanceof ExpiringStatus) {
            ExpiringStatus expiringStatus = (ExpiringStatus) entityContainer;
            long statusId = expiringStatus.getStatus().getId();

            if (this.statusStorage.containsKey(statusId)) {
                return false;
            }

            this.statusStorage.putWithoutCheck(statusId, expiringStatus);
            return true;
        }

        if (entityContainer instanceof ExpiringUser) {
            ExpiringUser expiringUser = (ExpiringUser) entityContainer;
            long userId = expiringUser.getUser().getId();

            if (this.userStorage.containsKey(userId)) {
                return false;
            }

            this.userStorage.putWithoutCheck(userId, expiringUser);
            return true;
        }

        if (entityContainer instanceof ExpiringPlace) {
            ExpiringPlace expiringPlace = (ExpiringPlace) entityContainer;
            String placeId = expiringPlace.getPlace().getId();

            if (this.placeStorage.containsKey(placeId)) {
                return false;
            }

            this.placeStorage.putWithoutCheck(placeId, expiringPlace);
            return true;
        }

        if (entityContainer instanceof ExpiringHashtag) {
            ExpiringHashtag expiringHashtag = (ExpiringHashtag) entityContainer;
            String text = expiringHashtag.getHashtag().getText();

            if (this.hashtagStorage.containsKey(text)) {
                return false;
            }

            this.hashtagStorage.putWithoutCheck(text, expiringHashtag);
            return true;
        }

        throw new IllegalArgumentException("Unkown entity type.");
    }

    public ArrayList<ExpiringEntityContainer> getEntityContainers() {
        ArrayList<ExpiringEntityContainer> entityContainers = new ArrayList<ExpiringEntityContainer>();

        entityContainers.addAll(this.statusStorage.values());
        entityContainers.addAll(this.userStorage.values());
        entityContainers.addAll(this.placeStorage.values());
        entityContainers.addAll(this.hashtagStorage.values());

        return entityContainers;
    }

}
