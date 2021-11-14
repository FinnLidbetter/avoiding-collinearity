import java.util.Comparator;

public class PointComparator<T extends AbstractNumber<T>> implements Comparator<Point<T>> {
    Point<T> pivot;
    public PointComparator(Point<T> pivotPoint){
        pivot = pivotPoint;
    }

    public int compare(Point<T> p1, Point<T> p2) {
        if (p1.equals(pivot) || p2.equals(pivot)) {
            throw new RuntimeException("Cannot compare a point equal to the pivot.");
        }
        if (p1.y.subtract(pivot.y).compareToZero() >= 0 && p2.y.subtract(pivot.y).compareToZero() < 0) {
            // Relative to pivot, p1 is at or above the horizontal, and p2 is below.
            return -1;
        }
        if (p2.y.subtract(pivot.y).compareToZero() >= 0 && p1.y.subtract(pivot.y).compareToZero() < 0) {
            // Relative to pivot, p2 is at or above the horizontal, and p1 is below.
            return 1;
        }
        if (p1.y.equals(pivot.y) && (p2.y.equals(pivot.y))) {
            // If both points lie on the horizontal line through the pivot
            if (p1.x.compareTo(pivot.x) > 0 && p2.x.compareTo(pivot.x) < 0) {
                // p1 is to the right of the pivot and p2 is to the left
                return -1;
            }
            if (p1.x.compareTo(pivot.x) < 0 && p2.x.compareTo(pivot.x) > 0) {
                // p1 is to the left of the pivot and p2 is to the right
                return 1;
            }
            // The point closest to the pivot is "less".
            return pivot.distanceSq(p1).compareTo(pivot.distanceSq(p2));
        }
        Vector<T> v1 = new Vector<T>(pivot, p1);
        Vector<T> v2 = new Vector<T>(pivot, p2);
        int side = v1.cross(v2).compareToZero();
        if (side == 0) {
            return pivot.distanceSq(p1).compareTo(pivot.distanceSq(p2));
        }
        return side;
    }
}