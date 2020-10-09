public class PointFactory {
    private static final FractionFactory ff = new FractionFactory();

    public Point<WholeNumber> makePoint(long x, long y) {
        return new Point<WholeNumber>(new WholeNumber(x), new WholeNumber(y));
    }
    public Point<Fraction<WholeNumber>> makeFractionPoint(long x, long y) {
        return new Point<Fraction<WholeNumber>>(
                ff.makeFraction(x, 1), ff.makeFraction(y, 1));
    }

    public Point<Fraction<WholeAndRt3>> makeWholeAndRt3Point(
            long xOnes, long xRt3, long yOnes, long yRt3) {
        return new Point<Fraction<WholeAndRt3>>(
                ff.makeRt3Fraction(xOnes, xRt3, 1, 0),
                ff.makeRt3Fraction(yOnes, yRt3, 1, 0));
    }
}
