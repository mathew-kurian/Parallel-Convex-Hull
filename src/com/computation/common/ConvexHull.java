package com.computation.common;

import java.util.List;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public abstract class ConvexHull implements Runnable {

    private static final Console console = Console.getInstance(ConvexHull.class);
    protected final Point2DCloud pointCloud;
    protected final int pointCount;
    protected final int threads;
    protected final List<Point2D> points;
    protected boolean debug = true;
    protected boolean debugStepThrough = false;
    protected long debugFrameDelay = 1000;
    protected Lock debugStep;
    protected Condition debugStepCondition;
    private boolean active = false;
    private long startTime;

    public ConvexHull(Point2DCloud pointCloud, int threads) {
        this(pointCloud, threads, true);
    }

    public ConvexHull(Point2DCloud pointCloud, int threads, boolean debug) {
        this(pointCloud, threads, debug, 1000);
    }

    public ConvexHull(Point2DCloud pointCloud, int threads, final boolean debug, int animationDelay) {
        this.pointCloud = pointCloud;
        this.threads = threads;
        this.debug = debug;
        this.debugStepThrough = animationDelay == Integer.MAX_VALUE;
        this.debugFrameDelay = debugStepThrough ? 0 : animationDelay;
        this.points = this.pointCloud.getPoints();
        this.pointCount = points.size();
        this.debugStep = new ReentrantLock();
        this.debugStepCondition = debugStep.newCondition();

        // Get algo name
        String algo = ConvexHull.this.getClass().getSimpleName();

        // Add a button
        pointCloud.addButton("Start", this);
        //pointCloud.addButton("Populate", this);

        pointCloud.addButton("Reset", new Runnable() {
            @Override
            public void run() {
                if(!active) {
                    for (Point2D point : ConvexHull.this.pointCloud.getPoints()) {
                        point.setColor(Point2D.UNVISITED);
                    }

                    ConvexHull.this.pointCloud.removeAllEdges();
                    ConvexHull.this.pointCloud.enableButton("Start", true);
                    ConvexHull.this.pointCloud.draw();
                }
            }
        });

        pointCloud.addButton("DebugStepThrough", new Runnable() {
            @Override
            public void run() {
                ConvexHull.this.debug = true;
                ConvexHull.this.debugStepThrough = !ConvexHull.this.debugStepThrough;
                ConvexHull.this.pointCloud.toggleButton("DebugStepThrough", debugStepThrough);

                // Add debug button
                if (debugStepThrough) {
                    ConvexHull.this.pointCloud.addButton("Step", new Runnable() {
                        @Override
                        public void run() {
                            debugStep.lock();
                            debugStepCondition.signal();
                            debugStep.unlock();
                        }
                    });

                    ConvexHull.this.pointCloud.enableButton("Step", true);

                } else {
                    ConvexHull.this.pointCloud.enableButton("Step", false);
                }
            }
        });

        if (debugStepThrough) {
            ConvexHull.this.pointCloud.addButton("Step", new Runnable() {
                @Override
                public void run() {
                    debugStep.lock();
                    debugStepCondition.signal();
                    debugStep.unlock();
                }
            });

            ConvexHull.this.pointCloud.enableButton("Step", true);
        }

        pointCloud.toggleButton("DebugStepThrough", debugStepThrough);

        // Set basic information
        pointCloud.setName(algo);
        pointCloud.setField("Algorithm", algo);
        pointCloud.setField("Points", pointCloud.getPoints().size());
        pointCloud.setField("Threads", threads);
        pointCloud.setField("Debug", debug);
        pointCloud.setField("Frame Delay (ms)", debugFrameDelay);
    }

    @Override
    public void run() {
        if (!active) {
            active = true;
            pointCloud.enableButton("Start", false);
            pointCloud.enableButton("Reset", false);
            pointCloud.enableButton("DebugStepThrough", false);
            pointCloud.enableButton("Step", debugStepThrough);
            startTime = System.nanoTime();
            findHull();
            double duration = ((double) (System.nanoTime() - startTime)) / 1000000000.0;
            duration = Math.round(duration * 10000.0) / 10000.0;
            pointCloud.setField("Duration (s)", duration);
            pointCloud.toast("Completed!");
            pointCloud.enableButton("Start", false);
            pointCloud.enableButton("Reset", true);
            pointCloud.enableButton("DebugStepThrough", true);
            pointCloud.enableButton("Step", false);
            active = false;
        }
    }

    protected void delay() {
        if (debug) {
            pointCloud.draw();
            try {
                Thread.sleep(debugFrameDelay);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    protected void requestAnimationFrame() {
        if (debugStepThrough) {
            debugStep.lock();
            try {
                debugStepCondition.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    protected void releaseAnimationFrame() {
        if (debugStepThrough) {
            console.err("Released lock");
            debugStep.unlock();
        }
    }

    protected abstract void findHull();
}
