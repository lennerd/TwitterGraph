package com.lennerd.processing.twitter_graph;

import com.lennerd.processing.twitter_graph.twitter.MyStatus;
import de.looksgood.ani.Ani;
import processing.core.PApplet;
import processing.core.PGraphics;

import java.io.Serializable;
import java.util.HashSet;

public abstract class SkyObject implements Serializable {

    private static final long serialVersionUID = 1L;
    private boolean focus = false;
    private boolean remove = false;
    private boolean removed = false;
    private PGraphics graphics;
    private float sketchX, sketchY;
    private int imageMode;
    protected StatusColor color;
    protected HashSet<Ani> anis;

    protected SkyObject() {
        this.anis = new HashSet<Ani>();
    }

    public void draw(PApplet sketch) {
        if (this.graphics == null) {
            this.onAdd(sketch);

            this.graphics = sketch.createGraphics(this.getWidth(), this.getHeight());
            this.sketchX = this.getSketchX();
            this.sketchY = this.getSketchY();
            this.imageMode = this.getImageMode();
        }

        if (this.remove) {
            this.onRemove(sketch);

            this.remove = false;
        } else if (this.focus) {
            this.onFocus(sketch);

            this.focus = false;
        }

        if (this.isAnimating()) {
            this.graphics.beginDraw();
            this.graphics.background(255, 0);
            this.drawGraphics(this.graphics);
            this.graphics.endDraw();
        }

        sketch.imageMode(this.imageMode);
        sketch.image(this.graphics, this.sketchX, this.sketchY);
    }

    public void remove() {
        this.remove = true;
    }

    public abstract void drawGraphics(PGraphics graphics);

    public abstract void onAdd(PApplet sketch);

    public abstract void onRemove(PApplet sketch);

    public abstract void onFocus(PApplet sketch);

    public abstract float getSketchX();

    public abstract float getSketchY();

    public abstract int getWidth();

    public abstract int getHeight();

    public int getImageMode() {
        return PApplet.CENTER;
    }

    public void focus() {
        this.focus = true;
    }

    public void onRemoved() {
        this.removed = true;
    }

    public boolean isRemoved() {
        return this.removed;
    }

    public abstract void addStatus(MyStatus status);

    public void reset() {
        this.graphics = null;
    }

    public abstract boolean isMouseOver(int mouseX, int mouseY);

    public abstract String getInfo();

    public boolean isAnimating() {
        return true;
    }

    public StatusColor getColor() {
        if (this.color == null) {
            return new StatusColor(0);
        }

        return this.color;
    }
}
