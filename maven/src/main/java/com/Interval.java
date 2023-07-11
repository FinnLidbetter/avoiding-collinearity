package com;

public class Interval {
    int lo;
    int hi;
    public Interval(int lo, int hi) {
        this.lo = lo;
        this.hi = hi;
    }

    public int getLo() {
        return lo;
    }

    public int getHi() {
        return hi;
    }

    public String toString() {
        return String.format("[%d,%d]", lo, hi);
    }

    public boolean equals(Object obj) {
        Interval i2 = (Interval) obj;
        return lo == i2.lo && hi == i2.hi;
    }
}
