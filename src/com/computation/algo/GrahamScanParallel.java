package com.computation.algo;

import com.computation.common.*;
import com.computation.common.concurrent.search.ForkedMaxBottomLeft;
import com.computation.external.HeavySort;

import java.util.Comparator;
import java.util.Stack;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@SuppressWarnings("unused")
public class GrahamScanParallel extends ConvexHull {

    private static final HeavySort.ArrayFactory<Point2D> arrayFactory = new HeavySort.ArrayFactory<Point2D>() {
        @Override
        public Point2D[] buildArray(int length) {
            return new Point2D[length];
        }
    };

    public GrahamScanParallel(Point2DCloud pointCloud, int threads,
                              boolean debug, int animationDelay) {
        super(pointCloud, threads, debug, animationDelay);
    }

    @Override
    protected void findHull() {
        ExecutorService executorService = Executors.newFixedThreadPool(threads);
        Stack<Point2D> stack = new Stack<Point2D>();
        pointCloud.setField("ThreadPool", true);

        ForkedMaxBottomLeft.Reference ref = ForkedMaxBottomLeft.find(executorService, threads, points);
        final Point2D firstPoint = ref.get();

        stack.push(ref.get());
        points.remove(ref.getIndex());

        Point2D[] pointArr = points.toArray(new Point2D[0]);

        // sort by polar angle with respect to base point points[0],
        // breaking ties by distance to points[0]
        HeavySort.sort(pointArr, executorService, threads, arrayFactory, new Comparator<Point2D>() {

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

        stack.push(pointArr[0]); // p[0] is first extreme point
        stack.push(pointArr[1]);

        Point2D k_point = pointArr[0];
        Point2D k1_point = pointArr[1];

        // find index k2 of first point not collinear with points[0] and points[k1]
        int k2_index;
        for (k2_index = 2; k2_index < pointArr.length; k2_index++) {
            Point2D k2 = pointArr[k2_index];
            if (Utils.ccwQuant(k_point, k1_point, k2) != 0) {
                break;
            }
        }

        // points[k2-1] is second extreme point
        stack.push(pointArr[k2_index - 1]);

        // Graham scan; note that points[N-1] is extreme point different from points[0]
        for (int i = k2_index; i < pointArr.length; i++) {
            Point2D top = stack.pop();

            while (Utils.ccwQuant(stack.peek(), top, pointArr[i]) <= 0) {
                top = stack.pop();
            }

            stack.push(top);
            stack.push(pointArr[i]);
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

        pointCloud.addEdge(new Edge(firstPoint, pointArr[pointArr.length - 1]));

        executorService.shutdownNow();
    }
}
