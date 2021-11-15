public class DoubleRep extends AbstractNumber<DoubleRep> {
    private static final double EPS = 0.00000000001;

    double value;

    public DoubleRep(double value) {
        this.value = value;
    }

    @Override
    public DoubleRep add(DoubleRep n2) {
        return new DoubleRep(value + n2.value);
    }

    @Override
    public DoubleRep subtract(DoubleRep n2) {
        return new DoubleRep(value - n2.value);
    }

    @Override
    public DoubleRep multiply(DoubleRep n2) {
        return new DoubleRep(value * n2.value);
    }

    @Override
    public DoubleRep divide(DoubleRep n2) {
        return new DoubleRep(value / n2.value);
    }

    public DoubleRep whole(long wholeValue) {
        return new DoubleRep(wholeValue);
    }

    @Override
    public DoubleRep commonDivisor(DoubleRep n2) {
        if (value == (long) value && n2.value == (long) n2.value) {
            return new DoubleRep(CommonMath.gcd((long) value, (long) n2.value));
        }
        return one();
    }

    @Override
    public DoubleRep additiveInverse() {
        return new DoubleRep(-value);
    }


    @Override
    public int compareToZero() {
        return compareTo(new DoubleRep(0));
    }

    @Override
    public int compareTo(DoubleRep o) {
        return Double.compare(value, o.value);
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof DoubleRep d2) {
            return Math.abs(value - d2.value) < EPS;
        }
        return false;
    }

    @Override
    public double toDouble() {
        return value;
    }

    @Override
    public String toString() {
        return ""+value;
    }

    @Override
    public DoubleRep one() {
        return new DoubleRep(1);
    }

    @Override
    public DoubleRep two() {
        return one().add(one());
    }

    @Override
    public DoubleRep three() {
        return two().add(one());
    }

    @Override
    public DoubleRep four() {
        return two().add(two());
    }

    @Override
    public DoubleRep five() {
        return three().add(two());
    }

    @Override
    public DoubleRep six() {
        return three().add(three());
    }

    @Override
    public DoubleRep rt3() {
        return new DoubleRep(Math.sqrt(3));
    }

    @Override
    public DoubleRep twoRt3() {
        return rt3().add(rt3());
    }

    @Override
    public DoubleRep threeRt3() {
        return twoRt3().add(rt3());
    }
}
