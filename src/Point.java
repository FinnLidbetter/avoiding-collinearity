public class Point<T extends AbstractNumber<T>> {
    T x;
    T y;
    public Point(T x, T y) {
        this.x = x;
        this.y = y;
    }
    public T distanceSq(Point<T> p2) {
        T dx = x.subtract(p2.x);
        T dy = y.subtract(p2.y);
        T dxSq = dx.multiply(dx);
        T dySq = dy.multiply(dy);
        return dxSq.add(dySq);
    }
}
