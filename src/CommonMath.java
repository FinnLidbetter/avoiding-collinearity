public class CommonMath {
    /**
     * Get the greatest common divisor of a and b.
     *
     * If either of a and b are negative, treat them as if they are positive.
     * @param a: the first value.
     * @param b: the second value.
     * @return The greatest common divisor of the given values.
     */
    public static long gcd(long a, long b) {
        if (a < 0)
            a *= -1;
        if (b < 0)
            b *= -1;
        return (b == 0) ? a : gcd(b, a%b);
    }

    /**
     * Return True iff a+b will overflow or underflow a Long.
     * @param a: the left summand.
     * @param b: the right summand.
     * @return True iff a+b will overflow or underflow a Long.
     */
    public static boolean additionWillOverflow(long a, long b) {
        if ((a>=0 && b<0) || (a<0 && b>=0))
            return false;
        if (a>=0) {
            return Long.MAX_VALUE - a < b;
        }
        return a < Long.MIN_VALUE - b;
    }

    /**
     * Return True iff a*b will overflow or underflow a Long.
     * @param a: the left multiplicand.
     * @param b: the right multiplicand.
     * @return True iff a*b will overflow or underflow a Long.
     */
    public static boolean multiplicationWillOverflow(long a, long b) {
        if (a==0 || b==0)
            return false;
        if (a == Long.MIN_VALUE || b == Long.MIN_VALUE)
            return a != 1 && b != 1;
        if ((a>0 && b>0))
            return Long.MAX_VALUE / a < b;
        if (a<0 && b<0)
            return Long.MAX_VALUE / a > b;
        if (a<0)
            return Long.MIN_VALUE / b > a;
        return Long.MIN_VALUE / a > b;
    }
}
