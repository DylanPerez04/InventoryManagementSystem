package com.DylanPerez.www.ims.service.simulate.util;

public class BoolSource {

    private double probability;

    public BoolSource() { this(.5); }

    public BoolSource(double p) {
        if(p >= 0 && p <= 1)
            this.probability = p;
        else
            throw new IllegalArgumentException("BoolSource#BoolSource() : Invalid range for p (" + p +")");
    }

    public boolean query() {
        return Math.random() * Integer.MAX_VALUE < probability * Integer.MAX_VALUE;
    }

}
