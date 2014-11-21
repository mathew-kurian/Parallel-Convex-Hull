package com.computation.common;

import java.awt.*;

public class Edge {

    public final Point2D p1;
    public final Point2D p2;
    public final Color color;

    public Edge(Point2D p1, Point2D p2) {
        this(p1, p2, Color.RED);
    }

    public Edge(Edge edge) {
        this(edge.p1, edge.p2, edge.color);
    }

    public Edge(Point2D p1, Point2D p2, Color color) {
        this.p1 = p1;
        this.p2 = p2;
        this.color = color;
    }


    @Override
    public int hashCode() {
        return new Integer(p1.hashCode() + p2.hashCode()).hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }

        if (obj instanceof Edge) {
            Edge edge = (Edge) obj;
            return (edge.p1.equals(p2) && edge.p2.equals(p1)) || (edge.p1.equals(p1) && edge.p2.equals(p2));
        }

        return false;
    }

    public Color getColor() {
        return this.color;
    }
}
