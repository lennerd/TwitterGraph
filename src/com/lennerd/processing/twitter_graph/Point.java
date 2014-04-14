package com.lennerd.processing.twitter_graph;

import com.lennerd.processing.twitter_graph.twitter.MyStatus;
import de.looksgood.ani.Ani;
import de.looksgood.ani.AniSequence;
import processing.core.PApplet;
import processing.core.PGraphics;
import twitter4j.GeoLocation;

public class Point extends SkyObject {

    private static final long serialVersionUID = Sketch.SERIALIZATION_ID;

    private final MyStatus status;
    private final String info;
    private float x, y;
    private float opacity = 0;
    private float radius;
    private int size;

    public Point(MyStatus status) {
        this.status = status;
        this.info = Point.buildInfo(status);
    }

    public MyStatus getStatus() {
        return this.status;
    }

    @Override
    public void drawGraphics(PGraphics graphics) {
        graphics.noStroke();
        graphics.fill(this.color.red, this.color.green, this.color.blue, this.opacity);

        graphics.ellipseMode(PApplet.RADIUS);
        graphics.ellipse(this.radius, this.radius, this.radius, this.radius);
    }

    @Override
    public void onAdd(PApplet sketch) {
        GeoLocation location = status.getGeoLocation();

        this.x = Sketch.calculateLongitude(sketch, location);
        this.y = Sketch.calculateLatitude(sketch, location);

        this.color = new StatusColor(this.status);
        this.radius = 1.5F;
        this.size = PApplet.ceil(this.radius * 2);

        Ani.to(this, Sketch.ANI_DURATION, "opacity", 255);
    }

    @Override
    public void onRemove(PApplet sketch) {
        Ani.to(this, Sketch.ANI_DURATION, "opacity", 0, Ani.getDefaultEasing(), "onEnd:onRemoved");
    }

    @Override
    public void onFocus(PApplet sketch) {
        AniSequence sequence = new AniSequence(sketch);

        sequence.beginSequence();
        sequence.add(Ani.to(this, Sketch.ANI_DURATION, "radius", 5F, Ani.EXPO_IN));
        sequence.add(Ani.to(this, Sketch.ANI_DURATION, "radius", 1.5F, Ani.EXPO_OUT));
        sequence.endSequence();

        sequence.start();
    }

    @Override
    public float getSketchX() {
        return this.x;
    }

    @Override
    public float getSketchY() {
        return this.y;
    }

    @Override
    public int getWidth() {
        return this.size;
    }

    @Override
    public int getHeight() {
        return this.size;
    }

    @Override
    public boolean equals(Object obj) {
        if (null == obj) {
            return false;
        }

        if (this == obj) {
            return true;
        }

        if (obj instanceof Point) {
            return ((Point) obj).getStatus().equals(this.status);
        }

        return false;
    }

    @Override
    public void addStatus(MyStatus status) {
    }

    @Override
    public boolean isMouseOver(int mouseX, int mouseY) {
        return PApplet.dist(this.x, this.y, mouseX, mouseY) <= this.radius + InfoBox.MOUSE_OVER_BORDER;
    }

    public static String buildInfo(MyStatus status) {
        return status.getUserName() + ": " + status.getText();
    }

    @Override
    public String getInfo() {
        // TODO Auto-generated method stub
        return this.info;
    }

}
