public class Vector<T extends AbstractNumber<T>> {
    Fraction<T> x;
    Fraction<T> y;
    public Vector(Fraction<T> x, Fraction<T> y) {
        this.x = x;
        this.y = y;
    }
    public Vector<T> add(Vector<T> v2) {
        return new Vector<T>(x.add(v2.x), y.add(v2.y));
    }
    public Vector<T> subtract(Vector<T> v2) {
        return new Vector<T>(x.subtract(v2.x), y.subtract(v2.y));
    }
    public Vector<T> additiveInverse() {
        return new Vector<T>(x.additiveInverse(), y.additiveInverse());
    }
    public Vector<T> scale(Fraction<T> m) {
        return new Vector<T>(x.multiply(m), y.multiply(m));
    }
    public Fraction<T> cross(Vector<T> v2) {
        return x.multiply(v2.y).subtract(y.multiply(v2.x));
    }
    public Vector<T> perpendicular() {
        return new Vector<T>(y, x.additiveInverse());
    }
}
