public class Point<T extends AbstractNumber<T>> {
    Fraction<T> x;
    Fraction<T> y;
    public Point(Fraction<T> x, Fraction<T> y) {
        this.x = x;
        this.y = y;
    }
    public Fraction<T> distanceSq(Point p2) {
        Fraction<T> dx = x.subtract(p2.x);
        Fraction<T> dy = y.subtract(p2.y);
        Fraction<T> dxSq = dx.multiply(dx);
        Fraction<T> dySq = dy.multiply(dy);
        return dxSq.add(dySq);
    }
}
