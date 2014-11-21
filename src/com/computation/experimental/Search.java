package com.computation.experimental;

import java.util.List;

/**
 * Created by mwkurian on 11/17/2014.
 */
public class Search<T extends Comparable<T>> {
    protected List<T> data;
    protected T tmin;
    protected int threadCount;
    protected long startTime = 0;
    protected double elapsedTime = 0;

    public Search(List<T> data, T tmin, int threadCount) {
        this.data = data;
        this.tmin = tmin;
        this.threadCount = threadCount;
    }

    public void start() {
        startTime = System.nanoTime();
    }

    protected double elapsed() {
        return elapsedTime;
    }

    public void found(T t) {
        elapsedTime = ((double) (System.nanoTime() - startTime)) / 1000000000.0;
//        System.out.printf("(size: %d)(availableThreads: %d)(time: %f)(value: %s)\n",
//                data.size(), availableThreads, elapsedTime, t.toString());

        // System.out.println("Termination - Found in " + elapsedTime + "s");
    }
}
