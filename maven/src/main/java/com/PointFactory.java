package com;

import com.numbers.DoubleRep;
import com.numbers.Fraction;
import com.numbers.FractionFactory;
import com.numbers.WholeAndRt3;
import com.numbers.WholeNumber;

public class PointFactory {
    private static final FractionFactory ff = new FractionFactory();

    public Point<WholeNumber> makePoint(long x, long y) {
        return new Point<>(new WholeNumber(x), new WholeNumber(y));
    }
    public Point<Fraction<WholeNumber>> makeFractionPoint(long x, long y) {
        return new Point<>(ff.makeFraction(x, 1), ff.makeFraction(y, 1));
    }

    public Point<Fraction<WholeAndRt3>> makeWholeAndRt3Point(
            long xOnes, long xRt3, long yOnes, long yRt3) {
        return new Point<>(
                ff.makeRt3Fraction(xOnes, xRt3, 1, 0),
                ff.makeRt3Fraction(yOnes, yRt3, 1, 0));
    }

    public Point<DoubleRep> makeDoublePoint(double x, double y) {
        return new Point<>(new DoubleRep(x), new DoubleRep(y));
    }
}
