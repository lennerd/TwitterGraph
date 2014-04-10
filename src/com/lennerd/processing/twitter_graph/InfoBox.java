package com.lennerd.processing.twitter_graph;

import de.looksgood.ani.Ani;
import processing.core.PApplet;
import processing.core.PFont;

import java.util.ArrayList;
import java.util.List;

public final class InfoBox {

    public static final float MOUSE_OVER_BORDER = 3F;
    public static final int HEIGHT = 30;

    private final Sky sky;
    private final PApplet sketch;
    private final List<Info> infos;
    private final int top, center;
    private final PFont font;

    public InfoBox(PApplet sketch, Sky sky, PFont font) {
        this.sketch = sketch;
        this.sky = sky;

        this.infos = new ArrayList<Info>();

        this.font = font;

        this.top = sketch.height - InfoBox.HEIGHT;
        this.center = InfoBox.HEIGHT / 2;
    }

    public void mouseMoved(int mouseX, int mouseY) {
        Info newInfo = null;

        for (SkyObject drawable : sky.getDrawables()) {
            if (drawable.isMouseOver(mouseX, mouseY)) {
                newInfo = new Info(drawable);
                int index = this.infos.indexOf(newInfo);

                if (index < 0) {
                    this.infos.add(newInfo);
                } else {
                    newInfo = this.infos.get(index);
                }

                newInfo.add();

                break;
            }
        }

        for (Info info : this.infos) {
            if (!info.equals(newInfo)) {
                info.remove();
            }
        }
    }

    public void draw() {
        this.sketch.pushMatrix();
        this.sketch.pushStyle();

        this.sketch.translate(0, this.top);

        this.sketch.textFont(this.font);
        this.sketch.textSize(12);
        this.sketch.textAlign(PApplet.LEFT, PApplet.CENTER);

        this.sketch.noStroke();
        this.sketch.rectMode(PApplet.CORNERS);

        for (Info info : this.infos) {
            info.draw(this.sketch);
        }

        this.sketch.popMatrix();
        this.sketch.popStyle();
    }

    private final class Info {

        private final SkyObject skyObject;
        private final String info;

        private float opacity;
        private boolean added = false;
        private boolean removed = false;
        private Ani ani;

        public Info(SkyObject skyObject) {
            this.skyObject = skyObject;
            this.opacity = 0F;

            this.info = skyObject.getInfo().replaceAll("\\n|\\r|\\t", " ").replaceAll("\\s+", " ").trim();
        }

        public void draw(PApplet sketch) {
            StatusColor color = this.skyObject.getColor();
            sketch.fill(color.red, color.green, color.blue, this.opacity);
            sketch.rect(0, 0, sketch.width, InfoBox.HEIGHT);

            sketch.fill(0, this.opacity);
            //sketch.text(this.info, 35, InfoBox.this.center - 1);
            sketch.text(this.info, 10, InfoBox.this.center - 1);
        }

        @Override
        public boolean equals(Object object) {
            if (object == null) {
                return false;
            }

            if (object == this) {
                return true;
            }

            if (object instanceof Info) {
                Info info = (Info) object;

                return info.getSkyObject().equals(this.skyObject);
            }

            return false;
        }

        public void add() {
            if (!this.added) {
                this.stopAni();
                this.ani = Ani.to(this, .7F, "opacity", 190F, Ani.EXPO_OUT);

                this.removed = false;
                this.added = true;
            }
        }

        public void remove() {
            if (!this.removed) {
                this.stopAni();
                this.ani = Ani.to(this, .7F, "opacity", 0F, Ani.EXPO_OUT, "onEnd:onRemoved");

                this.added = false;
                this.removed = true;
            }
        }

        private void stopAni() {
            if (this.ani != null) {
                this.ani.end();
            }
        }

        @SuppressWarnings("unused")
        public void onRemoved() {
            InfoBox.this.infos.remove(this);
        }

        public SkyObject getSkyObject() {
            return this.skyObject;
        }

    }

}
