public class LineFactory {
    private static final PointFactory pf = new PointFactory();

    public LineSegment<WholeNumber> makeLine(long x1, long y1, long x2, long y2) {
        return new LineSegment<>(pf.makePoint(x1, y1), pf.makePoint(x2, y2));
    }

    public LineSegment<Fraction<WholeNumber>> makeFractionLine(
            long x1, long y1, long x2, long y2) {
        return new LineSegment<>(
                pf.makeFractionPoint(x1, y1), pf.makeFractionPoint(x2, y2));
    }
}
