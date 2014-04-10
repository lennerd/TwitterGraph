package com.lennerd.processing.twitter_graph;

import processing.core.PApplet;

import java.io.Serializable;

public final class StatusColor implements Serializable, Cloneable {

    private static final long serialVersionUID = 1L;

    public static final int MASK_RED = 255 << 16;
    public static final int MASK_GREEN = 255 << 8;
    public static final int MASK_BLUE = 255;

    public int red;
    public int green;
    public int blue;

    public StatusColor(MyStatus status) {
        this(status.getText().length());
    }

    public StatusColor(int size) {
        int color = StatusColor.calculateColor(size);

        this.red = (color & StatusColor.MASK_RED) >> 16;
        this.green = (color & StatusColor.MASK_GREEN) >> 8;
        this.blue = (color & StatusColor.MASK_BLUE);
    }

    public void extend(MyStatus status) {
        this.extend(new StatusColor(status));
    }

    public void extend(StatusColor statusColor) {
        this.red = (this.red + statusColor.red) / 2;
        this.green = (this.green + statusColor.green) / 2;
        this.blue = (this.blue + statusColor.blue) / 2;
    }

    @Override
    public StatusColor clone() {
        try {
            return (StatusColor) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new InternalError();
        }
    }

    private static int calculateColor(int size) {
        float c = PApplet.map(size, 1, 140, 16777215, 0);

        return PApplet.round(c);

		/*if (c < 1) {
			return 0xff0000 + (PApplet.round(255 * c) << 8);
		} else if (c < 2) {
			return 0xffff00 - (PApplet.round(255 * (c - 1)) << 16);
		} else if (c < 3) {
			return 0x00ff00 + PApplet.round(255 * (c - 2));
		} else if (c < 4) {
			return 0x00ffff - (PApplet.round(255 * (c - 3)) << 8);
		} else {
			return 0x0000ff + (PApplet.round(255 * (c - 4)) << 16);
		}*/
    }

}
