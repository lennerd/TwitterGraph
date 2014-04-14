package com.lennerd.processing.twitter_graph;

import com.lennerd.processing.twitter_graph.twitter.MyPlace;
import com.lennerd.processing.twitter_graph.twitter.MyStatus;
import de.looksgood.ani.Ani;
import de.looksgood.ani.AniSequence;
import processing.core.PApplet;
import processing.core.PGraphics;
import twitter4j.GeoLocation;

public class Circle extends SkyObject {

    private static final long serialVersionUID = Sketch.SERIALIZATION_ID;

    private final MyPlace place;
    private final String info;
    private float x, y, ellipseX, ellipseY;
    private int size;
    private float radius = 0;
    private float opacity;
    private StatusColor newColor;

    public Circle(MyPlace place) {
        this.place = place;
        this.info = place.getName();
    }

    @Override
    public void drawGraphics(PGraphics graphics) {
        if (this.newColor != null) {
            Ani.to(this.color, Sketch.ANI_DURATION, "red", this.newColor.red);
            Ani.to(this.color, Sketch.ANI_DURATION, "green", this.newColor.green);
            Ani.to(this.color, Sketch.ANI_DURATION, "blue", this.newColor.blue);

            this.newColor = null;
        }

        graphics.noStroke();
        graphics.fill(this.color.red, this.color.green, this.color.blue, this.opacity);

        graphics.ellipseMode(PApplet.RADIUS);
        graphics.ellipse(this.ellipseX, this.ellipseY, this.radius, this.radius);
    }

    @Override
    public void onAdd(PApplet sketch) {
        GeoLocation[][] boundingBox = place.getBoundingBoxCoordinates();
        GeoLocation topLeft = boundingBox[0][0];
        GeoLocation bottomRight = boundingBox[0][2];
        float x1, x2, y1, y2, a, b;

        x1 = Sketch.calculateLongitude(sketch, topLeft);
        x2 = Sketch.calculateLongitude(sketch, bottomRight);

        y1 = Sketch.calculateLatitude(sketch, topLeft);
        y2 = Sketch.calculateLatitude(sketch, bottomRight);

        a = x2 - x1;
        b = y2 - y1;

        this.opacity = 30F;
        float radius = PApplet.sqrt(PApplet.pow(a, 2) + PApplet.pow(b, 2)) / 2F;

        Ani.to(this, Sketch.ANI_DURATION, "radius", radius);

        this.x = x1;
        this.y = y1;
        this.ellipseX = radius;
        this.ellipseY = radius;
        this.size = PApplet.ceil(radius * 2);
    }

    @Override
    public void onRemove(PApplet sketch) {
        Ani.to(this, Sketch.ANI_DURATION, "radius", 0, Ani.getDefaultEasing(), "onEnd:onRemoved");
    }

    @Override
    public void onFocus(PApplet sketch) {
        AniSequence sequence = new AniSequence(sketch);

        sequence.beginSequence();
        sequence.add(Ani.to(this, Sketch.ANI_DURATION, "opacity", 80F, Ani.EXPO_IN));
        sequence.add(Ani.to(this, Sketch.ANI_DURATION, "opacity", 30F, Ani.EXPO_OUT));
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

    public MyPlace getPlace() {
        return this.place;
    }

    @Override
    public boolean equals(Object obj) {
        return null != obj && (this == obj || obj instanceof Circle && ((Circle) obj).getPlace().equals(this.place));
    }

    @Override
    public void addStatus(MyStatus status) {
        if (this.color == null) {
            this.color = new StatusColor(status);
            return;
        }

        this.newColor = this.color.clone();
        this.newColor.extend(status);
    }

    @Override
    public boolean isMouseOver(int mouseX, int mouseY) {
        return PApplet.dist(this.ellipseX, this.ellipseY, mouseX, mouseY) <= (this.radius + InfoBox.MOUSE_OVER_BORDER);
    }

    @Override
    public String getInfo() {
        return this.info;
    }

}
