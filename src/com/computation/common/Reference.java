package com.computation.common;

/**
 * Created by mwkurian on 11/19/2014.
 */
public class Reference<T> {

    private volatile T t;

    public Reference(T t) {
        this.t = t;
    }

    public void update(T t){
        this.t = t;
    }

    public T get(){
        return t;
    }
}