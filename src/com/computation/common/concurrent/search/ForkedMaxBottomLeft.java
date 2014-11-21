package com.computation.common.concurrent.search;

import com.computation.common.Point2D;

import java.util.List;
import java.util.concurrent.ExecutorService;

/**
 * Created by mwkurian on 11/20/2014.
 */
public class ForkedMaxBottomLeft extends ForkableSearch<Point2D, List<Point2D>> {

    /**
     * @param executorService
     * @param availableThreads
     * @param data
     * @return
     */
    public static Reference find(ExecutorService executorService, int availableThreads, List<Point2D> data) {
        return (Reference) new ForkedMaxBottomLeft(executorService, availableThreads, data).find();
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


    protected ForkedMaxBottomLeft(ExecutorService executorService, int availableThreads, List<Point2D> data) {
        super(executorService, availableThreads, data);
    }

    @Override
    protected Reference getReferenceInstance() {
        return new Reference(null);
    }

    private int comparePoints(Point2D p1, Point2D p2) {
        if (p2.y == p1.y) {
            return p1.x - p2.x;
        }

        return p2.y - p1.y;
    }

    @Override
    protected void onSearch(int start, int end, com.computation.common.Reference<Point2D> ref) {
        Point2D min = null;
        int minIndex = 0;

        for (int i = start; i < end; i++) {
            Point2D curr = data.get(i);
            if (min == null) {
                min = curr;
                minIndex = i;
                continue;
            }
            if (comparePoints(min, curr) > 0) {
                min = curr;
                minIndex = i;
            }
        }

        synchronized (ref) {
            Point2D last = ref.get();
            if (last == null) {
                ref.update(min);
                ((Reference) ref).setIndex(minIndex);
            } else if (comparePoints(last, min) > 0) {
                ref.update(min);
                ((Reference) ref).setIndex(minIndex);
            }
        }
    }
}
