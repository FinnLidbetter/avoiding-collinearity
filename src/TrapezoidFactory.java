public class TrapezoidFactory {
    private static final PointFactory pf = new PointFactory();

    public Trapezoid<Fraction<WholeNumber>> makeFractionTrapezoid(
            long x1, long y1, long x2, long y2,
            long x3, long y3, long x4, long y4) {
        Point<Fraction<WholeNumber>> p1 = pf.makeFractionPoint(x1, y1);
        Point<Fraction<WholeNumber>> p2 = pf.makeFractionPoint(x2, y2);
        Point<Fraction<WholeNumber>> p3 = pf.makeFractionPoint(x3, y3);
        Point<Fraction<WholeNumber>> p4 = pf.makeFractionPoint(x4, y4);

        return new Trapezoid<Fraction<WholeNumber>>(p1, p2, p3, p4);
    }
}
