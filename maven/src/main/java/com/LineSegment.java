package com;

import com.numbers.AbstractNumber;

public class LineSegment<T extends AbstractNumber<T>> {
    Point<T> p1, p2;

    public LineSegment(Point<T> p1, Point<T> p2) {
        this.p1 = p1;
        this.p2 = p2;
    }

    /**
     * Get the squared distance from this line segment to a point.
     *
     * For the case where the distance between the line segment and the point
     * is equal to the distance between the point and the infinite line through
     * the line segment's end points the formula found here is used:
     *      https://en.wikipedia.org/wiki/Distance_from_a_point_to_a_line
     * @param p: the point to get the distance to.
     * @return the squared distance from this line segment to the point.
     */
    public T distanceSq(Point<T> p) {
        if (hasBetween(p)) {
            T dx = p2.x.subtract(p1.x);
            T dy = p2.y.subtract(p1.y);
            T term1 = dy.multiply(p.x);
            T term2 = dx.multiply(p.y).additiveInverse();
            T term3 = (p2.x.multiply(p1.y)).subtract(p1.x.multiply(p2.y));
            T sum = term1.add(term2).add(term3);
            T numerator = sum.multiply(sum);
            T denominator = (dx.multiply(dx)).add(dy.multiply(dy));
            return numerator.divide(denominator);
        }
        T distSq1 = p.distanceSq(p1);
        T distSq2 = p.distanceSq(p2);
        if (distSq1.compareTo(distSq2) < 0) {
            return distSq1;
        }
        return distSq2;
    }

    /**
     * Return True iff p lies on or between the infinite lines perpendicular
     * to this line segment through p1 and p2.
     * @param p: the point to evaluate.
     * @return boolean indicating if p lies between the endpoints of this
     *  line segment.
     */
    public boolean hasBetween(Point<T> p) {
        Vector<T> p1ToP2 = new Vector<>(p2.x.subtract(p1.x), p2.y.subtract(p1.y));
        Vector<T> p1PerpendicularVector = p1ToP2.perpendicular();
        T p2SideOfP1 = p1PerpendicularVector.cross(p1ToP2);
        Vector<T> p1ToP = new Vector<>(p.x.subtract(p1.x), p.y.subtract(p1.y));
        T pSideOfP1 = p1PerpendicularVector.cross(p1ToP);
        if (p2SideOfP1.compareToZero()
                != pSideOfP1.compareToZero())
            return pSideOfP1.compareToZero() == 0;
        Vector<T> p2ToP1 = p1ToP2.additiveInverse();
        Vector<T> p2PerpendicularVector = p2ToP1.perpendicular();
        T p1SideOfP2 = p2PerpendicularVector.cross(p2ToP1);
        Vector<T> p2ToP = new Vector<>(p.x.subtract(p2.x), p.y.subtract(p2.y));
        T pSideOfP2 = p2PerpendicularVector.cross(p2ToP);
        if (p1SideOfP2.compareToZero() != pSideOfP2.compareToZero())
            return pSideOfP2.compareToZero() == 0;
        return true;
    }

    public boolean intersectsInfiniteLine(Point<T> linePoint1, Point<T> linePoint2) {
        Vector<T> lineVector = new Vector<>(linePoint1, linePoint2);
        Vector<T> v1 = new Vector<>(linePoint1, p1);
        Vector<T> v2 = new Vector<>(linePoint1, p2);
        int v1Side = lineVector.cross(v1).compareToZero();
        int v2Side = lineVector.cross(v2).compareToZero();
        if (v1Side == 0 || v2Side == 0 || v1Side != v2Side)
            return true;
        return false;
    }

    public boolean intersectsSemiInfiniteLine(Point<T> linePoint1, Point<T> linePoint2) {
        if (!intersectsInfiniteLine(linePoint1, linePoint2)) {
            return false;
        }
        Vector<T> lineVector = new Vector<>(linePoint1, linePoint2);
        Vector<T> v1 = new Vector<>(linePoint1, p1);
        Vector<T> v2 = new Vector<>(linePoint1, p2);
        T crossV12 = v1.cross(v2);
        if (crossV12.compareToZero() == 0) {
            // p1, p2, and linePoint1 are on the same line.
            if (hasBetween(linePoint1)) {
                // Handle the non-coincident case and the case where the line
                // segment contains linePoint1.
                return true;
            }
            return lineVector.dot(v1).compareToZero() >= 0;
        } else if (crossV12.compareToZero() < 0) {
            v1 = v2;
        }
        T crossv1lineVector = v1.cross(lineVector);
        if (crossv1lineVector.compareToZero() > 0) {
            return true;
        } else if (crossv1lineVector.compareToZero() == 0) {
            return lineVector.dot(v1).compareToZero() > 0;
        }
        return false;
    }

    public String toString() {
        return String.format("[%s, %s]", p1, p2);
    }
}
