package com;

import com.numbers.AbstractNumber;
import com.numbers.Fraction;
import com.numbers.WholeNumber;

public class TrapezoidFactory<T extends AbstractNumber<T>> {
    private static final PointFactory pf = new PointFactory();

    /**
     * Create a trapezoid with coordinates using fractions of whole com.numbers.
     *
     * The denominator is always 1.
     */
    public Trapezoid<Fraction<WholeNumber>> makeFractionTrapezoid(
            long x1, long y1, long x2, long y2,
            long x3, long y3, long x4, long y4) {
        Point<Fraction<WholeNumber>> p1 = pf.makeFractionPoint(x1, y1);
        Point<Fraction<WholeNumber>> p2 = pf.makeFractionPoint(x2, y2);
        Point<Fraction<WholeNumber>> p3 = pf.makeFractionPoint(x3, y3);
        Point<Fraction<WholeNumber>> p4 = pf.makeFractionPoint(x4, y4);
        return new Trapezoid<>(p1, p2, p3, p4);
    }

    /**
     * Make a com.Trapezoid of the specified type starting at startPoint.
     *
     * The types are given below. The startPoints are marked by '.' these
     * will always be vertex 0 in the trapezoid and vertex 3 will always be
     * the vertex marked by ','
     * The trapezoids have a base length (the longest side) of 6.
     * The top edge has length 4, the two sides have length 2.
     *     Type 0   Type 1   Type 2  Type 3   Type 4  Type 5
     *      ___    .____,    ,      ,__         __.      .
     *    ./___\,   \__/     /\      \ \       / /      /\
     *                       \ \      \ \     / /      / /
     *                        \_\      \/     \/      /_/
     *                           .      .     ,      ,
     */
    public Trapezoid<T> makeSequenceTrapezoid(
            TrapezoidType type, Point<T> startPoint) {
        return switch (type) {
            case ZERO -> makeTrapezoidType0(startPoint);
            case ONE -> makeTrapezoidType1(startPoint);
            case TWO -> makeTrapezoidType2(startPoint);
            case THREE -> makeTrapezoidType3(startPoint);
            case FOUR -> makeTrapezoidType4(startPoint);
            case FIVE -> makeTrapezoidType5(startPoint);
            default -> throw new IllegalArgumentException("Unknown trapezoid type.");
        };
    }
    private Trapezoid<T> makeTrapezoidType0(Point<T> startPoint) {
        T tVal = startPoint.x;
        Point<T> pt1 = new Point<>(
            startPoint.x.add(tVal.one()), startPoint.y.add(tVal.rt3()));
        Point<T> pt2 = new Point<>(
            startPoint.x.add(tVal.five()), startPoint.y.add(tVal.rt3()));
        Point<T> pt3 = new Point<>(
            startPoint.x.add(tVal.six()), startPoint.y);
        return new Trapezoid<>(startPoint, pt1, pt2, pt3);
    }
    private Trapezoid<T> makeTrapezoidType1(Point<T> startPoint) {
        T tVal = startPoint.x;
        Point<T> pt1 = new Point<>(
            startPoint.x.add(tVal.one()), startPoint.y.subtract(tVal.rt3()));
        Point<T> pt2 = new Point<>(
            startPoint.x.add(tVal.five()), startPoint.y.subtract(tVal.rt3()));
        Point<T> pt3 = new Point<>(
            startPoint.x.add(tVal.six()), startPoint.y);
        return new Trapezoid<>(startPoint, pt1, pt2, pt3);
    }

    private Trapezoid<T> makeTrapezoidType2(Point<T> startPoint) {
        T tVal = startPoint.x;
        Point<T> pt1 = new Point<>(
                startPoint.x.subtract(tVal.two()), startPoint.y);
        Point<T> pt2 = new Point<>(
                startPoint.x.subtract(tVal.four()), startPoint.y.add(tVal.twoRt3()));
        Point<T> pt3 = new Point<>(
                startPoint.x.subtract(tVal.three()), startPoint.y.add(tVal.threeRt3()));
        return new Trapezoid<>(startPoint, pt1, pt2, pt3);
    }

    private Trapezoid<T> makeTrapezoidType3(Point<T> startPoint) {
        T tVal = startPoint.x;
        Point<T> pt1 = new Point<>(
                startPoint.x.add(tVal.one()), startPoint.y.add(tVal.rt3()));
        Point<T> pt2 = new Point<>(
                startPoint.x.subtract(tVal.one()), startPoint.y.add(tVal.threeRt3()));
        Point<T> pt3 = new Point<>(
                startPoint.x.subtract(tVal.three()), startPoint.y.add(tVal.threeRt3()));
        return new Trapezoid<>(startPoint, pt1, pt2, pt3);
    }

    private Trapezoid<T> makeTrapezoidType4(Point<T> startPoint) {
        T tVal = startPoint.x;
        Point<T> pt1 = new Point<>(
            startPoint.x.subtract(tVal.two()), startPoint.y);
        Point<T> pt2 = new Point<>(
            startPoint.x.subtract(tVal.four()), startPoint.y.subtract(tVal.twoRt3()));
        Point<T> pt3 = new Point<>(
            startPoint.x.subtract(tVal.three()), startPoint.y.subtract(tVal.threeRt3()));
        return new Trapezoid<>(startPoint, pt1, pt2, pt3);
    }

    private Trapezoid<T> makeTrapezoidType5(Point<T> startPoint) {
        T tVal = startPoint.x;
        Point<T> pt1 = new Point<>(
            startPoint.x.add(tVal.one()), startPoint.y.subtract(tVal.rt3()));
        Point<T> pt2 = new Point<>(
            startPoint.x.subtract(tVal.one()), startPoint.y.subtract(tVal.threeRt3()));
        Point<T> pt3 = new Point<>(
            startPoint.x.subtract(tVal.three()), startPoint.y.subtract(tVal.threeRt3()));
        return new Trapezoid<>(startPoint, pt1, pt2, pt3);
    }
}
