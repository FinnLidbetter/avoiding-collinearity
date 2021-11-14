public class RadialPoint<T extends AbstractNumber<T>> {
    Point<T> point;
    boolean isEnter;

    public RadialPoint(Point<T> point, boolean isEnter) {
        this.point = point;
        this.isEnter = isEnter;
    }
}
