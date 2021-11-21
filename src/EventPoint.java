public class EventPoint<T extends AbstractNumber<T>> {
    Point<T> point;
    int trapezoidIndex;
    boolean isStart;

    public EventPoint(Point<T> point, int trapezoidIndex, boolean isStart) {
        this.point = point;
        this.trapezoidIndex = trapezoidIndex;
        this.isStart = isStart;
    }
}
