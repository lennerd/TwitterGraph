package com.lennerd.processing.twitter_graph;

import com.lennerd.processing.twitter_graph.twitter.MyStatus;
import processing.core.PApplet;

import java.io.Serializable;

public abstract class SkyObject implements Serializable {

    private static final long serialVersionUID = 1L;
    private boolean calculated = false;
    private boolean focus = false;
    private boolean remove = false;
    private boolean removed = false;
    protected StatusColor color;

    public void draw(PApplet sketch) {
        if (!this.calculated) {
            this.onAdd(sketch);

            this.calculated = true;
        } else if (this.remove) {
            this.onRemove(sketch);

            this.remove = false;
        } else if (this.focus) {
            //PApplet.println("--- Focus ... " + this.getClass());
            this.onFocus(sketch);

            this.focus = false;
        }

        this.onDraw(sketch);
    }

    public void remove() {
        this.remove = true;
    }

    public abstract void onDraw(PApplet sketch);

    public abstract void onAdd(PApplet sketch);

    public abstract void onRemove(PApplet sketch);

    public abstract void onFocus(PApplet sketch);

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
        this.calculated = false;
    }

    public abstract boolean isMouseOver(int mouseX, int mouseY);

    public abstract String getInfo();

    public StatusColor getColor() {
        if (this.color == null) {
            return new StatusColor(0);
        }

        return this.color;
    }
}
