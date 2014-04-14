package com.lennerd.processing.twitter_graph;

import com.lennerd.processing.twitter_graph.twitter.MyPlace;
import com.lennerd.processing.twitter_graph.twitter.MyStatus;
import de.looksgood.ani.Ani;
import de.looksgood.ani.AniSequence;
import processing.core.PApplet;
import twitter4j.GeoLocation;

public final class Circle extends SkyObject {

    private static final long serialVersionUID = Sketch.SERIALIZATION_ID;

    private final MyPlace place;
    private final String info;
    private float x, y;
    private float radius = 0;
    private float opacity;
    private StatusColor newColor;

    public Circle(MyPlace place) {
        this.place = place;
        this.info = place.getName();
    }

    @Override
    public void onDraw(PApplet sketch) {
        sketch.pushMatrix();
        sketch.pushStyle();

        if (this.newColor != null) {
            Ani.to(this.color, Sketch.ANI_DURATION, "red", this.newColor.red);
            Ani.to(this.color, Sketch.ANI_DURATION, "green", this.newColor.green);
            Ani.to(this.color, Sketch.ANI_DURATION, "blue", this.newColor.blue);

            this.newColor = null;
        }

        sketch.noStroke();
        sketch.fill(this.color.red, this.color.green, this.color.blue, this.opacity);

        sketch.ellipseMode(PApplet.RADIUS);
        sketch.ellipse(this.x, this.y, this.radius, this.radius);

        sketch.popMatrix();
        sketch.popStyle();
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
        //this.color = new StatusColor(0x000000);

        Ani.to(this, Sketch.ANI_DURATION, "radius", PApplet.sqrt(PApplet.pow(a, 2) + PApplet.pow(b, 2)) / 2);

        this.x = x1 + a / 2;
        this.y = y1 + b / 2;
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
        return PApplet.dist(this.x, this.y, mouseX, mouseY) <= (this.radius + InfoBox.MOUSE_OVER_BORDER);
    }

    @Override
    public String getInfo() {
        return this.info;
    }

}
