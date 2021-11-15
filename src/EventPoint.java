public class EventPoint<T extends AbstractNumber<T>> {
    Point<T> point;
    boolean isStart;

    public EventPoint(Point<T> point, boolean isStart) {
        this.point = point;
        this.isStart = isStart;
    }
}
