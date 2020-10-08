import org.junit.Assert;
import org.junit.Test;

public class TrapezoidTests {
    private static final TrapezoidFactory tf = new TrapezoidFactory();
    private static final PointFactory pf = new PointFactory();
    private static final FractionFactory ff = new FractionFactory();

    @Test
    public void testDistanceToPoint() {
        Trapezoid<Fraction<WholeNumber>> trap = tf.makeFractionTrapezoid(
                0, 0, 20, 0, 3, 5, 17, 5);

        Point<Fraction<WholeNumber>> pointAtVertex = pf.makeFractionPoint(17, 5);
        Assert.assertEquals(ff.makeFraction(0, 1),
                trap.distanceSq(pointAtVertex));

        Point<Fraction<WholeNumber>> pointOnBoundary = pf.makeFractionPoint(4, 0);
        Assert.assertEquals(ff.makeFraction(0, 1),
                trap.distanceSq(pointOnBoundary));

        Point<Fraction<WholeNumber>> pointNearVertex = pf.makeFractionPoint(
                -15, -16);
        Assert.assertEquals(ff.makeFraction(15 * 15 + 16 * 16, 1),
                trap.distanceSq(pointNearVertex));

        Point<Fraction<WholeNumber>> pointNearSide = pf.makeFractionPoint(
                11, -13);
        Assert.assertEquals(ff.makeFraction(13 * 13, 1),
                trap.distanceSq(pointNearSide));
    }

    @Test
    public void testTrapezoidMinDistSq() {
        Trapezoid<Fraction<WholeNumber>> trap1 = tf.makeFractionTrapezoid(
                0, 0, 20, 0, 17, 5, 3, 5);

        // Minimum distance is between two vertices.
        Trapezoid<Fraction<WholeNumber>> trap2 = tf.makeFractionTrapezoid(
                20, 100, 30, 110, 29, 110, 20, 101);
        Assert.assertEquals(ff.makeFraction(3 * 3 + 95 * 95, 1),
                trap1.minDistanceSq(trap2));
        Assert.assertEquals(ff.makeFraction(3 * 3 + 95 * 95, 1),
                trap2.minDistanceSq(trap1));

        // Minimum distance is between a vertex and a trapezoid side.
        Trapezoid<Fraction<WholeNumber>> trap3 = tf.makeFractionTrapezoid(
                10, 100, 20, 110, 19, 110, 10, 101);
        Assert.assertEquals(ff.makeFraction(95 * 95, 1),
                trap1.minDistanceSq(trap3));
        Assert.assertEquals(ff.makeFraction(95 * 95, 1),
                trap3.minDistanceSq(trap1));

        // Trapezoids share a vertex.
        Trapezoid<Fraction<WholeNumber>> trap4 = tf.makeFractionTrapezoid(
                20, 0, 21, -1, 21, -10, 20, -11);
        Assert.assertEquals(ff.makeFraction(0, 1), trap1.minDistanceSq(trap4));
        Assert.assertEquals(ff.makeFraction(0, 1), trap4.minDistanceSq(trap1));

        // Trapezoid sides overlap (and do not share a vertex).
        Trapezoid<Fraction<WholeNumber>> trap5 = tf.makeFractionTrapezoid(
                -1, 5, 100, 5, 101, 6, -2, 6);
        Assert.assertEquals(ff.makeFraction(0, 1), trap1.minDistanceSq(trap5));
        Assert.assertEquals(ff.makeFraction(0, 1), trap5.minDistanceSq(trap1));
    }

    @Test
    public void testTrapezoidMaxDistSq() {
        Trapezoid<Fraction<WholeNumber>> trap1 = tf.makeFractionTrapezoid(
                0, 0, 20, 0, 17, 5, 3, 5);

        Trapezoid<Fraction<WholeNumber>> trap2 = tf.makeFractionTrapezoid(
                20, 100, 30, 110, 29, 110, 20, 101);
        Assert.assertEquals(ff.makeFraction(30 * 30 + 110 * 110, 1),
                trap1.maxDistanceSq(trap2));
        Assert.assertEquals(ff.makeFraction(30 * 30 + 110 * 110, 1),
                trap2.maxDistanceSq(trap1));

        // Minimum distance is between a vertex and a trapezoid side.
        Trapezoid<Fraction<WholeNumber>> trap3 = tf.makeFractionTrapezoid(
                10, 100, 20, 110, 19, 110, 10, 101);
        Assert.assertEquals(ff.makeFraction(20 * 20 + 110 * 110, 1),
                trap1.maxDistanceSq(trap3));
        Assert.assertEquals(ff.makeFraction(20 * 20 + 110 * 110, 1),
                trap3.maxDistanceSq(trap1));

        // Trapezoids share a vertex.
        Trapezoid<Fraction<WholeNumber>> trap4 = tf.makeFractionTrapezoid(
                20, 0, 21, -1, 21, -10, 20, -11);
        Assert.assertEquals(ff.makeFraction(18 * 18 + 15 * 15, 1),
                trap1.maxDistanceSq(trap4));
        Assert.assertEquals(ff.makeFraction(18 * 18 + 15 * 15, 1),
                trap4.maxDistanceSq(trap1));

        // Trapezoid sides overlap (and do not share a vertex).
        Trapezoid<Fraction<WholeNumber>> trap5 = tf.makeFractionTrapezoid(
                -1, 5, 100, 5, 101, 6, -2, 6);
        Assert.assertEquals(ff.makeFraction(101 * 101 + 6 * 6, 1),
                trap1.maxDistanceSq(trap5));
        Assert.assertEquals(ff.makeFraction(101 * 101 + 6 * 6, 1),
                trap5.maxDistanceSq(trap1));
    }
}
