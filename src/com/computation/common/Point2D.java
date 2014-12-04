package com.computation.common;

import java.awt.*;

public class Point2D extends java.awt.Point {

    public static final Color VISITED = new Color(0x2ecc71);
    public static final Color UNVISITED = new Color(0xe74c3c);

    private volatile Color color = UNVISITED;

    public volatile String debugText = "";

    public Point2D(int a, int i) {
        super(a, i);
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }
}
