package com.lennerd.processing.twitter_graph.twitter;

import com.lennerd.processing.twitter_graph.Sketch;
import com.lennerd.processing.twitter_graph.SkyObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

abstract public class Entity implements Serializable {

    protected final List<SkyObject> drawables;

    private static final long serialVersionUID = Sketch.SERIALIZATION_ID;

    public Entity() {
        this.drawables = new ArrayList<SkyObject>();
    }

    public boolean addStatus(MyStatus status) {
        for (SkyObject drawable : this.drawables) {
            drawable.addStatus(status);
        }

        return true;
    }

    public boolean addDrawable(SkyObject drawable) {
        if (this.drawables.contains(drawable)) {
            return false;
        }

        this.drawables.add(drawable);

        return true;
    }

    public List<SkyObject> getDrawables() {
        return this.drawables;
    }

    public void focus() {
        if (this.drawables.size() == 0) {
            return;
        }

        for (SkyObject drawable : this.drawables) {
            drawable.focus();
        }
    }

}
