package com.computation.algo;

import com.computation.common.*;

import java.util.Collections;
import java.util.Comparator;
import java.util.Stack;

@SuppressWarnings("unused")
public class GrahamScan extends ConvexHull {
    public GrahamScan(Point2DCloud pointCloud, int threads,
                      boolean debug, int animationDelay) {
        super(pointCloud, threads, debug, animationDelay);
    }

    @Override
    protected void findHull() {

        Stack<Point2D> stack = new Stack<Point2D>();
        pointCloud.setField("ThreadPool", true);

        // preprocess so that points[0] has lowest y-coordinate; break ties by
        // x-coordinate
        // points[0] is an extreme point of the convex hull
        // (alternatively, could do easily in linear time)

        Collections.sort(points, new Comparator<Point2D>() {
            public int compare(Point2D p1, Point2D p2) {
                if (p2.y == p1.y) {
                    return p1.x - p2.x;
                }

                return p2.y - p1.y;
            }
        });

        final Point2D firstPoint = points.get(0);

        stack.push(firstPoint);

        points.remove(0);

        // sort by polar angle with respect to base point points[0],
        // breaking ties by distance to points[0]
        Collections.sort(points, new Comparator<Point2D>() {
            public int compare(Point2D q1, Point2D q2) {

                double dx1 = q1.x - firstPoint.x;
                double dy1 = q1.y - firstPoint.y;
                double dx2 = q2.x - firstPoint.x;
                double dy2 = q2.y - firstPoint.y;

                if (dy1 >= 0 && dy2 < 0) {
                    return -1; // q1 above; q2 below
                } else if (dy2 >= 0 && dy1 < 0) {
                    return +1; // q1 below; q2 above
                }
                // 3-collinear and horizontal
                else if (dy1 == 0 && dy2 == 0) {
                    if (dx1 >= 0 && dx2 < 0) {
                        return -1;
                    } else if (dx2 >= 0 && dx1 < 0) {
                        return 1;
                    } else {
                        return 0;
                    }
                }
                // both above or below
                else {
                    // Note: ccwQuant() recomputes dx1, dy1, dx2, and dy2
                    return -Utils.ccwQuant(firstPoint, q1, q2);
                }
            }
        });

        stack.push(points.get(0)); // p[0] is first extreme point
        stack.push(points.get(1));

        int k1;
        for (k1 = 1; k1 < points.size(); k1++) {
            if (!points.get(0).equals(points.get(k1))) {
                break;
            }
        }

        // all points equal
        if (k1 == points.size()) {
            return;
        }

        // find index k2 of first point not collinear with points[0] and points[k1]
        int k2;
        for (k2 = k1 + 1; k2 < points.size(); k2++) {
            if (Utils.ccwQuant(points.get(0), points.get(k1), points.get(k2)) != 0) {
                break;
            }
        }

        stack.push(points.get(k2 - 1));    // points[k2-1] is second extreme point

        // Graham scan; note that points[N-1] is extreme point different from points[0]
        for (int i = k2; i < points.size(); i++) {
            Point2D top = stack.pop();

            while (Utils.ccwQuant(stack.peek(), top, points.get(i)) <= 0) {
                top = stack.pop();
            }

            stack.push(top);
            stack.push(points.get(i));
        }

        while (stack.size() != 1) {
            requestAnimationFrame();
            delay();

            Point2D a = stack.pop();
            Point2D b = stack.pop();

            a.setColor(Point2D.VISITED);
            b.setColor(Point2D.VISITED);

            pointCloud.addEdge(new Edge(a, b));
            stack.push(b);
            releaseAnimationFrame();
        }

        pointCloud.addEdge(new Edge(firstPoint, points.get(points.size() - 1)));
    }
}

