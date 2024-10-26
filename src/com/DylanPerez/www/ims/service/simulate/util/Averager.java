package com.DylanPerez.www.ims.service.simulate.util;

public class Averager {

    private double sum;
    private int count;

    public Averager() {
        this.sum = 0;
        this.count = 0;
    }

    public void nextNumber(double value) {
        sum += value;
        count++;
    }

    public int getCount() { return count; }

    public double average() {
        if(count == 0) throw new IllegalStateException("Averager#average() : Cannot get average as count == 0");
        return sum / count;
    }



}
