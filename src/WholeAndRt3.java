/**
 * Class for precisely handling arithmetic on numbers of the form
 *      a + b * sqrt(3)
 * where a and b are integers.
 */
public class WholeAndRt3 extends AbstractNumber<WholeAndRt3> {
    private static final String PRECISION_ERROR =
            "The rational over and under approximations of sqrt(3) are not " +
                    "sufficiently precise to compare %s and %s.";
    private static final String DIVISION_ERROR =
            "Non-integer division of %s by %s";
    private static final long RT3_OVER_APPROX_NUMERATOR = 262087;
    private static final long RT3_OVER_APPROX_DENOMINATOR = 151316;
    private static final long RT3_UNDER_APPROX_NUMERATOR = 716035;
    private static final long RT3_UNDER_APPROX_DENOMINATOR = 413403;
    private static final long RT = 3;
    private static final int SQRT_APPROXIMATION_ITERATIONS = 10;
    public static final WholeAndRt3 ZERO = new WholeAndRt3(0, 0);

    long ones;
    long rt3;
    public WholeAndRt3(long ones, long rt3) {
        this.ones = ones;
        this.rt3 = rt3;
    }

    public WholeAndRt3 whole(long wholeValue) {
        return new WholeAndRt3(wholeValue, 0);
    }

    @Override
    public WholeAndRt3 add(WholeAndRt3 summand) {
        if (CommonMath.additionWillOverflow(ones, summand.ones) ||
                CommonMath.additionWillOverflow(rt3, summand.rt3))
            throw new RuntimeException("Addition overflow or underflow.");
        return new WholeAndRt3(ones + summand.ones, rt3 + summand.rt3);
    }

    @Override
    public WholeAndRt3 subtract(WholeAndRt3 summand) {
        if (summand.ones == Long.MIN_VALUE || summand.rt3 == Long.MIN_VALUE
                || CommonMath.additionWillOverflow(ones, -summand.ones)
                || CommonMath.additionWillOverflow(rt3, -summand.rt3))
            throw new RuntimeException("Subtraction overflow or underflow.");
        return new WholeAndRt3(ones - summand.ones, rt3 - summand.rt3);
    }

    @Override
    public WholeAndRt3 additiveInverse() {
        if (ones == Long.MIN_VALUE || rt3 == Long.MIN_VALUE)
            throw new RuntimeException("Additive inverse will overflow.");
        return new WholeAndRt3(-ones, -rt3);
    }

    @Override
    public WholeAndRt3 multiply(WholeAndRt3 scalar) {
        if (CommonMath.multiplicationWillOverflow(ones, scalar.ones)) {
            System.out.printf("Overflowing ones: %d * %d\n", ones, scalar.ones);
        }
        if (CommonMath.multiplicationWillOverflow(rt3, scalar.rt3)) {
            System.out.printf("Overflowing rt3: %d * %d\n", rt3, scalar.rt3);
        }
        if (CommonMath.multiplicationWillOverflow(ones, scalar.ones)
                || CommonMath.multiplicationWillOverflow(rt3, scalar.rt3)
                || CommonMath.multiplicationWillOverflow(rt3 * scalar.rt3, RT)
                || CommonMath.multiplicationWillOverflow(ones, scalar.rt3)
                || CommonMath.multiplicationWillOverflow(rt3, scalar.ones)
                || CommonMath.additionWillOverflow(ones * scalar.ones,
                    RT * rt3 * scalar.rt3)
                || CommonMath.additionWillOverflow(ones * scalar.rt3,
                    rt3 * scalar.ones))
            throw new RuntimeException("Multiplication overflow or underflow.");
        return new WholeAndRt3(ones * scalar.ones + RT * rt3 * scalar.rt3,
                ones * scalar.rt3 + rt3 * scalar.ones);
    }

    /**
     * Divide this WholeAndRt3 by the given WholeAndRt3.
     *
     * This method only supports "integer" division. That is, the result
     * of this operation multiplied by the divisor should give back the
     * original WholeAndRt3.
     * @param divisor: The WholeAndRt3 to divide by.
     * @return the result of the division.
     */
    @Override
    public WholeAndRt3 divide(WholeAndRt3 divisor) {
        WholeAndRt3 result;
        if (divisor.rt3 == 0) {
            result = new WholeAndRt3(
                    ones / divisor.ones, rt3 / divisor.ones);
        } else if (this.equals(ZERO)) {
            result = ZERO;
        } else {
            checkRationalizedDenominator(divisor);
            checkResultOnes(divisor);
            checkResultRt3(divisor);
            long rationalizedDenominator =
                    divisor.ones * divisor.ones - RT * divisor.rt3 * divisor.rt3;
            long resultOnes = (ones * divisor.ones - RT * rt3 * divisor.rt3)
                    / rationalizedDenominator;
            long resultRt3 = (rt3 * divisor.ones - ones * divisor.rt3)
                    / rationalizedDenominator;
            result = new WholeAndRt3(resultOnes, resultRt3);
        }
        if (this.compareTo(result.multiply(divisor)) != 0)
            throw new ArithmeticException(
                    String.format(DIVISION_ERROR, this, divisor));
        return result;
    }

    /**
     * Throw an exception if the computation with overflow or underflow.
     *
     * The computation being checked is:
     *  divisor.ones * divisor.ones - RT * divisor.rt3 * divisor.rt3
     * @throws RuntimeException if calculating the rationalized denominator
     *  results in overflow or underflow.
     * @param divisor: the divisor term.
     */
    private void checkRationalizedDenominator(WholeAndRt3 divisor) {
        if (CommonMath.multiplicationWillOverflow(divisor.ones, divisor.ones)
                || CommonMath.multiplicationWillOverflow(divisor.rt3, divisor.rt3)
                || CommonMath.multiplicationWillOverflow(RT * divisor.rt3, divisor.rt3))
            throw new RuntimeException("Multiplication overflow.");
        if (CommonMath.additionWillOverflow(divisor.ones * divisor.ones,
                -RT * divisor.rt3 * divisor.rt3))
            throw new RuntimeException("Subtraction overflow or underflow");
    }

    /**
     * Throw an exception if the computation will overflow or underflow.
     *
     * The computation being checked is:
     *  ones * divisor.ones - RT * rt3 * divisor.rt3
     * @throws RuntimeException if the computation results in overflow or
     *  underflow.
     * @param divisor: the divisor term.
     */
    private void checkResultOnes(WholeAndRt3 divisor) {
        if (CommonMath.multiplicationWillOverflow(ones, divisor.ones)
                || CommonMath.multiplicationWillOverflow(RT, rt3)
                || CommonMath.multiplicationWillOverflow(RT * rt3, divisor.rt3))
            throw new RuntimeException("Multiplication overflow.");
        if (CommonMath.additionWillOverflow(ones * divisor.ones,
                -RT * rt3 * divisor.rt3))
            throw new RuntimeException("Subtraction overflow or underflow");
    }
    /**
     * Throw an exception if the computation will overflow or underflow.
     *
     * The computation being checked is:
     *  rt3 * divisor.ones - ones * divisor.rt3
     * @throws RuntimeException if the computation results in overflow or
     *  underflow.
     * @param divisor: the divisor term.
     */
    private void checkResultRt3(WholeAndRt3 divisor) {
        if (CommonMath.multiplicationWillOverflow(rt3, divisor.ones)
                || CommonMath.multiplicationWillOverflow(ones, divisor.rt3))
            throw new RuntimeException("Multiplication overflow.");
        if (CommonMath.additionWillOverflow(rt3 * divisor.ones,
                -ones * divisor.rt3))
            throw new RuntimeException("Subtraction overflow or underflow");
    }

    /**
     * Find a "common divisor" of this WholeAndRt3 and another.
     *
     * The returned WholeAndRt3 has no irrational part and the value is the
     * largest integer that divides the integer parts of both WholeAndRt3s
     * and the irrational parts of both WholeAndRt3s.
     * If both terms are "zero", then return "one".
     * @param m2: the second WholeAndRt3.
     * @return a "common divisor" of this WholeAndRt3 and another.
     */
    @Override
    public WholeAndRt3 commonDivisor(WholeAndRt3 m2) {
        if (this.equals(ZERO) && m2.equals(ZERO))
            return one();
       return new WholeAndRt3(CommonMath.gcd(
               CommonMath.gcd(ones, rt3),
               CommonMath.gcd(m2.ones, m2.rt3)), 0);
    }

    /**
     * Compare two WholeAndRt3s using rational approximations of sqrt(3).
     * @param m2: the magnitude to compare against.
     * @return -1 if this WholeAndRt3 is definitely smaller than m2, 0 if this
     *  WholeAndRt3 is definitely equal to m2, 1 if this WholeAndRt3 is definitely
     *  larger than m2. Throws an ArithmeticException otherwise.
     */
    @Override
    public int compareTo(WholeAndRt3 m2) {
        if (ones == m2.ones && rt3 == m2.rt3)
            return 0;
        checkLessThanOverflow(m2);
        long summedOverApproxNumerator = ones * RT3_OVER_APPROX_DENOMINATOR
                + rt3 * RT3_OVER_APPROX_NUMERATOR;
        long summedUnderApproxNumerator2 = m2.ones * RT3_UNDER_APPROX_DENOMINATOR
                + m2.rt3 * RT3_UNDER_APPROX_NUMERATOR;
        if (summedOverApproxNumerator * RT3_UNDER_APPROX_DENOMINATOR
                < summedUnderApproxNumerator2 * RT3_OVER_APPROX_DENOMINATOR)
            return -1;
        checkGreaterThanOverflow(m2);
        long summedUnderApproxNumerator = ones * RT3_UNDER_APPROX_DENOMINATOR
                + rt3 * RT3_UNDER_APPROX_NUMERATOR;
        long summedOverApproxNumerator2 = m2.ones * RT3_OVER_APPROX_DENOMINATOR
                + m2.rt3 * RT3_OVER_APPROX_NUMERATOR;
        if (summedOverApproxNumerator2 * RT3_UNDER_APPROX_DENOMINATOR
                < summedUnderApproxNumerator * RT3_OVER_APPROX_DENOMINATOR)
            return 1;
        throw new ArithmeticException(
                String.format(PRECISION_ERROR, this, m2));
    }
    private void checkLessThanOverflow(WholeAndRt3 m2) {
        if (CommonMath.multiplicationWillOverflow(ones, RT3_OVER_APPROX_DENOMINATOR)
                || CommonMath.multiplicationWillOverflow(rt3, RT3_OVER_APPROX_NUMERATOR)
                || CommonMath.additionWillOverflow(
                ones * RT3_OVER_APPROX_DENOMINATOR,
                rt3 * RT3_OVER_APPROX_NUMERATOR))
            throw new RuntimeException("Comparison overflow.");
        if (CommonMath.multiplicationWillOverflow(m2.ones, RT3_UNDER_APPROX_DENOMINATOR)
                || CommonMath.multiplicationWillOverflow(m2.rt3, RT3_UNDER_APPROX_NUMERATOR)
                || CommonMath.additionWillOverflow(
                m2.ones * RT3_UNDER_APPROX_DENOMINATOR,
                m2.rt3 * RT3_UNDER_APPROX_NUMERATOR))
            throw new RuntimeException("Comparison overflow.");
        long summedOverApproxNumerator = ones * RT3_OVER_APPROX_DENOMINATOR
                + rt3 * RT3_OVER_APPROX_NUMERATOR;
        long summedUnderApproxNumerator2 = m2.ones * RT3_UNDER_APPROX_DENOMINATOR
                + m2.rt3 * RT3_UNDER_APPROX_NUMERATOR;
        if (CommonMath.multiplicationWillOverflow(
                summedOverApproxNumerator, RT3_UNDER_APPROX_DENOMINATOR)
                || CommonMath.multiplicationWillOverflow(
                summedUnderApproxNumerator2, RT3_OVER_APPROX_DENOMINATOR))
            throw new RuntimeException("Comparison overflow.");
    }
    private void checkGreaterThanOverflow(WholeAndRt3 m2) {
        if (CommonMath.multiplicationWillOverflow(ones, RT3_UNDER_APPROX_DENOMINATOR)
                || CommonMath.multiplicationWillOverflow(rt3, RT3_UNDER_APPROX_NUMERATOR)
                || CommonMath.additionWillOverflow(
                        ones * RT3_UNDER_APPROX_DENOMINATOR,
                        rt3 * RT3_UNDER_APPROX_NUMERATOR))
            throw new RuntimeException("Comparison overflow.");
        if (CommonMath.multiplicationWillOverflow(m2.ones, RT3_OVER_APPROX_DENOMINATOR)
                || CommonMath.multiplicationWillOverflow(m2.rt3, RT3_OVER_APPROX_NUMERATOR)
                || CommonMath.additionWillOverflow(
                m2.ones * RT3_OVER_APPROX_DENOMINATOR,
                m2.rt3 * RT3_OVER_APPROX_NUMERATOR))
            throw new RuntimeException("Comparison overflow.");
        long summedUnderApproxNumerator = ones * RT3_UNDER_APPROX_DENOMINATOR
                + rt3 * RT3_UNDER_APPROX_NUMERATOR;
        long summedOverApproxNumerator2 = m2.ones * RT3_OVER_APPROX_DENOMINATOR
                + m2.rt3 * RT3_OVER_APPROX_NUMERATOR;
        if (CommonMath.multiplicationWillOverflow(
                summedOverApproxNumerator2, RT3_UNDER_APPROX_DENOMINATOR)
                || CommonMath.multiplicationWillOverflow(
                summedUnderApproxNumerator, RT3_OVER_APPROX_DENOMINATOR))
            throw new RuntimeException("Comparison overflow.");
    }

    @Override
    public String toString() {
        return String.format("%d + %d * sqrt(3)", ones, rt3);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof WholeAndRt3 value2))
            return false;
        return compareTo(value2) == 0;
    }

    @Override
    public int compareToZero() {
        return this.compareTo(ZERO);
    }

    @Override
    public double toDouble() {
        return ones + Math.sqrt(3) * rt3;
    }

    public Fraction<WholeNumber> lower() {
        Fraction<WholeNumber> rt3Part = new Fraction<>(
                new WholeNumber(rt3 * RT3_UNDER_APPROX_NUMERATOR),
                new WholeNumber(RT3_UNDER_APPROX_DENOMINATOR)
        );
        return rt3Part.add(new Fraction<WholeNumber>(new WholeNumber(ones), WholeNumber.ONE));
    }
    public Fraction<WholeNumber> upper() {
        Fraction<WholeNumber> rt3Part = new Fraction<>(
                new WholeNumber(rt3 * RT3_OVER_APPROX_NUMERATOR),
                new WholeNumber(RT3_OVER_APPROX_DENOMINATOR)
        );
        return rt3Part.add(new Fraction<>(new WholeNumber(ones), WholeNumber.ONE));
    }

    public WholeAndRt3 one() {
        return new WholeAndRt3(1, 0);
    }
    public WholeAndRt3 two() {
        return new WholeAndRt3(2, 0);
    }
    public WholeAndRt3 three() {
        return new WholeAndRt3(3, 0);
    }
    public WholeAndRt3 four() {
        return new WholeAndRt3(4, 0);
    }
    public WholeAndRt3 five() {
        return new WholeAndRt3(5, 0);
    }
    public WholeAndRt3 six() {
        return new WholeAndRt3(6, 0);
    }
    public WholeAndRt3 rt3() {
        return new WholeAndRt3(0, 1);
    }
    public WholeAndRt3 twoRt3() {
        return new WholeAndRt3(0, 2);
    }
    public WholeAndRt3 threeRt3() {
        return new WholeAndRt3(0, 3);
    }
}
