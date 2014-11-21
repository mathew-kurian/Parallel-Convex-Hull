package com.computation.algo;

import com.computation.common.*;
import com.computation.common.concurrent.Forkable;
import com.computation.common.concurrent.search.ForkedMaxAngle;
import com.computation.common.concurrent.search.ForkedExtrema;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by kgowru on 11/12/14.
 */
public class GiftWrapping extends ConvexHull {

    private static final Console console = Console.getInstance(GiftWrapping.class);
    private ExecutorService executorService;
    private volatile int searchCount;
    private Reference<Integer> threadCount;
    private Lock angleFindLock;

    private final Object lock = new Object();

    public GiftWrapping(Point2DCloud pointCloud, int threads) {
        super(pointCloud, threads);
    }

    public GiftWrapping(Point2DCloud pointCloud, int threads, boolean debug) {
        super(pointCloud, threads, debug);
    }

    public GiftWrapping(Point2DCloud pointCloud, int threads, boolean debug, int animationDelay) {
        super(pointCloud, threads, debug, animationDelay);
    }

    @Override
    protected void findHull() {

        // Set all the global variables you need
        this.executorService = Executors.newFixedThreadPool(threads);

        // Set thread pool field in JPanel
        this.pointCloud.setField("ThreadPool", true);

        // Set search count
        this.searchCount = 0;

        // Thread count
        this.threadCount = new Reference<Integer>(threads);

        // Angle between
        this.angleFindLock = new ReentrantLock();

        double radOffset = (Math.PI / 2) / (threads - 3);
        int index = 0;
        int rad = 0;
        Utils.Direction dir = Utils.Direction.NORTH;
        List<Integer> extremas = new ArrayList<Integer>();

        for (; index < threads;) {

            extremas.add(ForkedExtrema.find(executorService, threads, points,
                    dir, rad).getIndex());

            int dirr = ++index % 4;

            switch (dirr) {
                case 1: {
                    dir = Utils.Direction.SOUTH;
                    break;
                }
                case 2: {
                    dir = Utils.Direction.EAST;
                    break;
                }
                case 3: {
                    dir = Utils.Direction.WEST;
                    break;
                }
                case 0: {
                    rad += radOffset;
                    dir = Utils.Direction.NORTH;
                    break;
                }
            }
        }

        int paletteIndex = 0;
        int maxSubsetCount = extremas.size();
        Color[] palette = Utils.getColorPalette(maxSubsetCount);

        pointCloud.setField("Wrap Threads", maxSubsetCount);
        pointCloud.setField("Search Threads", 0);

        for (int pointIndex : extremas) {
            Point2D point = points.get(pointIndex);
            if (point.getColor() != Point2D.VISITED) {
                point.setColor(Point2D.VISITED);
                executorService.execute(new Subset(pointIndex, palette[paletteIndex++]));
            } else {

                int currThreadCount = 0;

                synchronized (threadCount) {
                    currThreadCount = threadCount.get() - 1;
                    threadCount.update(currThreadCount);
                }

                if (debug) {
                    console.log("Adding search thread");
                }

                // Increase search active
                searchCount++;

                // Update availableThreads count
                pointCloud.setField("Wrap Threads", currThreadCount);
                pointCloud.setField("Search Threads", searchCount);
            }
        }

        pointCloud.draw();

        synchronized (threadCount) {
            if (threadCount.get() > 0) {
                try {
                    threadCount.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        executorService.shutdownNow();
    }

    private class Subset implements Runnable {

        private Color color;
        private int edge;
        private boolean firstSearch;

        public Subset(int edge, Color color) {
            this.edge = edge;
            this.color = color;
            this.firstSearch = true;
        }

        @Override
        public void run() {

            int pivPointIndex = edge;
            int refPointIndex;
            int lastPivPointIndex = 0;
            boolean performLinear;
            Point2D pivPoint, refPoint = null;

            do {

                requestAnimationFrame();

                performLinear = true;
                pivPoint = points.get(pivPointIndex);

                /**
                 * Mark the point as visited; if we see this again in
                 * another thread, that thread knows they are done
                 */
                pivPoint.setColor(Point2D.VISITED);

                //search for q such that it is ccwQuant for all other i
                refPointIndex = (pivPointIndex + 1) % pointCount;

                if (!firstSearch && searchCount > 1) {

                    if (angleFindLock.tryLock()) {

                        try {
                            if (debug) {
                                console.log("Performing concurrent search");
                            }

                            // Get next
                            refPointIndex = ForkedMaxAngle.find(executorService, searchCount, points, pivPointIndex, lastPivPointIndex).getIndex();
                            refPoint = points.get(refPointIndex);

                            // Skip linear
                            performLinear = false;

                        } finally {
                            angleFindLock.unlock();
                        }
                    }
                }

                if (performLinear) {
                    if (debug) {
                        console.log("Performing linear search");
                    }

                    pivPoint = points.get(pivPointIndex);
                    refPoint = points.get(refPointIndex);

                    for (int currPointIndex = 0; currPointIndex < pointCount; currPointIndex++) {
                        Point2D currPoint = points.get(currPointIndex);
                        if (Utils.ccw(pivPoint, currPoint, refPoint) == 2) {
                            refPoint = currPoint;
                            refPointIndex = currPointIndex;
                        }
                    }

                    firstSearch = false;
                }

                // Add edge
                pointCloud.addEdge(new Edge(pivPoint, refPoint, color));

                // Wait a while so you can see it
                delay();

                // Last pivot
                lastPivPointIndex = pivPointIndex;

                // Start from q next time
                pivPointIndex = refPointIndex;

                releaseAnimationFrame();
            }
            while (points.get(pivPointIndex).getColor() == Point2D.UNVISITED);

            int currThreadCount = 0;

            synchronized (threadCount) {
                currThreadCount = threadCount.get() - 1;
                threadCount.update(currThreadCount);
                if (currThreadCount == 0) {
                    threadCount.notify();
                }
            }

            // Update availableThreads count
            if(debug) {
                pointCloud.setField("Wrap Threads", currThreadCount);
                pointCloud.setField("Search Threads", searchCount + 1);
            }

            searchCount++;
        }
    }
}
