import java.util.ArrayList;
import java.util.List;

/**
 * The methods in this class assume that the points form a Trapezoid.
 *
 * @param <T>: Some AbstractNumber.
 */
public class Trapezoid<T extends AbstractNumber<T>> {
   List<Point<T>> vertices;
   List<LineSegment<T>> sides;
   public Trapezoid(Point<T> p0, Point<T> p1, Point<T> p2, Point<T> p3) {
       vertices = new ArrayList<>(4);
       vertices.add(p0);
       vertices.add(p1);
       vertices.add(p2);
       vertices.add(p3);
       sides = new ArrayList<>(4);
       sides.add(new LineSegment<>(p0, p1));
       sides.add(new LineSegment<>(p1, p2));
       sides.add(new LineSegment<>(p2, p3));
       sides.add(new LineSegment<>(p3, p0));
   }

    /**
     * Return the shortest distance squared from the boundary of the Trapezoid
     * to the provided point.
     *
     * @param p: the point to get the squared distance to.
     * @return the squared distance from point p to the trapezoid.
     */
   public T distanceSq(Point<T> p) {
       T minDistSq = null;
       for (LineSegment<T> side: sides) {
           T currDistSq = side.distanceSq(p);
           if (minDistSq == null || currDistSq.compareTo(minDistSq) < 0) {
               minDistSq = currDistSq;
           }
       }
       return minDistSq;
   }

    /**
     * Get the largest distance squared between this trapezoid and another.
     *
     * The largest distance is always between two vertices.
     * @param t2: another trapezoid.
     * @return The largest straight line distance squared between all pairs
     *  of points inside the regions defined by the two trapezoids.
     */
   public T maxDistanceSq(Trapezoid<T> t2) {
       T maxDistSq = null;
       for (Point<T> p1: vertices) {
           for (Point<T> p2: t2.vertices) {
               T currDistSq = p1.distanceSq(p2);
               if (maxDistSq == null || currDistSq.compareTo(maxDistSq) > 0) {
                   maxDistSq = currDistSq;
               }
           }
       }
       return maxDistSq;
   }
   /**
    * Get the smallest distance between this trapezoid and another trapezoid.
    *
    * This assumes that the area of the intersection of the trapezoids is 0.
    * @param t2: another trapezoid.
    * @return The smallest distance between all pairs of points inside the
    *  regions defined by the two trapezoids.
    */
   public T minDistanceSq(Trapezoid<T> t2) {
       T minDistSq = null;
       for (Point<T> p: vertices) {
           for (LineSegment<T> l: t2.sides) {
               T currDistSq = l.distanceSq(p);
               if (minDistSq == null || currDistSq.compareTo(minDistSq) < 0) {
                   minDistSq = currDistSq;
               }
           }
       }
       for (Point<T> p: t2.vertices) {
           for (LineSegment<T> l: sides) {
               T currDistSq = l.distanceSq(p);
               if (minDistSq == null || currDistSq.compareTo(minDistSq) < 0) {
                   minDistSq = currDistSq;
               }
           }
       }
       return minDistSq;
   }

    /**
     * Determine if an infinite line intersects this trapezoid.
     *
     * @param linePoint1: a point on an infinite line.
     * @param linePoint2: a distinct point defining an infinite line.
     * @return true iff the infinite line through linePoint1 and linePoint2
     * intersects this trapezoid.
     */
   public boolean intersectsInfiniteLine(Point<T> linePoint1, Point<T> linePoint2) {
       if (linePoint1.equals(linePoint2)) {
           throw new IllegalArgumentException(
                   "Equal points do not define a unique line.");
       }
       Vector<T> lineVector = new Vector<>(linePoint1, linePoint2);
       for (LineSegment<T> side: sides) {
           Vector<T> v1 = new Vector<>(linePoint1, side.p1);
           Vector<T> v2 = new Vector<>(linePoint1, side.p2);
           int v1Side = lineVector.cross(v1).compareToZero();
           int v2Side = lineVector.cross(v2).compareToZero();
           if (v1Side == 0 || v2Side == 0 || v1Side != v2Side)
               return true;
       }
       return false;
   }
}
