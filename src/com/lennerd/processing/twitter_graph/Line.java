package com.lennerd.processing.twitter_graph;

import com.lennerd.processing.twitter_graph.twitter.MyStatus;
import de.looksgood.ani.Ani;
import de.looksgood.ani.AniSequence;
import processing.core.PApplet;
import processing.core.PVector;
import twitter4j.GeoLocation;

import java.io.Serializable;

public final class Line extends SkyObject {

    private static final long serialVersionUID = Sketch.SERIALIZATION_ID;

    private final MyStatus reply, status;
    private final String info;
    private float x1, x2, y1, y2;
    private float opacity;
    private Focus focus;
    private float radius;

    public Line(MyStatus reply, MyStatus status) {
        this.reply = reply;
        this.status = status;

        this.info = Point.buildInfo(status) + " — " + Point.buildInfo(reply);
    }

    @Override
    public void onDraw(PApplet sketch) {
        sketch.pushMatrix();
        sketch.pushStyle();

        sketch.noFill();
        sketch.stroke(this.color.red, this.color.green, this.color.blue,
                this.opacity);

        sketch.line(this.x1, this.y1, this.x2, this.y2);

        sketch.ellipseMode(PApplet.RADIUS);

        if (this.focus != null) {
            sketch.noStroke();
            sketch.fill(this.focus.color.red, this.focus.color.green,
                    this.focus.color.blue, this.focus.opacity);
            sketch.ellipse(this.focus.x, this.focus.y, this.radius, this.radius);
        }

        sketch.popMatrix();
        sketch.popStyle();
    }

    public MyStatus getReply() {
        return this.reply;
    }

    public MyStatus getStatus() {
        return this.status;
    }

    @Override
    public void onAdd(PApplet sketch) {
        GeoLocation statusLocation = this.status.getGeoLocation();
        GeoLocation replyLocation = this.reply.getGeoLocation();

        this.x1 = this.x2 = Sketch.calculateLongitude(sketch, statusLocation);
        this.y1 = this.y2 = Sketch.calculateLatitude(sketch, statusLocation);

        this.opacity = 50F;

        this.color = new StatusColor(this.status);
        this.color.extend(this.reply);

        this.radius = 1.5f;

        Ani.to(this,
                Sketch.ANI_DURATION,
                "x2:" + Sketch.calculateLongitude(sketch, replyLocation)
                        + ",y2:"
                        + Sketch.calculateLatitude(sketch, replyLocation)
        );
    }

    @Override
    public void onRemove(PApplet sketch) {
        Ani.to(this, Sketch.ANI_DURATION, "x2:" + this.x1 + ",y2:" + this.y1,
                Ani.getDefaultEasing(), "onEnd:onRemoved");
    }

    @Override
    public void onFocus(PApplet sketch) {
        this.focus = new Focus(this.x1, this.y1, this.color, Sketch.LINE_OPACITY);

        AniSequence sequence = new AniSequence(sketch);

        sequence.beginSequence();
        sequence.add(Ani.from(this.focus, Sketch.ANI_DURATION, "opacity", 0));
        sequence.add(Ani.to(this.focus, Sketch.ANI_DURATION, "x:" + this.x2
                + ",y:" + this.y2));
        sequence.add(Ani.to(this.focus, Sketch.ANI_DURATION, "opacity", 0,
                Ani.getDefaultEasing(), "onEnd:remove"));
        sequence.endSequence();

        sequence.start();
    }

    public void removePoint() {
        this.focus = null;
    }

    @Override
    public boolean equals(Object obj) {
        if (null == obj) {
            return false;
        }

        if (this == obj) {
            return true;
        }

        if (obj instanceof Line) {
            Line line = (Line) obj;
            MyStatus reply = line.getReply();
            MyStatus status = line.getStatus();

            if ((this.reply.equals(reply) && this.status.equals(status))
                    || (this.status.equals(reply) && this.reply.equals(status))) {
                return true;
            }
        }

        return false;
    }

    private class Focus implements Serializable {

        private static final long serialVersionUID = 1L;
        private float x;
        private float y;
        private StatusColor color;
        private float opacity;

        public Focus(float x, float y, StatusColor color, float opacity) {
            this.x = x;
            this.y = y;
            this.color = color;
            this.opacity = opacity;
        }

        @SuppressWarnings("unused")
        public void remove() {
            Line.this.focus = null;
        }
    }

    @Override
    public void addStatus(MyStatus status) {
    }

    @Override
    public boolean isMouseOver(int mouseX, int mouseY) {
        PVector lineStart, lineEnd, mouse, projection, temp;

        lineStart = new PVector(this.x1, this.y1);
        lineEnd = new PVector(this.x2, this.y2);
        mouse = new PVector(mouseX, mouseY);

        temp = PVector.sub(lineEnd, lineStart);

        float lineLength = temp.x * temp.x + temp.y * temp.y; // lineStart.dist(lineEnd);

        if (lineLength == 0F) {
            return mouse.dist(lineStart) <= InfoBox.MOUSE_OVER_BORDER;
        }

        float t = PVector.dot(PVector.sub(mouse, lineStart), temp) / lineLength;

        if (t < 0F) {
            return mouse.dist(lineStart) <= InfoBox.MOUSE_OVER_BORDER;
        }

        if (t > 1F) {
            return mouse.dist(lineEnd) <= InfoBox.MOUSE_OVER_BORDER;
        }

        projection = PVector.add(lineStart, PVector.mult(temp, t));

        return mouse.dist(projection) <= InfoBox.MOUSE_OVER_BORDER;

		/*
         * this.projection = null;
		 * 
		 * lineStart = new PVector(this.x1, this.y1); lineEnd = new
		 * PVector(this.x2, this.y2); mouse = new PVector(mouseX, mouseY);
		 * 
		 * float lineLength = lineStart.dist(lineEnd);
		 * 
		 * if (lineLength == 0F) { return mouse.dist(lineStart) <
		 * InfoBox.MOUSE_OVER_BORDER; }
		 * 
		 * float t = PVector.dot(PVector.sub(mouse, lineStart),
		 * PVector.sub(lineEnd, lineStart)) / lineLength;
		 * 
		 * if (t < 0F) { PApplet.println("lineStart"); return
		 * mouse.dist(lineStart) < InfoBox.MOUSE_OVER_BORDER; }
		 * 
		 * if (t > lineLength) { PApplet.println("lineEnd"); return
		 * mouse.dist(lineEnd) < InfoBox.MOUSE_OVER_BORDER; }
		 * 
		 * this.projection = PVector.add(lineStart,
		 * PVector.mult(PVector.sub(lineEnd, lineStart), t));
		 * 
		 * return mouse.dist(this.projection) < InfoBox.MOUSE_OVER_BORDER;
		 */
    }

    @Override
    public String getInfo() {
        return this.info;
    }

}
