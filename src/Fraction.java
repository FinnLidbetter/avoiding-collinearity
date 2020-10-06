public class Fraction<T extends AbstractNumber<T>> extends AbstractNumber<Fraction<T>> {

    T num, denom;

    public Fraction(T numerator, T denominator) {
        this.num = numerator;
        this.denom = denominator;
    }

    public Fraction<T> add(Fraction<T> f2) {
        Fraction<T> result = new Fraction<T>(
                num.multiply(f2.denom).add(f2.num.multiply(denom)),
                denom.multiply(f2.denom));
        result.normalize();
        return result;
    }

    public Fraction<T> additiveInverse() {
        Fraction<T> result = new Fraction<T>(num.additiveInverse(), denom);
        result.normalize();
        return result;
    }

    public Fraction<T> subtract(Fraction<T> f2) {
        Fraction<T> result = new Fraction<T>(
                num.multiply(f2.denom).add(f2.num.multiply(denom)),
                denom.multiply(f2.denom));
        result.normalize();
        return result;
    }

    public Fraction<T> multiply(Fraction<T> f2) {
        Fraction<T> result = new Fraction<T>(
                num.multiply(f2.num), denom.multiply(f2.denom));
        result.normalize();
        return result;
    }

    public Fraction<T> divide(Fraction<T> f2) {
        return this.multiply(f2.reciprocal());
    }

    public Fraction<T> reciprocal() {
        Fraction<T> result = new Fraction<T>(denom, num);
        result.normalize();
        return result;
    }

    public void normalize() {
        if (denom.compareToZero() < 0) {
            num = num.additiveInverse();
            denom = denom.additiveInverse();
        }
        T gcf = num.gcd(denom);
        num = num.divide(gcf);
        denom = denom.divide(gcf);
    }

    @Override
    public int compareTo(Fraction<T> f2) {
        return num.multiply(f2.denom).compareTo(denom.multiply(f2.num));
    }

    @Override
    public int compareToZero() {
        return num.compareToZero();
    }

    @Override
    public Fraction<T> gcd(Fraction<T> f2) {
        return f2.multiply(f2.reciprocal());
    }
}
