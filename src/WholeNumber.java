/**
 * Class used as a wrapper around long. This is defined to be compatible
 * with the Fraction class by extending AbstractNumber. This makes
 * testing the Fraction, Vector, and Trapezoid classes easier.
 */
public class WholeNumber extends AbstractNumber<WholeNumber> {
    public static final WholeNumber MINUS_ONE = new WholeNumber(-1);
    public static final WholeNumber ZERO = new WholeNumber(0);
    public static final WholeNumber ONE = new WholeNumber(1);
    private static final String DIVISION_ERROR =
            "Non-integer division of %s by %s";

    long value;

    public WholeNumber(long value) {
        this.value = value;
    }

    @Override
    public WholeNumber add(WholeNumber n2) {
        if (CommonMath.additionWillOverflow(value, n2.value))
            throw new RuntimeException("Addition will overflow.");
        return new WholeNumber(value + n2.value);
    }

    @Override
    public WholeNumber subtract(WholeNumber n2) {
        if (n2.value == Long.MIN_VALUE
                || CommonMath.additionWillOverflow(value, -n2.value))
            throw new RuntimeException("Subtraction will overflow.");
        return new WholeNumber(value - n2.value);
    }

    @Override
    public WholeNumber multiply(WholeNumber n2) {
        if (CommonMath.multiplicationWillOverflow(value, n2.value))
            throw new RuntimeException("Multiplication will overflow.");
        return new WholeNumber(value * n2.value);
    }

    @Override
    public WholeNumber divide(WholeNumber n2) {
        long result = value / n2.value;
        if (n2.value * result != value)
            throw new ArithmeticException(
                    String.format(DIVISION_ERROR, value, n2.value));
        return new WholeNumber(result);
    }

    @Override
    public WholeNumber commonDivisor(WholeNumber n2) {
        if (value == 0 && n2.value == 0)
            return new WholeNumber(1);
        return new WholeNumber(CommonMath.gcd(value, n2.value));
    }

    @Override
    public WholeNumber additiveInverse() {
        if (value == Long.MIN_VALUE)
            throw new RuntimeException("Additive inverse will overflow.");
        return new WholeNumber(-value);
    }

    public WholeNumber whole(long wholeValue) {
        return new WholeNumber(wholeValue);
    }

    @Override
    public int compareTo(WholeNumber n2) {
        return Long.compare(value, n2.value);
    }

    @Override
    public int compareToZero() {
        return this.compareTo(ZERO);
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof WholeNumber n2) {
            return value == n2.value;
        }
        return false;
    }

    @Override
    public double toDouble() {
        return (double) value;
    }

    @Override
    public String toString() {
        return ""+value;
    }

    public WholeNumber one() {
        return new WholeNumber(1);
    }
    public WholeNumber two() {
        return one().add(one());
    }
    public WholeNumber three() {
        return two().add(one());
    }
    public WholeNumber four() {
        return two().add(two());
    }
    public WholeNumber five() {
        return three().add(two());
    }
    public WholeNumber six() {
        return three().add(three());
    }

    /**
     * Return a value representing sqrt(3).
     *
     * For the WholeNumber class this is undefined, but for the purpose of
     * the trapezoid sequences the y coordinate is always a multiple of sqrt(3)
     * and the x coordinate has no sqrt(3) terms. So for these purposes we
     * just represent multiples of sqrt(3) the same way as whole numbers.
     * @return a representation of sqrt(3).
     */
    public WholeNumber rt3() {
        return new WholeNumber(1);
    }

    /**
     * Return a value representing 2 * sqrt(3).
     * @return a representation of 2 * sqrt(3).
     */
    public WholeNumber twoRt3() {
        return rt3().add(rt3());
    }

    /**
     * Return a value representing 3 * sqrt(3).
     * @return a representation of 3 * sqrt(3).
     */
    public WholeNumber threeRt3() {
        return twoRt3().add(rt3());
    }
}
