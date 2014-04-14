package com.lennerd.processing.twitter_graph;

import com.lennerd.processing.twitter_graph.status_collector.Storage;
import com.lennerd.processing.twitter_graph.twitter.*;
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
    private final ExpiringHashMap<Long, MyStatus> statusStorage;
    private final ExpiringHashMap<Long, MyUser> userStorage;
    private final ExpiringHashMap<String, MyPlace> placeStorage;
    private final ExpiringHashMap<String, MyHashtag> hashtagStorage;
    private final Map<Long, List<MyStatus>> replyStorage;
    private final Map<Long, List<MyStatus>> retweetStorage;

    public Sky(int field, int amount) {
        this.field = field;
        this.amount = amount;

        this.lines = new CopyOnWriteArrayList<SkyObject>();
        this.points = new CopyOnWriteArrayList<SkyObject>();
        this.circles = new CopyOnWriteArrayList<SkyObject>();
        this.removeDrawable = new CopyOnWriteArrayList<SkyObject>();

        this.statusStorage = new ExpiringHashMap<Long, MyStatus>(field, amount);
        this.userStorage = new ExpiringHashMap<Long, MyUser>(field, amount);
        this.placeStorage = new ExpiringHashMap<String, MyPlace>(field, amount);
        this.hashtagStorage = new ExpiringHashMap<String, MyHashtag>(field, amount);
        this.replyStorage = new HashMap<Long, List<MyStatus>>();
        this.retweetStorage = new HashMap<Long, List<MyStatus>>();
    }

    @Override
    public boolean addStatus(Status status) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(this.field, this.amount);

        if (status.getCreatedAt().before(calendar.getTime())) {
            return false;
        }

        long id = status.getId();
        MyStatus myStatus = this.statusStorage.get(id);

        if (myStatus != null) {
            myStatus.focus();

            return false;
        }

        myStatus = new MyStatus(status);

        if (myStatus.getGeoLocation() == null) {
            return false;
        }

        SkyObject point = new Point(myStatus);

        this.points.addIfAbsent(point);
        myStatus.addDrawable(point);

        this.statusStorage.put(id, myStatus);

        this.checkReply(myStatus);
        this.checkRetweet(myStatus);

        return true;

    }

    private void checkReply(MyStatus myStatus) {
        List<MyStatus> replyList = this.replyStorage.remove(myStatus.getId());

        if (replyList != null) {
            for (MyStatus reply : replyList) {
                SkyObject line = new Line(reply, myStatus);

                this.lines.addIfAbsent(line);

                reply.addDrawable(line);
                myStatus.addDrawable(line);

                reply.addStatus(myStatus);
                myStatus.addStatus(reply);
            }
        }

        long repliedStatusId = myStatus.getInReplyToStatusId();

        if (repliedStatusId == -1) {
            return;
        }

        MyStatus repliedStatus = this.statusStorage.get(repliedStatusId);

        if (repliedStatus != null) {
            SkyObject line = new Line(myStatus, repliedStatus);

            this.lines.addIfAbsent(line);

            myStatus.addDrawable(line);
            repliedStatus.addDrawable(line);

            myStatus.addStatus(repliedStatus);
            repliedStatus.addStatus(myStatus);

            return;
        }

        replyList = this.replyStorage.get(repliedStatusId);

        if (replyList == null) {
            replyList = new ArrayList<MyStatus>();

            this.replyStorage.put(repliedStatusId, replyList);
        }

        replyList.add(myStatus);
    }

    private void checkRetweet(MyStatus myStatus) {
        long statusId = myStatus.getId();

        // Check for tweets that are retweets of the given status
        List<MyStatus> retweetList = this.retweetStorage.remove(statusId);

        // Is retweeted?
        if (retweetList != null) {
            // Yeah! We have a waiting list for this status. Draw the lines!
            for (MyStatus retweet : retweetList) {
                SkyObject line = new Line(retweet, myStatus);

                this.lines.addIfAbsent(line);

                retweet.addDrawable(line);
                myStatus.addDrawable(line);

                retweet.addStatus(myStatus);
                myStatus.addStatus(retweet);
            }
        }

        // Is a retweet?
        MyStatus retweetedStatus = myStatus.getRetweetedStatus();

        if (retweetedStatus == null) {
            // No retweeted status
            return;
        }

        long retweetedStatusId = retweetedStatus.getId();
        retweetedStatus = this.statusStorage.get(retweetedStatusId);

        if (retweetedStatus != null) {
            // Retweeted status already in storage, so add line and dependencies
            SkyObject line = new Line(myStatus, retweetedStatus);

            this.lines.addIfAbsent(line);

            myStatus.addDrawable(line);
            retweetedStatus.addDrawable(line);

            myStatus.addStatus(retweetedStatus);
            retweetedStatus.addStatus(myStatus);

            return;
        }

        // Save to waiting list
        retweetList = this.retweetStorage.get(retweetedStatusId);

        if (retweetList == null) {
            retweetList = new ArrayList<MyStatus>();

            this.replyStorage.put(retweetedStatusId, retweetList);
        }

        retweetList.add(myStatus);
    }

    @Override
    public boolean addUser(Status status, User user) {
        MyStatus myStatus = this.statusStorage.get(status.getId());

        if (myStatus == null) {
            if (!this.addStatus(status)) {
                return false;
            }

            myStatus = this.statusStorage.get(status.getId());
        }

        return this.addUser(myStatus, user);
    }

    public boolean addUser(MyStatus status, User user) {
        if (!user.isGeoEnabled()) {
            return false;
        }

        boolean added = false;
        long id = user.getId();
        MyUser myUser = this.userStorage.get(id);

        if (myUser == null) {
            myUser = new MyUser(user);

            this.userStorage.put(id, myUser);

            added = true;
        } else {
            myUser.focus();
        }

        myUser.addStatus(status);

        return added;
    }

    @Override
    public boolean addPlace(Status status, Place place) {
        MyStatus myStatus = this.statusStorage.get(status.getId());

        if (myStatus == null) {
            if (!this.addStatus(status)) {
                return false;
            }

            myStatus = this.statusStorage.get(status.getId());
        }

        return this.addPlace(myStatus, place);
    }


    public boolean addPlace(MyStatus status, Place place) {
        boolean added = false;
        String id = place.getId();
        MyPlace myPlace = this.placeStorage.get(id);

        if (myPlace == null) {
            myPlace = new MyPlace(place);

            if (place.getBoundingBoxCoordinates() != null) {
                SkyObject circle = new Circle(myPlace);

                this.circles.addIfAbsent(circle);
                myPlace.addDrawable(circle);
            }

            this.placeStorage.put(id, myPlace);

            added = true;
        } else {
            myPlace.focus();
        }

        myPlace.addStatus(status);

        return added;
    }

    @Override
    public boolean addHashtag(Status status, HashtagEntity entity) {
        MyStatus myStatus = this.statusStorage.get(status.getId());

        if (myStatus == null) {
            if (!this.addStatus(status)) {
                return false;
            }

            myStatus = this.statusStorage.get(status.getId());
        }

        return this.addHashtag(myStatus, entity);
    }

    public boolean addHashtag(MyStatus status, HashtagEntity entity) {
        if (status.getGeoLocation() == null) {
            return false;
        }

        boolean added = false;
        String text = entity.getText();
        MyHashtag myHashtag = this.hashtagStorage.get(text);

        if (myHashtag == null) {
            myHashtag = new MyHashtag(entity);

            this.hashtagStorage.put(text, myHashtag);

            added = true;
        }

        if (myHashtag.addStatus(status)) {
            List<MyStatus> hashtaggedStatuses = myHashtag.getStatuses();
            MyStatus myStatus = this.statusStorage.get(status.getId());

            if (hashtaggedStatuses.size() > 1) {
                for (MyStatus hashtaggedStatus : hashtaggedStatuses) {
                    long hashtaggedStatusId = hashtaggedStatus.getId();

                    if (hashtaggedStatusId == status.getId()) {
                        continue;
                    }

                    hashtaggedStatus = this.statusStorage.get(hashtaggedStatusId);

                    if (hashtaggedStatus != null) {
                        Line line = new Line(status, hashtaggedStatus);

                        this.lines.addIfAbsent(line);

                        myStatus.addDrawable(line);
                        myHashtag.addDrawable(line);
                        hashtaggedStatus.addDrawable(line);
                    }
                }
            }
        }

        myHashtag.addStatus(status);

        if (!added) {
            myHashtag.focus();
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

    private class ExpiringHashMap<K, V extends ExpiringEntity> extends ConcurrentHashMap<K, V> {

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
            CopyOnWriteArrayList<SkyObject> list;

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
                ExpiringEntity expiringObject = entry.getValue();

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

    public boolean addEntityContainer(ExpiringEntity entityContainer) {
        if (entityContainer instanceof MyStatus) {
            MyStatus myStatus = (MyStatus) entityContainer;
            long statusId = myStatus.getId();

            if (this.statusStorage.containsKey(statusId)) {
                return false;
            }

            this.statusStorage.putWithoutCheck(statusId, myStatus);
            return true;
        }

        if (entityContainer instanceof MyUser) {
            MyUser myUser = (MyUser) entityContainer;
            long userId = myUser.getId();

            if (this.userStorage.containsKey(userId)) {
                return false;
            }

            this.userStorage.putWithoutCheck(userId, myUser);
            return true;
        }

        if (entityContainer instanceof MyPlace) {
            MyPlace myPlace = (MyPlace) entityContainer;
            String placeId = myPlace.getId();

            if (this.placeStorage.containsKey(placeId)) {
                return false;
            }

            this.placeStorage.putWithoutCheck(placeId, myPlace);
            return true;
        }

        if (entityContainer instanceof MyHashtag) {
            MyHashtag myHashtag = (MyHashtag) entityContainer;
            String text = myHashtag.getText();

            if (this.hashtagStorage.containsKey(text)) {
                return false;
            }

            this.hashtagStorage.putWithoutCheck(text, myHashtag);
            return true;
        }

        throw new IllegalArgumentException("Unkown entity type.");
    }

    public ArrayList<ExpiringEntity> getEntityContainers() {
        ArrayList<ExpiringEntity> entityContainers = new ArrayList<ExpiringEntity>();

        entityContainers.addAll(this.statusStorage.values());
        entityContainers.addAll(this.userStorage.values());
        entityContainers.addAll(this.placeStorage.values());
        entityContainers.addAll(this.hashtagStorage.values());

        return entityContainers;
    }

}
