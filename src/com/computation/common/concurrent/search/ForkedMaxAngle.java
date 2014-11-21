package com.computation.common.concurrent.search;

import com.computation.common.Point2D;
import com.computation.common.Reference;
import com.computation.common.Utils;

import java.util.List;
import java.util.concurrent.ExecutorService;

/**
 * Created by mwkurian on 11/19/2014.
 */
public class ForkedMaxAngle extends ForkableSearch<Point2D, List<Point2D>> {

    /**
     *
     * @param executorService
     * @param availableThreads
     * @param data
     * @param pivot
     * @param next
     * @return
     */
    public static Reference find(ExecutorService executorService, int availableThreads, List<Point2D> data, int pivot, int next){
        return (Reference) new ForkedMaxAngle(executorService, availableThreads, data, pivot, next).find();
    }

    /**
     * Implementation
     */

    private final int pivot;
    private final int next;

    protected ForkedMaxAngle(ExecutorService executorService, int availableThreads, List<Point2D> data, int pivot, int next) {
        super(executorService, availableThreads, data);
        this.pivot = pivot;
        this.next = next;
    }

    @Override
    protected com.computation.common.Reference getReferenceInstance() {
        Reference cf = new Reference(null);
        cf.setIndex(Integer.MIN_VALUE);
        return cf;
    }

    @Override
    protected void onSearch(int start, int end, com.computation.common.Reference ref) {
        Reference exRef = (Reference) ref;
        int q = Integer.MIN_VALUE;
        double maxAngle = Double.MIN_VALUE;

        Point2D pivPoint = data.get(pivot);
        Point2D nexPoint = data.get(next);

        for (int i = start; i < end; i++) {
            Point2D currPoint = data.get(i);
            if (nexPoint.equals(currPoint) ||
                    pivPoint.equals(currPoint)) continue;
            double pot = Utils.angleBetween(pivPoint, nexPoint, currPoint);
            if (pot > maxAngle) {
                maxAngle = pot;
                q = i;
            }
        }

        synchronized (exRef) {
            // System.out.println("Thread: " + Thread.currentThread().getId() + "maxAngle: " + maxAngle + " > " + exRef.getAngle());
            if (maxAngle > exRef.getAngle()) {
                exRef.setAngle(maxAngle);
                exRef.setIndex(q);
            }
        }
    }

    public class Reference extends com.computation.common.Reference<Point2D> {

        private volatile int index = -1;

        public double getAngle() {
            return angle;
        }

        public void setAngle(double angle) {
            this.angle = angle;
        }

        private volatile double angle = Double.MIN_VALUE;

        public Reference(Point2D point2D) {
            super(point2D);
        }

        public int getIndex() {
            return index;
        }

        private void setIndex(int index) {
            this.index = index;
        }
    }
}
