/**
 * I want:
 *  Number class that all implement: add, subtract, multiply.
 *  Fraction class that takes two "Numbers" of the same type as numerator
 *  and denominator.
 *  Number could be:
 *  - Just integers.
 *  - Just doubles.
 *  - Complex number (real and imaginary part)
 *  - Precise representation of irrational number as whole number plus
 *    multiple of a fixed irrational number.
 * Ideally, each number can have their own static definition of -1, 0, 1.
 *
 *  Methods in the fraction class can rely on the fact that the numerator
 *  and denominator are numbers of the same type and so we can use the
 *  add, multiply, subtract, etc. for each of them.
 */

public class Magnitude extends AbstractNumber<Magnitude> {
    private static final String PRECISION_ERROR =
            "The rational over and under approximations of sqrt(3) are not " +
                    "sufficiently precise to compare %s and %s.";
    private static final String DIVISION_ERROR =
            "Non-integer division of %s by %s";
    private static final long OVER_APPROX_NUMERATOR_RT3 = 1351;
    private static final long OVER_APPROX_DENOMINATOR_RT3 = 780;
    private static final long UNDER_APPROX_NUMERATOR_RT3 = 265;
    private static final long UNDER_APPROX_DENOMINATOR_RT3 = 153;
    private static final long RT = 3;
    public static final Magnitude ONE = new Magnitude(1, 0);
    public static final Magnitude MINUS_ONE = new Magnitude(-1, 0);
    public static final Magnitude ZERO = new Magnitude(0, 0);

    long ones;
    long rt3;
    public Magnitude(long ones, long rt3) {
        this.ones = ones;
        this.rt3 = rt3;
    }

    @Override
    public Magnitude add(Magnitude summand) {
        return new Magnitude(ones + summand.ones, rt3 + summand.rt3);
    }

    @Override
    public Magnitude subtract(Magnitude summand) {
        return new Magnitude(ones - summand.ones, rt3 - summand.rt3);
    }

    @Override
    public Magnitude additiveInverse() {
        return this.multiply(MINUS_ONE);
    }

    @Override
    public Magnitude multiply(Magnitude scalar) {
        return new Magnitude(ones * scalar.ones + RT * rt3 * scalar.rt3,
                ones * scalar.rt3 + rt3 * scalar.ones);
    }

    /**
     * Divide this Magnitude by the given Magnitude.
     *
     * This method only supports "integer" division. That is, the result
     * of this operation multiplied by the divisor should give back the
     * original Magnitude.
     * @param divisor: The Magnitude to divide by.
     * @return the result of the division.
     */
    @Override
    public Magnitude divide(Magnitude divisor) {
        long rationalizedDenominator =
                divisor.ones * divisor.ones - RT * divisor.rt3 * divisor.rt3;
        long resultOnes = (ones * divisor.ones - RT * rt3 * divisor.rt3)
                / rationalizedDenominator;
        long resultRt3 = (rt3 * divisor.ones - ones * divisor.rt3)
                / rationalizedDenominator;
        Magnitude result = new Magnitude(resultOnes, resultRt3);
        if (this.compareTo(result.multiply(divisor)) != 0)
            throw new ArithmeticException(
                    String.format(DIVISION_ERROR, this, divisor));
        return result;
    }

    /**
     * Find the "greatest common divisor" of this Magnitude and another.
     *
     * This is a slight misnomer. The returned Magnitude has no irrational
     * part and the value is the largest integer that divides the integer
     * parts of both Magnitudes and the irrational parts of both Magnitudes.
     * @param m2: the second Magnitude.
     * @return the "greatest common divisor" of this Magnitude and another.
     */
    @Override
    public Magnitude gcd(Magnitude m2) {
       return new Magnitude(CommonMath.gcd(
               CommonMath.gcd(ones, rt3),
               CommonMath.gcd(m2.ones, m2.rt3)), 0);
    }

    /**
     * Compare two Magnitudes using rational approximations of sqrt(3).
     * @param m2: the magnitude to compare against.
     * @return -1 if this Magnitude is definitely smaller than m2, 0 if this
     *  Magnitude is definitely equal to m2, 1 if this Magnitude is definitely
     *  larger than m2. Throws an ArithmeticException otherwise.
     */
    @Override
    public int compareTo(Magnitude m2) {
        if (ones == m2.ones && rt3 == m2.rt3)
            return 0;
        long summedOverApproxNumerator = ones * OVER_APPROX_DENOMINATOR_RT3
                + rt3 * OVER_APPROX_NUMERATOR_RT3;
        long summedUnderApproxNumerator2 = m2.ones * UNDER_APPROX_DENOMINATOR_RT3
                + m2.rt3 * UNDER_APPROX_NUMERATOR_RT3;
        if (summedOverApproxNumerator * UNDER_APPROX_DENOMINATOR_RT3
                < summedUnderApproxNumerator2 * OVER_APPROX_DENOMINATOR_RT3)
            return -1;
        long summedUnderApproxNumerator = ones * UNDER_APPROX_DENOMINATOR_RT3
                + rt3 * UNDER_APPROX_NUMERATOR_RT3;
        long summedOverApproxNumerator2 = m2.ones * OVER_APPROX_DENOMINATOR_RT3
                + m2.rt3 * OVER_APPROX_NUMERATOR_RT3;
        if (summedOverApproxNumerator2 * UNDER_APPROX_DENOMINATOR_RT3
                < summedUnderApproxNumerator * OVER_APPROX_DENOMINATOR_RT3)
            return 1;
        throw new ArithmeticException(
                String.format(PRECISION_ERROR, this.toString(), m2.toString()));
    }

    @Override
    public String toString() {
        return String.format("%d + %d * sqrt(3)", ones, rt3);
    }

    @Override
    public int compareToZero() {
        return this.compareTo(ZERO);
    }
}
