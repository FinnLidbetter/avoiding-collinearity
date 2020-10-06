public class Trapezoid {
   Point[] vertices;
   LineSegment[] sides;
   public Trapezoid(Point p1, Point p2, Point p3, Point p4) {
       vertices = new Point[4];
       vertices[0] = p1;
       vertices[1] = p2;
       vertices[2] = p3;
       vertices[3] = p4;
       sides = new LineSegment[4];
       sides[0] = new LineSegment(p1, p2);
       sides[1] = new LineSegment(p2, p3);
       sides[2] = new LineSegment(p3, p4);
       sides[3] = new LineSegment(p4, p1);
   }

    /**
     * Return the shortest distance squared from the Trapezoid to
     * the provided point p.
     * @param p: the point to get the squared distance to.
     * @return the squared distance from point p to the trapezoid.
     */
   public Fraction distanceSq(Point p) {
       Fraction minDistSq = null;
       for (LineSegment side: sides) {
           Fraction currDistSq = side.distanceSq(p);
           if (minDistSq == null || currDistSq.compareTo(minDistSq) < 0) {
               minDistSq = currDistSq;
           }
       }
       return minDistSq;
   }

   public Fraction maxDistanceSq(Trapezoid t2) {
       Fraction maxDistSq = null;
       for (Point p: vertices) {
           for (LineSegment l: t2.sides) {
               Fraction currDistSq = l.distanceSq(p);
               if (maxDistSq == null || currDistSq.compareTo(maxDistSq) > 0) {
                   maxDistSq = currDistSq;
               }
           }
       }
       return maxDistSq;
   }

   public Fraction minDistanceSq(Trapezoid t2) {
       Fraction minDistSq = null;
       for (Point p: vertices) {
           for (LineSegment l: t2.sides) {
               Fraction currDistSq = l.distanceSq(p);
               if (minDistSq == null || currDistSq.compareTo(minDistSq) < 0) {
                   minDistSq = currDistSq;
               }
           }
       }
       return minDistSq;
   }
}
