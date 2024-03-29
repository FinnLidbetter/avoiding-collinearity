package com.numbers;

/**
 * Class for precisely handling Fractions of AbstractNumbers.
 * @param <T> a subclass of com.numbers.AbstractNumber.
 */
public class Fraction<T extends AbstractNumber<T>> extends AbstractNumber<Fraction<T>> {

    T num, denom;

    public Fraction(T numerator, T denominator) {
        this.num = numerator;
        this.denom = denominator;
    }

    public Fraction<T> add(Fraction<T> f2) {
        Fraction<T> result = new Fraction<>(
                num.multiply(f2.denom).add(f2.num.multiply(denom)),
                denom.multiply(f2.denom));
        result.normalize();
        return result;
    }

    public Fraction<T> additiveInverse() {
        Fraction<T> result = new Fraction<>(num.additiveInverse(), denom);
        result.normalize();
        return result;
    }

    public Fraction<T> subtract(Fraction<T> f2) {
        Fraction<T> result = new Fraction<>(
                num.multiply(f2.denom).subtract(f2.num.multiply(denom)),
                denom.multiply(f2.denom));
        result.normalize();
        return result;
    }

    public Fraction<T> multiply(Fraction<T> f2) {
        Fraction<T> result = new Fraction<>(
                num.multiply(f2.num), denom.multiply(f2.denom));
        result.normalize();
        return result;
    }

    public Fraction<T> divide(Fraction<T> f2) {
        if (f2.compareToZero() == 0)
            throw new ArithmeticException("Divide by zero.");
        return this.multiply(f2.reciprocal());
    }

    public Fraction<T> reciprocal() {
        Fraction<T> result = new Fraction<>(denom, num);
        result.normalize();
        return result;
    }

    public Fraction<T> whole(long value) {
        return new Fraction<>(num.whole(value), num.one());
    }

    public void normalize() {
        if (denom.compareToZero() < 0) {
            num = num.additiveInverse();
            denom = denom.additiveInverse();
        }
        T commonDivisor = num.commonDivisor(denom);
        num = num.divide(commonDivisor);
        denom = denom.divide(commonDivisor);
    }

    @Override
    public int compareTo(Fraction<T> f2) {
        return num.multiply(f2.denom).compareTo(denom.multiply(f2.num));
    }

    @Override
    public int compareToZero() {
        return num.compareToZero();
    }

    /**
     * Get a common divisor of this com.numbers.Fraction and another.
     *
     * This just returns the unit com.numbers.Fraction if either this or the other
     * com.numbers.Fraction is nonzero. If both are zero, this returns 0.
     * @param f2: the other com.numbers.Fraction to get the common divisor of.
     * @return the unit com.numbers.Fraction.
     */
    @Override
    public Fraction<T> commonDivisor(Fraction<T> f2) {
        if (this.compareToZero() == 0 && f2.compareToZero() == 0)
            return this;
        if (f2.compareToZero() == 0)
            return this.multiply(reciprocal());
        return f2.multiply(f2.reciprocal());
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof Fraction<?> f2) {
            this.normalize();
            f2.normalize();
            return num.equals(f2.num) && denom.equals(f2.denom);
        }
        return false;
    }

    @Override
    public double toDouble() {
        return num.toDouble() / denom.toDouble();
    }

    @Override
    public String toString() {
        if (denom.compareTo(denom.one()) == 0){
            return num.toString();
        }
        return String.format("%s / %s", num.toString(), denom.toString());
    }

    public Fraction<T> one() {
        return new Fraction<>(num.one(), num.one());
    }
    public Fraction<T> two() {
        return new Fraction<>(num.two(), num.one());
    }
    public Fraction<T> three() {
        return new Fraction<>(num.three(), num.one());
    }
    public Fraction<T> four() {
        return new Fraction<>(num.four(), num.one());
    }
    public Fraction<T> five() {
        return new Fraction<>(num.five(), num.one());
    }
    public Fraction<T> six() {
        return new Fraction<>(num.six(), num.one());
    }
    public Fraction<T> rt3() {
        return new Fraction<>(num.rt3(), num.one());
    }
    public Fraction<T> twoRt3() {
        return new Fraction<>(num.twoRt3(), num.one());
    }
    public Fraction<T> threeRt3() {
        return new Fraction<>(num.threeRt3(), num.one());
    }
}
