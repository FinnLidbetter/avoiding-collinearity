import java.util.Comparator;

public class EventPointComparator<T extends AbstractNumber<T>> implements Comparator<EventPoint<T>> {
    Point<T> pivot;
    public EventPointComparator(Point<T> pivotPoint){
        pivot = pivotPoint;
    }

    public int compare(EventPoint<T> ep1, EventPoint<T> ep2) {
        if (ep1.point.equals(pivot) || ep2.point.equals(pivot)) {
            throw new RuntimeException("Cannot compare a point equal to the pivot.");
        }
        if (ep1.point.y.subtract(pivot.y).compareToZero() >= 0 && ep2.point.y.subtract(pivot.y).compareToZero() < 0) {
            // Relative to pivot, p1 is at or above the horizontal, and p2 is below.
            return -1;
        }
        if (ep2.point.y.subtract(pivot.y).compareToZero() >= 0 && ep1.point.y.subtract(pivot.y).compareToZero() < 0) {
            // Relative to pivot, p2 is at or above the horizontal, and p1 is below.
            return 1;
        }
        if (ep1.point.y.equals(pivot.y) && (ep2.point.y.equals(pivot.y))) {
            // If both points lie on the horizontal line through the pivot
            if (ep1.point.x.compareTo(pivot.x) > 0 && ep2.point.x.compareTo(pivot.x) < 0) {
                // p1 is to the right of the pivot and p2 is to the left
                return -1;
            }
            if (ep1.point.x.compareTo(pivot.x) < 0 && ep2.point.x.compareTo(pivot.x) > 0) {
                // p1 is to the left of the pivot and p2 is to the right
                return 1;
            }
            if (ep1.isStart && !ep2.isStart) {
                return -1;
            }
            if (!ep1.isStart && ep2.isStart) {
                return 1;
            }
            // The point closest to the pivot is "less".
            return pivot.distanceSq(ep1.point).compareTo(pivot.distanceSq(ep2.point));
        }
        Vector<T> v1 = new Vector<>(pivot, ep1.point);
        Vector<T> v2 = new Vector<>(pivot, ep2.point);
        int side = v1.cross(v2).compareToZero();
        if (side == 0) {
            if (ep1.isStart && !ep2.isStart) {
                return -1;
            }
            if (!ep1.isStart && ep2.isStart) {
                return 1;
            }
            return pivot.distanceSq(ep1.point).compareTo(pivot.distanceSq(ep2.point));
        }
        return -side;
    }
}
