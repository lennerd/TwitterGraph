package com.lennerd.processing.twitter_graph;

import processing.core.PApplet;
import processing.core.PFont;

import java.util.Calendar;
import java.util.TimeZone;

public final class TimeZones {

    private static final int MAX_MILLIS_OF_DAY = 1000 * 60 * 60 * 24;
    private static final int HEIGHT = 20;

    private final PApplet sketch;
    private final float timeZoneWidth, halfTimeZoneWidth;
    private final PFont font;
    private final float top, center;

    public TimeZones(PApplet sketch, PFont font) {
        this.sketch = sketch;

        this.timeZoneWidth = this.sketch.width / 24F;
        this.halfTimeZoneWidth = this.timeZoneWidth / 2F;
        this.font = font;

        this.center = TimeZones.HEIGHT / 2F;
        this.top = this.sketch.height / 2F + this.center;
    }

    public void draw() {
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("Etc/GMT"));
        int millisOfDay = cal.get(Calendar.MILLISECOND) + (cal.get(Calendar.SECOND) +
                ((cal.get(Calendar.MINUTE) + cal.get(Calendar.HOUR_OF_DAY) * 60) * 60) * 1000);

        float positionOfCurrentMillis = PApplet.round(PApplet.map(millisOfDay, 0, TimeZones.MAX_MILLIS_OF_DAY - 1, this.sketch.width, 1)) + this.sketch.width / 2F;

        if (positionOfCurrentMillis > this.sketch.width) {
            positionOfCurrentMillis -= this.sketch.width;
        }

        this.sketch.pushMatrix();
        this.sketch.pushStyle();

        this.sketch.translate(positionOfCurrentMillis, this.top);

        this.sketch.stroke(230);
        this.sketch.fill(190);
        this.sketch.textFont(this.font);
        this.sketch.textSize(12);
        this.sketch.textAlign(PApplet.CENTER, PApplet.CENTER);

        float currentX = positionOfCurrentMillis;

        for (int hour = 0; hour < 24; hour++) {
            this.sketch.text(hour, this.halfTimeZoneWidth, this.center);

            if (currentX + this.timeZoneWidth > this.sketch.width) {
                // Reset to the left side of the sketch to draw the remaining hours
                this.sketch.translate(this.sketch.width * -1, 0);
                currentX -= this.sketch.width;

                this.sketch.text(hour, this.halfTimeZoneWidth, this.center);
            }

            this.sketch.translate(this.timeZoneWidth, 0);
            currentX += this.timeZoneWidth;

            this.sketch.line(0, 0, 0, TimeZones.HEIGHT);
        }

        this.sketch.popMatrix();
        this.sketch.popStyle();
    }

}
