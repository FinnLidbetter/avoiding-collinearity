public class PointFactory {
    private static final FractionFactory ff = new FractionFactory();

    public Point<WholeNumber> makePoint(long x, long y) {
        return new Point<WholeNumber>(new WholeNumber(x), new WholeNumber(y));
    }
    public Point<Fraction<WholeNumber>> makeFractionPoint(long x, long y) {
        return new Point<Fraction<WholeNumber>>(
                ff.makeFraction(x, 1), ff.makeFraction(y, 1));
    }
}
