package com.computation.experimental;

import java.util.List;

/**
 * Created by mwkurian on 11/17/2014.
 */
public abstract class SimpleSearch<T extends Comparable<T>> extends
        Search<T> {
    public SimpleSearch(List<T> data, T tmin) {
        super(data, tmin, 1);
    }

    @Override
    public void start() {
        super.start();

        T max = tmin;

        for(int i = 0; i < data.size(); i++){
            T d = data.get(i);
            if(d.compareTo(max) > 0){
                max = d;
            }
        }

        found(max);
    }
}
