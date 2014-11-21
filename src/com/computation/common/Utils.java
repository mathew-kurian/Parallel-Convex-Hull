package com.computation.common;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Utils {

    public static List<Point2D> generateRandomPoints(int length, int width, int height, int inset) {
        Random rand = new Random();
        List<Point2D> point2Ds = new ArrayList<Point2D>();

        for (int i = 0; i < length; i++) {
            point2Ds.add(new Point2D(rand.nextInt(width - inset * 2) + inset, rand.nextInt(height - inset * 2) + inset));
        }

        return point2Ds;
    }

    public static Point2D findMax(List<Point2D> point2Ds, Direction dir, double rads) {
        return findMax(point2Ds, 0, point2Ds.size(), dir, rads);
    }

    public static Point2D findMax(List<Point2D> point2Ds, int start, int end, Direction dir, double rads) {

        Point2D p = null;
        double maxY = Integer.MIN_VALUE;
        rads += dir.getRadianOffset();

        for (int i = start; i < end; i++) {
            Point2D point2D = point2Ds.get(i);
            double y = rotateY(point2D, rads);
            if (y > maxY) {
                p = point2D;
                maxY = y;
            }
        }

        return p;
    }

    public static double rotateY(Point2D point2D, double rads) {
        return -Math.sin(rads) * point2D.x + Math.cos(rads) * point2D.y;
    }

    public static double rotateX(Point2D point2D, double rads) {
        return Math.cos(rads) * point2D.x + Math.sin(rads) * point2D.y;
    }

    public static boolean isPointLeftOf(Point2D line1, Point2D line2, Point2D point2D) {
        return (((line2.x - line1.x) * (point2D.y - line1.y) - (line2.y - line1.y) * (point2D.x - line1.x)) > 0);
    }

    public static double angleBetween(Point2D center, Point2D current, Point2D previous) {
        Double degrees = Math.toDegrees(Math.atan2(current.x - center.x, current.y - center.y) -
                Math.atan2(previous.x - center.x, previous.y - center.y));
        //System.out.println(Math.abs(degrees));
        if (Math.abs(degrees) > 180) {
            return 360 - Math.abs(degrees);
        } else {
            return Math.abs(degrees);
        }
    }

    public static int distance(Point2D line1, Point2D line2, Point2D p) {
        int ABx = line2.x - line1.x;
        int ABy = line2.y - line1.y;
        int num = ABx * (line1.y - p.y) - ABy * (line1.x - p.x);
        if (num < 0) num = -num;
        return num;
    }

    //finds orientation of triplet (p,q,r)
    //true --> p-r is counterclockwise from p-q
    //false --> if not
    public static int ccw(Point2D p, Point2D q, Point2D r) {
        int val = (q.y - p.y) * (r.x - q.x) -
                (q.x - p.x) * (r.y - q.y);

        if (val == 0) return 0;  // colinear
        return (val > 0) ? 1 : 2; // clock or counterclock wise
    }


    public static int ccwQuant(Point2D p, Point2D q, Point2D r) {
        int val = (q.y - p.y) * (r.x - q.x) -
                (q.x - p.x) * (r.y - q.y);

        return val;
    }

    public static Color[] getColorPalette(int n) {
        Color[] cols = new Color[n];
        for (int i = 0; i < n; i++) {
            cols[i] = Color.getHSBColor((float) i / (float) n, 0.85f, 1.0f);
        }
        return cols;
    }

    public static enum Direction {
        NORTH(Math.PI), SOUTH(0), EAST(Math.PI * 3 / 2), WEST(Math.PI / 2);

        private final double rads;

        Direction(double rads) {
            this.rads = rads;
        }

        public double getRadianOffset() {
            return rads;
        }
    }
}
