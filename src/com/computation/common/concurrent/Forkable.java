package com.computation.common.concurrent;

import com.computation.common.Console;
import com.computation.common.Reference;

import java.util.Collection;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by mwkurian on 11/20/2014.
 */
public abstract class Forkable<T, O extends Collection<T>> {

    private final static Console console = Console.getInstance(Forkable.class);
    protected final ExecutorService executorService;

    protected O data;
    protected int numThreads;
    protected Lock lock;

    public Forkable(ExecutorService executorService, int numThreads, O data) {
        this.executorService = executorService;
        this.data = data;
        this.numThreads = numThreads;
        this.lock = new ReentrantLock();
    }

    public Lock getLock() {
        return lock;
    }

    public void setNumThreads(int numThreads) {
        this.numThreads = numThreads;
    }

    public Reference<T> find(){
        return onFind();
    }

    protected abstract Reference<T> onFind();

}
