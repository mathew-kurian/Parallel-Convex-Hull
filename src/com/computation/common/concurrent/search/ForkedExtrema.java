package com.computation.common.concurrent.search;

import com.computation.common.Console;
import com.computation.common.Point2D;
import com.computation.common.Utils;

import java.util.List;
import java.util.concurrent.ExecutorService;

public class ForkedExtrema extends ForkableSearch<Point2D, List<Point2D>> {

    /**
     *
     * @param executorService
     * @param numThreads
     * @param data
     * @param dir
     * @param rad
     * @return
     */
    public static Reference find(ExecutorService executorService, int numThreads, List<Point2D> data,
                                                                    Utils.Direction dir, double rad) {
        return (Reference) new ForkedExtrema(executorService, numThreads, data, dir, rad).find();
    }

    /**
     * Implementation
     */
    public class Reference extends com.computation.common.Reference<Point2D> {

        private volatile int index = -1;

        public Reference(Point2D point2D) {
            super(point2D);
        }

        private void setIndex(int index) {
            this.index = index;
        }

        public int getIndex() {
            return index;
        }
    }

    private static Console console = Console.getInstance(ForkedExtrema.class);
    private Utils.Direction dir;
    private double rad;

    protected ForkedExtrema(ExecutorService executorService, int numThreads, List<Point2D> data,
                            Utils.Direction dir, double rad) {
        super(executorService, numThreads, data);
        this.dir = dir;
        this.rad = rad;
    }

    @Override
    protected com.computation.common.Reference getReferenceInstance() {
        return new Reference(null);
    }

    @Override
    protected void onSearch(int start, int end, com.computation.common.Reference ref) {
        Point2D max = null;
        int maxIndex = -1;

        double maxY = Integer.MIN_VALUE;
        double rad = this.rad + dir.getRadianOffset();

        for (int i = start; i < end; i++) {
            Point2D point = data.get(i);
            double y = Utils.rotateY(point, rad);
            if (y > maxY) {
                max = point;
                maxY = y;
                maxIndex = i;
            }
        }

        if (max == null) {
            console.err("Max is empty. List empty?");
            return;
        }

        Reference exRef = (Reference) ref;

        synchronized (exRef) {
            Point2D lastPoint = exRef.get();

            if (lastPoint == null ||
                    Utils.rotateY(lastPoint, rad) < maxY) {
                exRef.update(max);
                exRef.setIndex(maxIndex);
            }
        }
    }
}
