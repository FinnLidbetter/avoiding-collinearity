import java.util.ArrayList;
import java.util.List;

public class Trapezoid<T extends AbstractNumber<T>> {
   List<Point<T>> vertices;
   List<LineSegment<T>> sides;
   public Trapezoid(Point<T> p1, Point<T> p2, Point<T> p3, Point<T> p4) {
       vertices = new ArrayList<Point<T>>(4);
       vertices.add(p1);
       vertices.add(p2);
       vertices.add(p3);
       vertices.add(p4);
       sides = new ArrayList<LineSegment<T>>(4);
       sides.add(new LineSegment<T>(p1, p2));
       sides.add(new LineSegment<T>(p2, p3));
       sides.add(new LineSegment<T>(p3, p4));
       sides.add(new LineSegment<T>(p4, p1));
   }

    /**
     * Return the shortest distance squared from the Trapezoid to
     * the provided point p.
     * @param p: the point to get the squared distance to.
     * @return the squared distance from point p to the trapezoid.
     */
   public Fraction<T> distanceSq(Point<T> p) {
       Fraction<T> minDistSq = null;
       for (LineSegment<T> side: sides) {
           Fraction<T> currDistSq = side.distanceSq(p);
           if (minDistSq == null || currDistSq.compareTo(minDistSq) < 0) {
               minDistSq = currDistSq;
           }
       }
       return minDistSq;
   }

   public Fraction<T> maxDistanceSq(Trapezoid<T> t2) {
       Fraction<T> maxDistSq = null;
       for (Point<T> p: vertices) {
           for (LineSegment<T> l: t2.sides) {
               Fraction<T> currDistSq = l.distanceSq(p);
               if (maxDistSq == null || currDistSq.compareTo(maxDistSq) > 0) {
                   maxDistSq = currDistSq;
               }
           }
       }
       return maxDistSq;
   }

   public Fraction<T> minDistanceSq(Trapezoid<T> t2) {
       Fraction<T> minDistSq = null;
       for (Point<T> p: vertices) {
           for (LineSegment<T> l: t2.sides) {
               Fraction<T> currDistSq = l.distanceSq(p);
               if (minDistSq == null || currDistSq.compareTo(minDistSq) < 0) {
                   minDistSq = currDistSq;
               }
           }
       }
       return minDistSq;
   }
}
