import org.junit.Assert;
import org.junit.Test;

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

        // Point strictly below line cases.
        Point<WholeNumber> belowPastLeft = pf.makePoint(3, 14);
        Assert.assertFalse(horizontalLine.hasBetween(belowPastLeft));
        Assert.assertFalse(flippedHorizontalLine.hasBetween(belowPastLeft));

        Point<WholeNumber> belowAtLeft = pf.makePoint(4, 0);
        Assert.assertTrue(horizontalLine.hasBetween(belowAtLeft));
        Assert.assertTrue(flippedHorizontalLine.hasBetween(belowAtLeft));

        Point<WholeNumber> belowStrictlyBetween = pf.makePoint(7, -1);
        Assert.assertTrue(horizontalLine.hasBetween(belowStrictlyBetween));
        Assert.assertTrue(flippedHorizontalLine.hasBetween(belowStrictlyBetween));

        Point<WholeNumber> belowAtRight = pf.makePoint(17, 0);
        Assert.assertTrue(horizontalLine.hasBetween(belowAtRight));
        Assert.assertTrue(flippedHorizontalLine.hasBetween(belowAtRight));

        Point<WholeNumber> belowPastRight = pf.makePoint(18, 14);
        Assert.assertFalse(horizontalLine.hasBetween(belowPastRight));
        Assert.assertFalse(flippedHorizontalLine.hasBetween(belowPastRight));

        // Point neither strictly above nor strictly below the line.
        Point<WholeNumber> onPastLeft = pf.makePoint(3, 15);
        Assert.assertFalse(horizontalLine.hasBetween(onPastLeft));
        Assert.assertFalse(flippedHorizontalLine.hasBetween(onPastLeft));

        Point<WholeNumber> onAtLeft = pf.makePoint(4, 15);
        Assert.assertTrue(horizontalLine.hasBetween(onAtLeft));
        Assert.assertTrue(flippedHorizontalLine.hasBetween(onAtLeft));

        Point<WholeNumber> onStrictlyBetween = pf.makePoint(7, 15);
        Assert.assertTrue(horizontalLine.hasBetween(onStrictlyBetween));
        Assert.assertTrue(flippedHorizontalLine.hasBetween(onStrictlyBetween));

        Point<WholeNumber> onAtRight = pf.makePoint(17, 15);
        Assert.assertTrue(horizontalLine.hasBetween(onAtRight));
        Assert.assertTrue(flippedHorizontalLine.hasBetween(onAtRight));

        Point<WholeNumber> onPastRight = pf.makePoint(18, 15);
        Assert.assertFalse(horizontalLine.hasBetween(onPastRight));
        Assert.assertFalse(flippedHorizontalLine.hasBetween(onPastRight));

        // Point is strictly above the line.
        Point<WholeNumber> abovePastLeft = pf.makePoint(3, 16);
        Assert.assertFalse(horizontalLine.hasBetween(abovePastLeft));
        Assert.assertFalse(flippedHorizontalLine.hasBetween(abovePastLeft));

        Point<WholeNumber> aboveAtLeft = pf.makePoint(4, 17);
        Assert.assertTrue(horizontalLine.hasBetween(aboveAtLeft));
        Assert.assertTrue(flippedHorizontalLine.hasBetween(aboveAtLeft));

        Point<WholeNumber> aboveStrictlyBetween = pf.makePoint(7, 200);
        Assert.assertTrue(horizontalLine.hasBetween(aboveStrictlyBetween));
        Assert.assertTrue(flippedHorizontalLine.hasBetween(aboveStrictlyBetween));

        Point<WholeNumber> aboveAtRight = pf.makePoint(17, 34);
        Assert.assertTrue(horizontalLine.hasBetween(aboveAtRight));
        Assert.assertTrue(flippedHorizontalLine.hasBetween(aboveAtRight));

        Point<WholeNumber> abovePastRight = pf.makePoint(18, 16);
        Assert.assertFalse(horizontalLine.hasBetween(abovePastRight));
        Assert.assertFalse(flippedHorizontalLine.hasBetween(abovePastRight));
    }

    /**
     * Test checking whether a point lies "between the endpoints" of a
     * vertical line.
     */
    private void testVerticalLineCases() {
        LineSegment<WholeNumber> verticalLine = lf.makeLine(-3, 15, -3, 36);
        LineSegment<WholeNumber> flippedVerticalLine = lf.makeLine(
                -3, 36, -3, 15);

        // Point strictly left of line cases.
        Point<WholeNumber> leftPastBottom = pf.makePoint(-5, 14);
        Assert.assertFalse(verticalLine.hasBetween(leftPastBottom));
        Assert.assertFalse(flippedVerticalLine.hasBetween(leftPastBottom));

        Point<WholeNumber> leftAtBottom = pf.makePoint(-6, 15);
        Assert.assertTrue(verticalLine.hasBetween(leftAtBottom));
        Assert.assertTrue(flippedVerticalLine.hasBetween(leftAtBottom));

        Point<WholeNumber> leftStrictlyBetween = pf.makePoint(-7, 17);
        Assert.assertTrue(verticalLine.hasBetween(leftStrictlyBetween));
        Assert.assertTrue(flippedVerticalLine.hasBetween(leftStrictlyBetween));

        Point<WholeNumber> leftAtTop = pf.makePoint(-10, 36);
        Assert.assertTrue(verticalLine.hasBetween(leftAtTop));
        Assert.assertTrue(flippedVerticalLine.hasBetween(leftAtTop));

        Point<WholeNumber> leftPastTop = pf.makePoint(-4, 37);
        Assert.assertFalse(verticalLine.hasBetween(leftPastTop));
        Assert.assertFalse(flippedVerticalLine.hasBetween(leftPastTop));

        // Point is neither strictly above nor strictly below the line.
        Point<WholeNumber> onPastBottom = pf.makePoint(-3, -100);
        Assert.assertFalse(verticalLine.hasBetween(onPastBottom));
        Assert.assertFalse(flippedVerticalLine.hasBetween(onPastBottom));

        Point<WholeNumber> onAtBottom = pf.makePoint(-3, 15);
        Assert.assertTrue(verticalLine.hasBetween(onAtBottom));
        Assert.assertTrue(flippedVerticalLine.hasBetween(onAtBottom));

        Point<WholeNumber> onStrictlyBetween = pf.makePoint(-3, 20);
        Assert.assertTrue(verticalLine.hasBetween(onStrictlyBetween));
        Assert.assertTrue(flippedVerticalLine.hasBetween(onStrictlyBetween));

        Point<WholeNumber> onAtTop = pf.makePoint(-3, 36);
        Assert.assertTrue(verticalLine.hasBetween(onAtTop));
        Assert.assertTrue(flippedVerticalLine.hasBetween(onAtTop));

        Point<WholeNumber> onPastTop = pf.makePoint(-3, 100);
        Assert.assertFalse(verticalLine.hasBetween(onPastTop));
        Assert.assertFalse(flippedVerticalLine.hasBetween(onPastTop));

        // Point is strictly right of the line cases.
        Point<WholeNumber> rightPastBottom = pf.makePoint(-2, -10);
        Assert.assertFalse(verticalLine.hasBetween(rightPastBottom));
        Assert.assertFalse(flippedVerticalLine.hasBetween(rightPastBottom));

        Point<WholeNumber> rightAtBottom = pf.makePoint(-1, 15);
        Assert.assertTrue(verticalLine.hasBetween(rightAtBottom));
        Assert.assertTrue(flippedVerticalLine.hasBetween(rightAtBottom));

        Point<WholeNumber> rightStrictlyBetween = pf.makePoint(0, 21);
        Assert.assertTrue(verticalLine.hasBetween(rightStrictlyBetween));
        Assert.assertTrue(flippedVerticalLine.hasBetween(rightStrictlyBetween));

        Point<WholeNumber> rightAtTop = pf.makePoint(150, 36);
        Assert.assertTrue(verticalLine.hasBetween(rightAtTop));
        Assert.assertTrue(flippedVerticalLine.hasBetween(rightAtTop));

        Point<WholeNumber> rightPastTop = pf.makePoint(-2, 37);
        Assert.assertFalse(verticalLine.hasBetween(rightPastTop));
        Assert.assertFalse(flippedVerticalLine.hasBetween(rightPastTop));
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

        // Point strictly below line cases.
        Point<WholeNumber> belowPastLeft = pf.makePoint(-3, -1000);
        Assert.assertFalse(slantedLine.hasBetween(belowPastLeft));
        Assert.assertFalse(flippedSlantedLine.hasBetween(belowPastLeft));

        Point<WholeNumber> belowAtLeft = pf.makePoint(4, -29);
        Assert.assertTrue(slantedLine.hasBetween(belowAtLeft));
        Assert.assertTrue(flippedSlantedLine.hasBetween(belowAtLeft));

        Point<WholeNumber> belowStrictlyBetween = pf.makePoint(2, 0);
        Assert.assertTrue(slantedLine.hasBetween(belowStrictlyBetween));
        Assert.assertTrue(flippedSlantedLine.hasBetween(belowStrictlyBetween));

        Point<WholeNumber> belowAtRight = pf.makePoint(-4, 12);
        Assert.assertTrue(slantedLine.hasBetween(belowAtRight));
        Assert.assertTrue(flippedSlantedLine.hasBetween(belowAtRight));

        Point<WholeNumber> belowPastRight = pf.makePoint(12, 0);
        Assert.assertFalse(slantedLine.hasBetween(belowPastRight));
        Assert.assertFalse(flippedSlantedLine.hasBetween(belowPastRight));

        // Point neither strictly above nor strictly below the line.
        Point<WholeNumber> onPastLeft = pf.makePoint(-11, -3);
        Assert.assertFalse(slantedLine.hasBetween(onPastLeft));
        Assert.assertFalse(flippedSlantedLine.hasBetween(onPastLeft));

        Point<WholeNumber> onAtLeft = pf.makePoint(-4, -1);
        Assert.assertTrue(slantedLine.hasBetween(onAtLeft));
        Assert.assertTrue(flippedSlantedLine.hasBetween(onAtLeft));

        Point<WholeNumber> onStrictlyBetween = pf.makePoint(3, 1);
        Assert.assertTrue(slantedLine.hasBetween(onStrictlyBetween));
        Assert.assertTrue(flippedSlantedLine.hasBetween(onStrictlyBetween));

        Point<WholeNumber> onAtRight = pf.makePoint(10, 3);
        Assert.assertTrue(slantedLine.hasBetween(onAtRight));
        Assert.assertTrue(flippedSlantedLine.hasBetween(onAtRight));

        Point<WholeNumber> onPastRight = pf.makePoint(7, 38);
        Assert.assertFalse(slantedLine.hasBetween(onPastRight));
        Assert.assertFalse(flippedSlantedLine.hasBetween(onPastRight));

        // Point is strictly above the line.
        Point<WholeNumber> abovePastLeft = pf.makePoint(-5, 0);
        Assert.assertFalse(slantedLine.hasBetween(abovePastLeft));
        Assert.assertFalse(flippedSlantedLine.hasBetween(abovePastLeft));

        Point<WholeNumber> aboveAtLeft = pf.makePoint(-8, 13);
        Assert.assertTrue(slantedLine.hasBetween(aboveAtLeft));
        Assert.assertTrue(flippedSlantedLine.hasBetween(aboveAtLeft));

        Point<WholeNumber> aboveStrictlyBetween = pf.makePoint(1, 1);
        Assert.assertTrue(slantedLine.hasBetween(aboveStrictlyBetween));
        Assert.assertTrue(flippedSlantedLine.hasBetween(aboveStrictlyBetween));

        Point<WholeNumber> aboveAtRight = pf.makePoint(6, 17);
        Assert.assertTrue(slantedLine.hasBetween(aboveAtRight));
        Assert.assertTrue(flippedSlantedLine.hasBetween(aboveAtRight));

        Point<WholeNumber> abovePastRight = pf.makePoint(10, 4);
        Assert.assertFalse(slantedLine.hasBetween(abovePastRight));
        Assert.assertFalse(flippedSlantedLine.hasBetween(abovePastRight));
    }

    /**
     * Test getting the squared distance from a point to a line segment.
     */
    @Test
    public void testDistanceSq() {
        LineSegment<WholeNumber> line = lf.makeLine(0, 0, 15, 3);
        // Point is at end point.
        Point<WholeNumber> endPoint = pf.makePoint(15, 3);
        Assert.assertEquals(new WholeNumber(0), line.distanceSq(endPoint));

        // Point is on line, not at an endpoint.
        Point<WholeNumber> onLine = pf.makePoint(5, 1);
        Assert.assertEquals(new WholeNumber(0), line.distanceSq(onLine));

        // Point is "between endpoints" but not on the line.
        LineSegment<Fraction<WholeNumber>> fractionLine = lf.makeFractionLine(
                0, 0, 15, 3);
        Point<Fraction<WholeNumber>> notOnButBetween = pf.makeFractionPoint(2, 7);
        Assert.assertEquals(ff.makeFraction(1089, 26),
                fractionLine.distanceSq(notOnButBetween));

        // Point is not "between endpoints" and not on the line.
        Point<WholeNumber> notOnNorBetween = pf.makePoint(-30, -100);
        Assert.assertEquals(new WholeNumber(30 * 30 + 100 * 100),
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
        Assert.assertTrue(s1.intersectsInfiniteLine(p00, p10));

        // Line segment has exactly one endpoint on the infinite line (the other endpoint).
        LineSegment<WholeNumber> s2 = lf.makeLine(9, 9, 5, 0);
        Assert.assertTrue(s2.intersectsInfiniteLine(p00, p10));

        // Both endpoints lie on the infinite line.
        LineSegment<WholeNumber> s3 = lf.makeLine(-100, 0, 10000, 0);
        Assert.assertTrue(s3.intersectsInfiniteLine(p00, p10));

        // Line segment intersects infinite line at a non-endpoint
        LineSegment<WholeNumber> s4 = lf.makeLine(-100, 2, 5, 0);
        Assert.assertTrue(s4.intersectsInfiniteLine(p00, p23));

        // Line segment does not intersect the infinite line---segment below horizontal.
        LineSegment<WholeNumber> s5 = lf.makeLine(3, -1, 200, -2);
        Assert.assertFalse(s5.intersectsInfiniteLine(p00, p10));

        // Line segment does not intersect the infinite line---segment above horizontal.
        LineSegment<WholeNumber> s6 = lf.makeLine(3, 1, 200, 2);
        Assert.assertFalse(s6.intersectsInfiniteLine(p00, p10));

        // Line segment does not intersect the infinite line---segment left of vertical.
        LineSegment<WholeNumber> s7 = lf.makeLine(-1, 100, 0, 200);
        Assert.assertFalse(s7.intersectsInfiniteLine(p10, p11));

        // Line segment does not intersect the infinite line---segment right of vertical.
        LineSegment<WholeNumber> s8 = lf.makeLine(3, 1, 200, 2);
        Assert.assertFalse(s8.intersectsInfiniteLine(p11, p10));
    }

    @Test
    public void testIntersectsSemiInfiniteLine() {
        Point<WholeNumber> p00 = pf.makePoint(0, 0);
        Point<WholeNumber> p50 = pf.makePoint(5, 0);
        // Intersection is between the semi-infinite line points.
        LineSegment<WholeNumber> s1 = lf.makeLine(2, 1, 3, -2);
        Assert.assertTrue(s1.intersectsSemiInfiniteLine(p00, p50));

        // Intersection is on the good side of the semi-infinite line points.
        LineSegment<WholeNumber> s2 = lf.makeLine(7, 1, 8, -2);
        Assert.assertTrue(s2.intersectsSemiInfiniteLine(p00, p50));

        // No intersection---line segment is on the wrong side of the semi-infinite line points.
        LineSegment<WholeNumber> s3 = lf.makeLine(-3, 1, 1, -2);
        Assert.assertFalse(s3.intersectsSemiInfiniteLine(p00, p50));

        // Same cases but semi-infinite line is in the other direction.
        Assert.assertTrue(s1.intersectsSemiInfiniteLine(p50, p00));
        Assert.assertFalse(s2.intersectsSemiInfiniteLine(p50, p00));
        Assert.assertTrue(s3.intersectsSemiInfiniteLine(p50, p00));

        Point<WholeNumber> p53 = pf.makePoint(5, 3);
        Point<WholeNumber> p59 = pf.makePoint(5, 9);
        // Coincident cases:
        //  Line segment does not intersect semi-infinite line.
        LineSegment<WholeNumber> s4 = lf.makeLine(5, 0, 5, 2);
        Assert.assertFalse(s4.intersectsSemiInfiniteLine(p53, p59));

        //  Line segment intersects at exactly one point.
        LineSegment<WholeNumber> s5 = lf.makeLine(5, 0, 5, 3);
        Assert.assertTrue(s5.intersectsSemiInfiniteLine(p53, p59));

        //  Line segment has one endpoint off, one endpoint between semi-infinite line points.
        LineSegment<WholeNumber> s6 = lf.makeLine(5, 1, 5, 5);
        Assert.assertTrue(s6.intersectsSemiInfiniteLine(p53, p59));

        //  Line segment has one endpoint off, one endpoint on the correct side of both semi-infinite line points.
        LineSegment<WholeNumber> s7 = lf.makeLine(5, -1, 5, 20);
        Assert.assertTrue(s7.intersectsSemiInfiniteLine(p53, p59));

        //  Line segment has both endpoints between semi-infinite line points.
        LineSegment<WholeNumber> s8 = lf.makeLine(5, 4, 5, 5);
        Assert.assertTrue(s8.intersectsSemiInfiniteLine(p53, p59));

        //  Line segment has one endpoint between semi-infinite line points, other on correct side of both.
        LineSegment<WholeNumber> s9 = lf.makeLine(5, 4, 5, 21);
        Assert.assertTrue(s9.intersectsSemiInfiniteLine(p53, p59));

        //  Line segment has both endpoints on the correct side of both semi-infinite line points.
        LineSegment<WholeNumber> s10 = lf.makeLine(5, 20, 5, 21);
        Assert.assertTrue(s10.intersectsSemiInfiniteLine(p53, p59));
    }
}
