import com.LineFactory;
import com.LineSegment;
import com.Point;
import com.PointFactory;
import com.numbers.Fraction;
import com.numbers.FractionFactory;
import com.numbers.WholeNumber;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class LineTests {
    private static final LineFactory lf = new LineFactory();
    private static final PointFactory pf = new PointFactory();
    private static final FractionFactory ff = new FractionFactory();

    /**
     * Test whether a point lies between the parallel lines through the
     * endpoints of a line, where the parallel lines are perpendicular to the
     * line in question.
     */
    @Test
    public void testHasBetween() {
        testHorizontalLineCases();
        testVerticalLineCases();
        testSlantedLineCases();
    }

    /**
     * Test checking whether a point lies "between the endpoints" of a
     * horizontal line.
     */
    private void testHorizontalLineCases() {
        LineSegment<WholeNumber> horizontalLine = lf.makeLine(4, 15, 17, 15);
        LineSegment<WholeNumber> flippedHorizontalLine = lf.makeLine(
                17, 15, 4, 15);

        // com.Point strictly below line cases.
        Point<WholeNumber> belowPastLeft = pf.makePoint(3, 14);
        Assertions.assertFalse(horizontalLine.hasBetween(belowPastLeft));
        Assertions.assertFalse(flippedHorizontalLine.hasBetween(belowPastLeft));

        Point<WholeNumber> belowAtLeft = pf.makePoint(4, 0);
        Assertions.assertTrue(horizontalLine.hasBetween(belowAtLeft));
        Assertions.assertTrue(flippedHorizontalLine.hasBetween(belowAtLeft));

        Point<WholeNumber> belowStrictlyBetween = pf.makePoint(7, -1);
        Assertions.assertTrue(horizontalLine.hasBetween(belowStrictlyBetween));
        Assertions.assertTrue(flippedHorizontalLine.hasBetween(belowStrictlyBetween));

        Point<WholeNumber> belowAtRight = pf.makePoint(17, 0);
        Assertions.assertTrue(horizontalLine.hasBetween(belowAtRight));
        Assertions.assertTrue(flippedHorizontalLine.hasBetween(belowAtRight));

        Point<WholeNumber> belowPastRight = pf.makePoint(18, 14);
        Assertions.assertFalse(horizontalLine.hasBetween(belowPastRight));
        Assertions.assertFalse(flippedHorizontalLine.hasBetween(belowPastRight));

        // com.Point neither strictly above nor strictly below the line.
        Point<WholeNumber> onPastLeft = pf.makePoint(3, 15);
        Assertions.assertFalse(horizontalLine.hasBetween(onPastLeft));
        Assertions.assertFalse(flippedHorizontalLine.hasBetween(onPastLeft));

        Point<WholeNumber> onAtLeft = pf.makePoint(4, 15);
        Assertions.assertTrue(horizontalLine.hasBetween(onAtLeft));
        Assertions.assertTrue(flippedHorizontalLine.hasBetween(onAtLeft));

        Point<WholeNumber> onStrictlyBetween = pf.makePoint(7, 15);
        Assertions.assertTrue(horizontalLine.hasBetween(onStrictlyBetween));
        Assertions.assertTrue(flippedHorizontalLine.hasBetween(onStrictlyBetween));

        Point<WholeNumber> onAtRight = pf.makePoint(17, 15);
        Assertions.assertTrue(horizontalLine.hasBetween(onAtRight));
        Assertions.assertTrue(flippedHorizontalLine.hasBetween(onAtRight));

        Point<WholeNumber> onPastRight = pf.makePoint(18, 15);
        Assertions.assertFalse(horizontalLine.hasBetween(onPastRight));
        Assertions.assertFalse(flippedHorizontalLine.hasBetween(onPastRight));

        // com.Point is strictly above the line.
        Point<WholeNumber> abovePastLeft = pf.makePoint(3, 16);
        Assertions.assertFalse(horizontalLine.hasBetween(abovePastLeft));
        Assertions.assertFalse(flippedHorizontalLine.hasBetween(abovePastLeft));

        Point<WholeNumber> aboveAtLeft = pf.makePoint(4, 17);
        Assertions.assertTrue(horizontalLine.hasBetween(aboveAtLeft));
        Assertions.assertTrue(flippedHorizontalLine.hasBetween(aboveAtLeft));

        Point<WholeNumber> aboveStrictlyBetween = pf.makePoint(7, 200);
        Assertions.assertTrue(horizontalLine.hasBetween(aboveStrictlyBetween));
        Assertions.assertTrue(flippedHorizontalLine.hasBetween(aboveStrictlyBetween));

        Point<WholeNumber> aboveAtRight = pf.makePoint(17, 34);
        Assertions.assertTrue(horizontalLine.hasBetween(aboveAtRight));
        Assertions.assertTrue(flippedHorizontalLine.hasBetween(aboveAtRight));

        Point<WholeNumber> abovePastRight = pf.makePoint(18, 16);
        Assertions.assertFalse(horizontalLine.hasBetween(abovePastRight));
        Assertions.assertFalse(flippedHorizontalLine.hasBetween(abovePastRight));
    }

    /**
     * Test checking whether a point lies "between the endpoints" of a
     * vertical line.
     */
    private void testVerticalLineCases() {
        LineSegment<WholeNumber> verticalLine = lf.makeLine(-3, 15, -3, 36);
        LineSegment<WholeNumber> flippedVerticalLine = lf.makeLine(
                -3, 36, -3, 15);

        // com.Point strictly left of line cases.
        Point<WholeNumber> leftPastBottom = pf.makePoint(-5, 14);
        Assertions.assertFalse(verticalLine.hasBetween(leftPastBottom));
        Assertions.assertFalse(flippedVerticalLine.hasBetween(leftPastBottom));

        Point<WholeNumber> leftAtBottom = pf.makePoint(-6, 15);
        Assertions.assertTrue(verticalLine.hasBetween(leftAtBottom));
        Assertions.assertTrue(flippedVerticalLine.hasBetween(leftAtBottom));

        Point<WholeNumber> leftStrictlyBetween = pf.makePoint(-7, 17);
        Assertions.assertTrue(verticalLine.hasBetween(leftStrictlyBetween));
        Assertions.assertTrue(flippedVerticalLine.hasBetween(leftStrictlyBetween));

        Point<WholeNumber> leftAtTop = pf.makePoint(-10, 36);
        Assertions.assertTrue(verticalLine.hasBetween(leftAtTop));
        Assertions.assertTrue(flippedVerticalLine.hasBetween(leftAtTop));

        Point<WholeNumber> leftPastTop = pf.makePoint(-4, 37);
        Assertions.assertFalse(verticalLine.hasBetween(leftPastTop));
        Assertions.assertFalse(flippedVerticalLine.hasBetween(leftPastTop));

        // com.Point is neither strictly above nor strictly below the line.
        Point<WholeNumber> onPastBottom = pf.makePoint(-3, -100);
        Assertions.assertFalse(verticalLine.hasBetween(onPastBottom));
        Assertions.assertFalse(flippedVerticalLine.hasBetween(onPastBottom));

        Point<WholeNumber> onAtBottom = pf.makePoint(-3, 15);
        Assertions.assertTrue(verticalLine.hasBetween(onAtBottom));
        Assertions.assertTrue(flippedVerticalLine.hasBetween(onAtBottom));

        Point<WholeNumber> onStrictlyBetween = pf.makePoint(-3, 20);
        Assertions.assertTrue(verticalLine.hasBetween(onStrictlyBetween));
        Assertions.assertTrue(flippedVerticalLine.hasBetween(onStrictlyBetween));

        Point<WholeNumber> onAtTop = pf.makePoint(-3, 36);
        Assertions.assertTrue(verticalLine.hasBetween(onAtTop));
        Assertions.assertTrue(flippedVerticalLine.hasBetween(onAtTop));

        Point<WholeNumber> onPastTop = pf.makePoint(-3, 100);
        Assertions.assertFalse(verticalLine.hasBetween(onPastTop));
        Assertions.assertFalse(flippedVerticalLine.hasBetween(onPastTop));

        // com.Point is strictly right of the line cases.
        Point<WholeNumber> rightPastBottom = pf.makePoint(-2, -10);
        Assertions.assertFalse(verticalLine.hasBetween(rightPastBottom));
        Assertions.assertFalse(flippedVerticalLine.hasBetween(rightPastBottom));

        Point<WholeNumber> rightAtBottom = pf.makePoint(-1, 15);
        Assertions.assertTrue(verticalLine.hasBetween(rightAtBottom));
        Assertions.assertTrue(flippedVerticalLine.hasBetween(rightAtBottom));

        Point<WholeNumber> rightStrictlyBetween = pf.makePoint(0, 21);
        Assertions.assertTrue(verticalLine.hasBetween(rightStrictlyBetween));
        Assertions.assertTrue(flippedVerticalLine.hasBetween(rightStrictlyBetween));

        Point<WholeNumber> rightAtTop = pf.makePoint(150, 36);
        Assertions.assertTrue(verticalLine.hasBetween(rightAtTop));
        Assertions.assertTrue(flippedVerticalLine.hasBetween(rightAtTop));

        Point<WholeNumber> rightPastTop = pf.makePoint(-2, 37);
        Assertions.assertFalse(verticalLine.hasBetween(rightPastTop));
        Assertions.assertFalse(flippedVerticalLine.hasBetween(rightPastTop));
    }

    /**
     * Test checking whether a point lies "between the endpoints" of a
     * line that is neither horizontal nor vertical.
     *
     * The naming for where the point lies relative to the endpoints matches
     * that of the horizontal line, since the lines tested are closer to being
     * horizontal than vertical.
     */
    private void testSlantedLineCases() {
        LineSegment<WholeNumber> slantedLine = lf.makeLine(-4, -1, 10, 3);
        LineSegment<WholeNumber> flippedSlantedLine = lf.makeLine(10, 3, -4, -1);

        // com.Point strictly below line cases.
        Point<WholeNumber> belowPastLeft = pf.makePoint(-3, -1000);
        Assertions.assertFalse(slantedLine.hasBetween(belowPastLeft));
        Assertions.assertFalse(flippedSlantedLine.hasBetween(belowPastLeft));

        Point<WholeNumber> belowAtLeft = pf.makePoint(4, -29);
        Assertions.assertTrue(slantedLine.hasBetween(belowAtLeft));
        Assertions.assertTrue(flippedSlantedLine.hasBetween(belowAtLeft));

        Point<WholeNumber> belowStrictlyBetween = pf.makePoint(2, 0);
        Assertions.assertTrue(slantedLine.hasBetween(belowStrictlyBetween));
        Assertions.assertTrue(flippedSlantedLine.hasBetween(belowStrictlyBetween));

        Point<WholeNumber> belowAtRight = pf.makePoint(-4, 12);
        Assertions.assertTrue(slantedLine.hasBetween(belowAtRight));
        Assertions.assertTrue(flippedSlantedLine.hasBetween(belowAtRight));

        Point<WholeNumber> belowPastRight = pf.makePoint(12, 0);
        Assertions.assertFalse(slantedLine.hasBetween(belowPastRight));
        Assertions.assertFalse(flippedSlantedLine.hasBetween(belowPastRight));

        // com.Point neither strictly above nor strictly below the line.
        Point<WholeNumber> onPastLeft = pf.makePoint(-11, -3);
        Assertions.assertFalse(slantedLine.hasBetween(onPastLeft));
        Assertions.assertFalse(flippedSlantedLine.hasBetween(onPastLeft));

        Point<WholeNumber> onAtLeft = pf.makePoint(-4, -1);
        Assertions.assertTrue(slantedLine.hasBetween(onAtLeft));
        Assertions.assertTrue(flippedSlantedLine.hasBetween(onAtLeft));

        Point<WholeNumber> onStrictlyBetween = pf.makePoint(3, 1);
        Assertions.assertTrue(slantedLine.hasBetween(onStrictlyBetween));
        Assertions.assertTrue(flippedSlantedLine.hasBetween(onStrictlyBetween));

        Point<WholeNumber> onAtRight = pf.makePoint(10, 3);
        Assertions.assertTrue(slantedLine.hasBetween(onAtRight));
        Assertions.assertTrue(flippedSlantedLine.hasBetween(onAtRight));

        Point<WholeNumber> onPastRight = pf.makePoint(7, 38);
        Assertions.assertFalse(slantedLine.hasBetween(onPastRight));
        Assertions.assertFalse(flippedSlantedLine.hasBetween(onPastRight));

        // com.Point is strictly above the line.
        Point<WholeNumber> abovePastLeft = pf.makePoint(-5, 0);
        Assertions.assertFalse(slantedLine.hasBetween(abovePastLeft));
        Assertions.assertFalse(flippedSlantedLine.hasBetween(abovePastLeft));

        Point<WholeNumber> aboveAtLeft = pf.makePoint(-8, 13);
        Assertions.assertTrue(slantedLine.hasBetween(aboveAtLeft));
        Assertions.assertTrue(flippedSlantedLine.hasBetween(aboveAtLeft));

        Point<WholeNumber> aboveStrictlyBetween = pf.makePoint(1, 1);
        Assertions.assertTrue(slantedLine.hasBetween(aboveStrictlyBetween));
        Assertions.assertTrue(flippedSlantedLine.hasBetween(aboveStrictlyBetween));

        Point<WholeNumber> aboveAtRight = pf.makePoint(6, 17);
        Assertions.assertTrue(slantedLine.hasBetween(aboveAtRight));
        Assertions.assertTrue(flippedSlantedLine.hasBetween(aboveAtRight));

        Point<WholeNumber> abovePastRight = pf.makePoint(10, 4);
        Assertions.assertFalse(slantedLine.hasBetween(abovePastRight));
        Assertions.assertFalse(flippedSlantedLine.hasBetween(abovePastRight));
    }

    /**
     * Test getting the squared distance from a point to a line segment.
     */
    @Test
    public void testDistanceSq() {
        LineSegment<WholeNumber> line = lf.makeLine(0, 0, 15, 3);
        // com.Point is at end point.
        Point<WholeNumber> endPoint = pf.makePoint(15, 3);
        Assertions.assertEquals(new WholeNumber(0), line.distanceSq(endPoint));

        // com.Point is on line, not at an endpoint.
        Point<WholeNumber> onLine = pf.makePoint(5, 1);
        Assertions.assertEquals(new WholeNumber(0), line.distanceSq(onLine));

        // com.Point is "between endpoints" but not on the line.
        LineSegment<Fraction<WholeNumber>> fractionLine = lf.makeFractionLine(
                0, 0, 15, 3);
        Point<Fraction<WholeNumber>> notOnButBetween = pf.makeFractionPoint(2, 7);
        Assertions.assertEquals(ff.makeFraction(1089, 26),
                fractionLine.distanceSq(notOnButBetween));

        // com.Point is not "between endpoints" and not on the line.
        Point<WholeNumber> notOnNorBetween = pf.makePoint(-30, -100);
        Assertions.assertEquals(new WholeNumber(30 * 30 + 100 * 100),
                line.distanceSq(notOnNorBetween));
    }

    @Test
    public void testIntersectsInfiniteLine() {
        Point<WholeNumber> p00 = pf.makePoint(0, 0);
        Point<WholeNumber> p10 = pf.makePoint(1, 0);
        Point<WholeNumber> p11 = pf.makePoint(1, 1);
        Point<WholeNumber> p23 = pf.makePoint(2, 3);

        // Line segment has exactly one endpoint on the infinite line.
        LineSegment<WholeNumber> s1 = lf.makeLine(5, 0, 9, 9);
        Assertions.assertTrue(s1.intersectsInfiniteLine(p00, p10));

        // Line segment has exactly one endpoint on the infinite line (the other endpoint).
        LineSegment<WholeNumber> s2 = lf.makeLine(9, 9, 5, 0);
        Assertions.assertTrue(s2.intersectsInfiniteLine(p00, p10));

        // Both endpoints lie on the infinite line.
        LineSegment<WholeNumber> s3 = lf.makeLine(-100, 0, 10000, 0);
        Assertions.assertTrue(s3.intersectsInfiniteLine(p00, p10));

        // Line segment intersects infinite line at a non-endpoint
        LineSegment<WholeNumber> s4 = lf.makeLine(-100, 2, 5, 0);
        Assertions.assertTrue(s4.intersectsInfiniteLine(p00, p23));

        // Line segment does not intersect the infinite line---segment below horizontal.
        LineSegment<WholeNumber> s5 = lf.makeLine(3, -1, 200, -2);
        Assertions.assertFalse(s5.intersectsInfiniteLine(p00, p10));

        // Line segment does not intersect the infinite line---segment above horizontal.
        LineSegment<WholeNumber> s6 = lf.makeLine(3, 1, 200, 2);
        Assertions.assertFalse(s6.intersectsInfiniteLine(p00, p10));

        // Line segment does not intersect the infinite line---segment left of vertical.
        LineSegment<WholeNumber> s7 = lf.makeLine(-1, 100, 0, 200);
        Assertions.assertFalse(s7.intersectsInfiniteLine(p10, p11));

        // Line segment does not intersect the infinite line---segment right of vertical.
        LineSegment<WholeNumber> s8 = lf.makeLine(3, 1, 200, 2);
        Assertions.assertFalse(s8.intersectsInfiniteLine(p11, p10));
    }

    @Test
    public void testIntersectsSemiInfiniteLine() {
        Point<WholeNumber> p00 = pf.makePoint(0, 0);
        Point<WholeNumber> p50 = pf.makePoint(5, 0);
        // Intersection is between the semi-infinite line points.
        LineSegment<WholeNumber> s1 = lf.makeLine(2, 1, 3, -2);
        Assertions.assertTrue(s1.intersectsSemiInfiniteLine(p00, p50));

        // Intersection is on the good side of the semi-infinite line points.
        LineSegment<WholeNumber> s2 = lf.makeLine(7, 1, 8, -2);
        Assertions.assertTrue(s2.intersectsSemiInfiniteLine(p00, p50));

        // No intersection---line segment is on the wrong side of the semi-infinite line points.
        LineSegment<WholeNumber> s3 = lf.makeLine(-3, 1, 1, -2);
        Assertions.assertFalse(s3.intersectsSemiInfiniteLine(p00, p50));

        // Same cases but semi-infinite line is in the other direction.
        Assertions.assertTrue(s1.intersectsSemiInfiniteLine(p50, p00));
        Assertions.assertFalse(s2.intersectsSemiInfiniteLine(p50, p00));
        Assertions.assertTrue(s3.intersectsSemiInfiniteLine(p50, p00));

        Point<WholeNumber> p53 = pf.makePoint(5, 3);
        Point<WholeNumber> p59 = pf.makePoint(5, 9);
        // Coincident cases:
        //  Line segment does not intersect semi-infinite line.
        LineSegment<WholeNumber> s4 = lf.makeLine(5, 0, 5, 2);
        Assertions.assertFalse(s4.intersectsSemiInfiniteLine(p53, p59));

        //  Line segment intersects at exactly one point.
        LineSegment<WholeNumber> s5 = lf.makeLine(5, 0, 5, 3);
        Assertions.assertTrue(s5.intersectsSemiInfiniteLine(p53, p59));

        //  Line segment has one endpoint off, one endpoint between semi-infinite line points.
        LineSegment<WholeNumber> s6 = lf.makeLine(5, 1, 5, 5);
        Assertions.assertTrue(s6.intersectsSemiInfiniteLine(p53, p59));

        //  Line segment has one endpoint off, one endpoint on the correct side of both semi-infinite line points.
        LineSegment<WholeNumber> s7 = lf.makeLine(5, -1, 5, 20);
        Assertions.assertTrue(s7.intersectsSemiInfiniteLine(p53, p59));

        //  Line segment has both endpoints between semi-infinite line points.
        LineSegment<WholeNumber> s8 = lf.makeLine(5, 4, 5, 5);
        Assertions.assertTrue(s8.intersectsSemiInfiniteLine(p53, p59));

        //  Line segment has one endpoint between semi-infinite line points, other on correct side of both.
        LineSegment<WholeNumber> s9 = lf.makeLine(5, 4, 5, 21);
        Assertions.assertTrue(s9.intersectsSemiInfiniteLine(p53, p59));

        //  Line segment has both endpoints on the correct side of both semi-infinite line points.
        LineSegment<WholeNumber> s10 = lf.makeLine(5, 20, 5, 21);
        Assertions.assertTrue(s10.intersectsSemiInfiniteLine(p53, p59));

        LineSegment<WholeNumber> s11 = lf.makeLine(-4, -3, -6, 0);
        Assertions.assertFalse(s11.intersectsSemiInfiniteLine(p00, p50));
    }
}
