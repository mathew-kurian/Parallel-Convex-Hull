package com.computation.common.concurrent.search;

import com.computation.common.Console;
import com.computation.common.Reference;
import com.computation.common.concurrent.Forkable;

import java.util.Collection;
import java.util.concurrent.ExecutorService;

/**
 * Created by mwkurian on 11/19/2014.
 */

public abstract class ForkableSearch<T, O extends Collection<T>> extends Forkable<T, O> {

    private final static Console console = Console.getInstance(ForkableSearch.class);

    protected ForkableSearch(ExecutorService executorService, int availableThreads, O data) {
        super(executorService, availableThreads, data);
    }

    @Override
    protected final Reference<T> onFind() {

        int size = data.size();
        int offset = size / numThreads;

        Reference<T> ref = getReferenceInstance();
        Reference<Integer> threadCount = new Reference<Integer>(numThreads);

        if (numThreads == 0) {
            // Execute directly on main thread
            new Subset(0, size,
                    ref, threadCount).run();
        } else {
            for (int i = 0; i < size; i += offset) {
                executorService.execute(new Subset(i, Math.min(i + offset, size), ref, threadCount));
            }
        }

        synchronized (threadCount) {
            if (threadCount.get() > 0) {
                try {
                    threadCount.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        return ref;
    }

    protected abstract Reference<T> getReferenceInstance();

    protected abstract void onSearch(int start, int end, Reference<T> ref);

    private class Subset implements Runnable {
        private final int start;
        private final int end;
        private final Reference<T> ref;
        private final Reference<Integer> threadCount;

        public Subset(int start, int end, Reference<T> ref,
                      Reference<Integer> threadCount) {
            this.start = start;
            this.end = end;
            this.ref = ref;
            this.threadCount = threadCount;
        }

        @Override
        public void run() {

            onSearch(start, end, ref);

            synchronized (threadCount) {
                int currThreadCount = threadCount.get() - 1;
                threadCount.update(currThreadCount);

                if (currThreadCount <= 0) {
                    threadCount.notify();
                }
            }
        }
    }
}
