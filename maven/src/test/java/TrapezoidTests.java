import com.Point;
import com.PointFactory;
import com.Trapezoid;
import com.TrapezoidFactory;
import com.numbers.Fraction;
import com.numbers.FractionFactory;
import com.numbers.WholeNumber;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class TrapezoidTests {
    private static final TrapezoidFactory tf = new TrapezoidFactory();
    private static final PointFactory pf = new PointFactory();
    private static final FractionFactory ff = new FractionFactory();

    @Test
    public void testDistanceToPoint() {
        Trapezoid<Fraction<WholeNumber>> trap = tf.makeFractionTrapezoid(
                0, 0, 20, 0, 3, 5, 17, 5);

        Point<Fraction<WholeNumber>> pointAtVertex = pf.makeFractionPoint(17, 5);
        Assertions.assertEquals(ff.makeFraction(0, 1),
                trap.distanceSq(pointAtVertex));

        Point<Fraction<WholeNumber>> pointOnBoundary = pf.makeFractionPoint(4, 0);
        Assertions.assertEquals(ff.makeFraction(0, 1),
                trap.distanceSq(pointOnBoundary));

        Point<Fraction<WholeNumber>> pointNearVertex = pf.makeFractionPoint(
                -15, -16);
        Assertions.assertEquals(ff.makeFraction(15 * 15 + 16 * 16, 1),
                trap.distanceSq(pointNearVertex));

        Point<Fraction<WholeNumber>> pointNearSide = pf.makeFractionPoint(
                11, -13);
        Assertions.assertEquals(ff.makeFraction(13 * 13, 1),
                trap.distanceSq(pointNearSide));
    }

    @Test
    public void testTrapezoidMinDistSq() {
        Trapezoid<Fraction<WholeNumber>> trap1 = tf.makeFractionTrapezoid(
                0, 0, 20, 0, 17, 5, 3, 5);

        // Minimum distance is between two vertices.
        Trapezoid<Fraction<WholeNumber>> trap2 = tf.makeFractionTrapezoid(
                20, 100, 30, 110, 29, 110, 20, 101);
        Assertions.assertEquals(ff.makeFraction(3 * 3 + 95 * 95, 1),
                trap1.minDistanceSq(trap2));
        Assertions.assertEquals(ff.makeFraction(3 * 3 + 95 * 95, 1),
                trap2.minDistanceSq(trap1));

        // Minimum distance is between a vertex and a trapezoid side.
        Trapezoid<Fraction<WholeNumber>> trap3 = tf.makeFractionTrapezoid(
                10, 100, 20, 110, 19, 110, 10, 101);
        Assertions.assertEquals(ff.makeFraction(95 * 95, 1),
                trap1.minDistanceSq(trap3));
        Assertions.assertEquals(ff.makeFraction(95 * 95, 1),
                trap3.minDistanceSq(trap1));

        // Trapezoids share a vertex.
        Trapezoid<Fraction<WholeNumber>> trap4 = tf.makeFractionTrapezoid(
                20, 0, 21, -1, 21, -10, 20, -11);
        Assertions.assertEquals(ff.makeFraction(0, 1), trap1.minDistanceSq(trap4));
        Assertions.assertEquals(ff.makeFraction(0, 1), trap4.minDistanceSq(trap1));

        // com.Trapezoid sides overlap (and do not share a vertex).
        Trapezoid<Fraction<WholeNumber>> trap5 = tf.makeFractionTrapezoid(
                -1, 5, 100, 5, 101, 6, -2, 6);
        Assertions.assertEquals(ff.makeFraction(0, 1), trap1.minDistanceSq(trap5));
        Assertions.assertEquals(ff.makeFraction(0, 1), trap5.minDistanceSq(trap1));
    }

    @Test
    public void testTrapezoidMaxDistSq() {
        Trapezoid<Fraction<WholeNumber>> trap1 = tf.makeFractionTrapezoid(
                0, 0, 20, 0, 17, 5, 3, 5);

        Trapezoid<Fraction<WholeNumber>> trap2 = tf.makeFractionTrapezoid(
                20, 100, 30, 110, 29, 110, 20, 101);
        Assertions.assertEquals(ff.makeFraction(30 * 30 + 110 * 110, 1),
                trap1.maxDistanceSq(trap2));
        Assertions.assertEquals(ff.makeFraction(30 * 30 + 110 * 110, 1),
                trap2.maxDistanceSq(trap1));

        // Minimum distance is between a vertex and a trapezoid side.
        Trapezoid<Fraction<WholeNumber>> trap3 = tf.makeFractionTrapezoid(
                10, 100, 20, 110, 19, 110, 10, 101);
        Assertions.assertEquals(ff.makeFraction(20 * 20 + 110 * 110, 1),
                trap1.maxDistanceSq(trap3));
        Assertions.assertEquals(ff.makeFraction(20 * 20 + 110 * 110, 1),
                trap3.maxDistanceSq(trap1));

        // Trapezoids share a vertex.
        Trapezoid<Fraction<WholeNumber>> trap4 = tf.makeFractionTrapezoid(
                20, 0, 21, -1, 21, -10, 20, -11);
        Assertions.assertEquals(ff.makeFraction(18 * 18 + 15 * 15, 1),
                trap1.maxDistanceSq(trap4));
        Assertions.assertEquals(ff.makeFraction(18 * 18 + 15 * 15, 1),
                trap4.maxDistanceSq(trap1));

        // com.Trapezoid sides overlap (and do not share a vertex).
        Trapezoid<Fraction<WholeNumber>> trap5 = tf.makeFractionTrapezoid(
                -1, 5, 100, 5, 101, 6, -2, 6);
        Assertions.assertEquals(ff.makeFraction(101 * 101 + 6 * 6, 1),
                trap1.maxDistanceSq(trap5));
        Assertions.assertEquals(ff.makeFraction(101 * 101 + 6 * 6, 1),
                trap5.maxDistanceSq(trap1));
    }

    @Test
    public void testIntersectsInfiniteLine() {
        Trapezoid<Fraction<WholeNumber>> trap1 = tf.makeFractionTrapezoid(
                0, 0, 20, 0, 17, 3, 3, 3);
        Trapezoid<Fraction<WholeNumber>> trap2 = tf.makeFractionTrapezoid(
                10, 10, 10, 6, 12, 5, 12, 9);
        // Infinite line goes along horizontal trapezoid side.
        Point<Fraction<WholeNumber>> p1 = pf.makeFractionPoint(-10, 0);
        Point<Fraction<WholeNumber>> p2 = pf.makeFractionPoint(3, 0);
        Assertions.assertTrue(trap1.intersectsInfiniteLine(p1, p2));
        Assertions.assertTrue(trap1.intersectsInfiniteLine(p2, p1));

        Point<Fraction<WholeNumber>> p3 = pf.makeFractionPoint(105, 3);
        Point<Fraction<WholeNumber>> p4 = pf.makeFractionPoint(106, 3);
        Assertions.assertTrue(trap1.intersectsInfiniteLine(p3, p4));
        Assertions.assertTrue(trap1.intersectsInfiniteLine(p4, p3));

        // Infinite line goes along vertical trapezoid side.
        Point<Fraction<WholeNumber>> p5 = pf.makeFractionPoint(10, 40);
        Point<Fraction<WholeNumber>> p6 = pf.makeFractionPoint(10, 43);
        Assertions.assertTrue(trap2.intersectsInfiniteLine(p5, p6));
        Assertions.assertTrue(trap2.intersectsInfiniteLine(p6, p5));

        // Infinite line goes along non-vertical, non-horizontal side.
        Point<Fraction<WholeNumber>> p7 = pf.makeFractionPoint(1, 1);
        Point<Fraction<WholeNumber>> p8 = pf.makeFractionPoint(2, 2);
        Assertions.assertTrue(trap1.intersectsInfiniteLine(p7, p8));
        Assertions.assertTrue(trap1.intersectsInfiniteLine(p8, p7));

        Point<Fraction<WholeNumber>> p9 = pf.makeFractionPoint(12, 9);
        Point<Fraction<WholeNumber>> p10 = pf.makeFractionPoint(8, 11);
        Assertions.assertTrue(trap2.intersectsInfiniteLine(p9, p10));
        Assertions.assertTrue(trap2.intersectsInfiniteLine(p10, p9));

        // Infinite line goes through exactly one point on trapezoid.
        //  First point.
        Point<Fraction<WholeNumber>> p11 = pf.makeFractionPoint(-10, 10);
        Point<Fraction<WholeNumber>> p12 = pf.makeFractionPoint(1, -1);
        Assertions.assertTrue(trap1.intersectsInfiniteLine(p11, p12));
        Assertions.assertTrue(trap1.intersectsInfiniteLine(p12, p11));
        //  Second point.
        Point<Fraction<WholeNumber>> p13 = pf.makeFractionPoint(20, -200);
        Point<Fraction<WholeNumber>> p14 = pf.makeFractionPoint(20, -1000);
        Assertions.assertTrue(trap1.intersectsInfiniteLine(p13, p14));
        Assertions.assertTrue(trap1.intersectsInfiniteLine(p14, p13));
        //  Third point.
        Point<Fraction<WholeNumber>> p15 = pf.makeFractionPoint(17, 3);
        Point<Fraction<WholeNumber>> p16 = pf.makeFractionPoint(27, 2);
        Assertions.assertTrue(trap1.intersectsInfiniteLine(p15, p16));
        Assertions.assertTrue(trap1.intersectsInfiniteLine(p16, p15));
        //  Fourth point.
        Point<Fraction<WholeNumber>> p17 = pf.makeFractionPoint(1, 2);
        Point<Fraction<WholeNumber>> p18 = pf.makeFractionPoint(-1, 1);
        Assertions.assertTrue(trap1.intersectsInfiniteLine(p17, p18));
        Assertions.assertTrue(trap1.intersectsInfiniteLine(p18, p17));

        // Infinite line crosses trapezoid sides at non-endpoints.
        Point<Fraction<WholeNumber>> p19 = pf.makeFractionPoint(4, 1);
        Point<Fraction<WholeNumber>> p20 = pf.makeFractionPoint(4, 500);
        Assertions.assertTrue(trap1.intersectsInfiniteLine(p19, p20));
        Assertions.assertTrue(trap1.intersectsInfiniteLine(p20, p19));

        Point<Fraction<WholeNumber>> p21 = pf.makeFractionPoint(-4, 1);
        Point<Fraction<WholeNumber>> p22 = pf.makeFractionPoint(7, 4);
        Assertions.assertTrue(trap1.intersectsInfiniteLine(p21, p22));
        Assertions.assertTrue(trap1.intersectsInfiniteLine(p22, p21));

        // Infinite line parallel to trapezoid top and base and
        // below trapezoid does not intersect the trapezoid.
        Point<Fraction<WholeNumber>> p23 = pf.makeFractionPoint(1, -1);
        Point<Fraction<WholeNumber>> p24 = pf.makeFractionPoint(2, -1);
        Assertions.assertFalse(trap1.intersectsInfiniteLine(p23, p24));
        Assertions.assertFalse(trap1.intersectsInfiniteLine(p24, p23));

        // Infinite line not parallel to any trapezoid sides and
        // above trapezoid does not intersect.
        Point<Fraction<WholeNumber>> p25 = pf.makeFractionPoint(-5, 1);
        Point<Fraction<WholeNumber>> p26 = pf.makeFractionPoint(6, 4);
        Assertions.assertFalse(trap1.intersectsInfiniteLine(p25, p26));
        Assertions.assertFalse(trap1.intersectsInfiniteLine(p26, p25));
    }

    @Test
    public void testContains() {
        Trapezoid<Fraction<WholeNumber>> trap = tf.makeFractionTrapezoid(
                1, 1, 9, 1, 7, 4, 3, 4);

        Point<Fraction<WholeNumber>> p23 = pf.makeFractionPoint(2, 3);
        Point<Fraction<WholeNumber>> p33 = pf.makeFractionPoint(3, 3);
        Point<Fraction<WholeNumber>> p51 = pf.makeFractionPoint(5, 1);
        Point<Fraction<WholeNumber>> p11 = pf.makeFractionPoint(1, 1);
        Point<Fraction<WholeNumber>> p91 = pf.makeFractionPoint(9, 1);
        Point<Fraction<WholeNumber>> p74 = pf.makeFractionPoint(7, 4);
        Point<Fraction<WholeNumber>> p34 = pf.makeFractionPoint(3, 4);
        Point<Fraction<WholeNumber>> p05 = pf.makeFractionPoint(0, 5);
        Point<Fraction<WholeNumber>> p83 = pf.makeFractionPoint(8, 3);
        Assertions.assertFalse(trap.contains(p23));
        Assertions.assertFalse(trap.contains(p05));
        Assertions.assertFalse(trap.contains(p83));
        Assertions.assertTrue(trap.contains(p33));
        Assertions.assertTrue(trap.contains(p51));
        Assertions.assertTrue(trap.contains(p11));
        Assertions.assertTrue(trap.contains(p91));
        Assertions.assertTrue(trap.contains(p74));
        Assertions.assertTrue(trap.contains(p34));
    }
}
