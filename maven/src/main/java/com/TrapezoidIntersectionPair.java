package com;

import com.numbers.AbstractNumber;

public class TrapezoidIntersectionPair<T extends AbstractNumber<T>> {
    public int numTrapezoidsIntersected;
    public int trapezoidIndex1;
    public int trapezoidIndex2;
    public Point<T> p1;
    public Point<T> p2;
    public TrapezoidIntersectionPair(
            int numTrapezoidsIntersected,
            int trapezoidIndex1,
            int trapezoidIndex2,
            Point<T> p1,
            Point<T> p2
    ) {
        this.numTrapezoidsIntersected = numTrapezoidsIntersected;
        this.trapezoidIndex1 = trapezoidIndex1;
        this.trapezoidIndex2 = trapezoidIndex2;
        this.p1 = p1;
        this.p2 = p2;
    }
}
