package com.computation.experimental;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by mwkurian on 11/17/2014.
 */
public abstract class ConcurrentSearch<T extends Comparable<T>> extends Search<T> {

    private ExecutorService executorService;

    public ConcurrentSearch(List<T> data, T tmin, int threadCount) {
        super(data, tmin, threadCount);
    }

    @Override
    public void start() {

        int threadCount = Math.min(this.threadCount, data.size());
        int offset = data.size() / threadCount;
        int size = data.size();

        this.executorService = Executors.newFixedThreadPool(threadCount);
        T max = tmin;

        super.start();

        Reference maxRef = new Reference(max);
        AtomicInteger atomicCount = new AtomicInteger(threadCount);

        for (int i = 0; i < size; i += offset) {
            executorService.execute(new Subset(this, i, Math.min(i + offset, size),
                    maxRef, atomicCount));
        }
    }

    private class Reference {
        public volatile T t;

        public Reference(T t) {
            this.t = t;
        }
    }

    private class Subset implements Runnable {
        private final int start;
        private final int end;
        private final Reference maxRef;
        private final Search search;
        private final AtomicInteger atomicCount;

        public Subset(Search search, int start, int end, Reference maxRef,
                      AtomicInteger atomicCount) {
            this.start = start;
            this.end = end;
            this.maxRef = maxRef;
            this.search = search;
            this.atomicCount = atomicCount;
        }

        @Override
        public void run() {

            T subsetMax = tmin;

            for (int i = start; i < end; i++) {
                T d = data.get(i);
                if (d.compareTo(subsetMax) > 0) {
                    subsetMax = d;
                }
            }

            synchronized (maxRef) {
                if (subsetMax.compareTo(maxRef.t) > 0) {
                    maxRef.t = subsetMax;
                }
            }

            if (atomicCount.decrementAndGet() == 0) {
                executorService.shutdown();
                search.found(maxRef.t);
            }
        }
    }
}
