public class TrapezoidFactory {
    private static final PointFactory pf = new PointFactory();
    private static final WholeAndRt3 rt3 = new WholeAndRt3(0, 1);
    private static final WholeAndRt3 twoRt3 = new WholeAndRt3(0, 2);
    private static final WholeAndRt3 threeRt3 = new WholeAndRt3(0, 3);
    private static final WholeAndRt3 one = new WholeAndRt3(1, 0);
    private static final WholeAndRt3 two = new WholeAndRt3(2, 0);
    private static final WholeAndRt3 three = new WholeAndRt3(3, 0);
    private static final WholeAndRt3 four = new WholeAndRt3(4, 0);
    private static final WholeAndRt3 five = new WholeAndRt3(5, 0);
    private static final WholeAndRt3 six = new WholeAndRt3(6, 0);
    private static final Fraction<WholeAndRt3> fracRt3 = new Fraction<>(rt3, one);
    private static final Fraction<WholeAndRt3> fracTwoRt3 = new Fraction<>(twoRt3, one);
    private static final Fraction<WholeAndRt3> fracThreeRt3 = new Fraction<>(threeRt3, one);
    private static final Fraction<WholeAndRt3> fracOne = new Fraction<>(one, one);
    private static final Fraction<WholeAndRt3> fracTwo = new Fraction<>(two, one);
    private static final Fraction<WholeAndRt3> fracThree = new Fraction<>(three, one);
    private static final Fraction<WholeAndRt3> fracFour = new Fraction<>(four, one);
    private static final Fraction<WholeAndRt3> fracFive = new Fraction<>(five, one);
    private static final Fraction<WholeAndRt3> fracSix = new Fraction<>(six, one);

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
     * Make a Trapezoid of the specified type starting at startPoint.
     *
     * The types are given below. The startPoints are marked by '.' these
     * will always be vertex 0 in the trapezoid and vertex 3 will always be
     * the vertex marked by ','
     * The trapezoids have a base length (longest side) of 6.
     * The top edge has length 4, the two sides have length 2.
     *     Type 0   Type 1   Type 2  Type 3   Type 4  Type 5
     *      ___    .____,   ,__       ,         __.      .
     *    ./___\,   \__/     \ \      /\       / /      /\
     *                        \ \     \ \     / /      / /
     *                         \/      \_\    \/      /_/
     *                          .         .   ,      ,
     */
    public Trapezoid<Fraction<WholeAndRt3>> makeSequenceTrapezoid(
            int type, Point<Fraction<WholeAndRt3>> startPoint) {
        switch (type) {
            case 0:
                return makeTrapezoidType0(startPoint);
            case 1:
                return makeTrapezoidType1(startPoint);
            case 2:
                return makeTrapezoidType2(startPoint);
            case 3:
                return makeTrapezoidType3(startPoint);
            case 4:
                return makeTrapezoidType4(startPoint);
            case 5:
                return makeTrapezoidType5(startPoint);
            default:
                throw new IllegalArgumentException("Unknown trapezoid type.");
        }
    }
    private Trapezoid<Fraction<WholeAndRt3>> makeTrapezoidType0(
            Point<Fraction<WholeAndRt3>> startPoint) {
        Point<Fraction<WholeAndRt3>> pt0 = startPoint;
        Point<Fraction<WholeAndRt3>> pt1 = new Point<>(
            startPoint.x.add(fracOne), startPoint.y.add(fracRt3));
        Point<Fraction<WholeAndRt3>> pt2 = new Point<>(
            startPoint.x.add(fracFive), startPoint.y.add(fracRt3));
        Point<Fraction<WholeAndRt3>> pt3 = new Point<>(
            startPoint.y.add(fracSix), startPoint.y);
        return new Trapezoid<>(pt0, pt1, pt2, pt3);
    }
    private Trapezoid<Fraction<WholeAndRt3>> makeTrapezoidType1(
            Point<Fraction<WholeAndRt3>> startPoint) {
        Point<Fraction<WholeAndRt3>> pt0 = startPoint;
        Point<Fraction<WholeAndRt3>> pt1 = new Point<>(
            startPoint.x.add(fracOne), startPoint.y.subtract(fracRt3));
        Point<Fraction<WholeAndRt3>> pt2 = new Point<>(
            startPoint.x.add(fracFive), startPoint.y.subtract(fracRt3));
        Point<Fraction<WholeAndRt3>> pt3 = new Point<>(
            startPoint.x.add(fracSix), startPoint.y);
        return new Trapezoid<>(pt0, pt1, pt2, pt3);
    }

    private Trapezoid<Fraction<WholeAndRt3>> makeTrapezoidType2(
            Point<Fraction<WholeAndRt3>> startPoint) {
        Point<Fraction<WholeAndRt3>> pt0 = startPoint;
        Point<Fraction<WholeAndRt3>> pt1 = new Point<>(
            startPoint.x.add(fracOne), startPoint.y.add(fracRt3));
        Point<Fraction<WholeAndRt3>> pt2 = new Point<>(
            startPoint.x.subtract(fracTwo), startPoint.y.add(fracThreeRt3));
        Point<Fraction<WholeAndRt3>> pt3 = new Point<>(
            startPoint.x.subtract(fracThree), startPoint.y.add(fracThreeRt3));
        return new Trapezoid<>(pt0, pt1, pt2, pt3);
    }

    private Trapezoid<Fraction<WholeAndRt3>> makeTrapezoidType3(
            Point<Fraction<WholeAndRt3>> startPoint) {
        Point<Fraction<WholeAndRt3>> pt0 = startPoint;
        Point<Fraction<WholeAndRt3>> pt1 = new Point<>(
            startPoint.x.subtract(fracTwo), startPoint.y);
        Point<Fraction<WholeAndRt3>> pt2 = new Point<>(
            startPoint.x.subtract(fracFour), startPoint.y.add(fracTwoRt3));
        Point<Fraction<WholeAndRt3>> pt3 = new Point<>(
            startPoint.x.subtract(fracThree), startPoint.y.add(fracThreeRt3));
        return new Trapezoid<>(pt0, pt1, pt2, pt3);
    }

    private Trapezoid<Fraction<WholeAndRt3>> makeTrapezoidType4(
            Point<Fraction<WholeAndRt3>> startPoint) {
        Point<Fraction<WholeAndRt3>> pt0 = startPoint;
        Point<Fraction<WholeAndRt3>> pt1 = new Point<>(
            startPoint.x.subtract(fracTwo), startPoint.y);
        Point<Fraction<WholeAndRt3>> pt2 = new Point<>(
            startPoint.x.subtract(fracFour), startPoint.y.subtract(fracTwoRt3));
        Point<Fraction<WholeAndRt3>> pt3 = new Point<>(
            startPoint.x.subtract(fracThree), startPoint.y.subtract(fracThreeRt3));
        return new Trapezoid<>(pt0, pt1, pt2, pt3);
    }

    private Trapezoid<Fraction<WholeAndRt3>> makeTrapezoidType5(
            Point<Fraction<WholeAndRt3>> startPoint) {
        Point<Fraction<WholeAndRt3>> pt0 = startPoint;
        Point<Fraction<WholeAndRt3>> pt1 = new Point<>(
            startPoint.x.add(fracOne), startPoint.y.subtract(fracRt3));
        Point<Fraction<WholeAndRt3>> pt2 = new Point<>(
            startPoint.x.subtract(fracOne), startPoint.y.subtract(fracThreeRt3));
        Point<Fraction<WholeAndRt3>> pt3 = new Point<>(
            startPoint.x.subtract(fracThree), startPoint.y.subtract(fracThreeRt3));
        return new Trapezoid<>(pt0, pt1, pt2, pt3);
    }
}
