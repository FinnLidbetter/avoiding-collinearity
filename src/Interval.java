public class Interval {
    int lo;
    int hi;
    public Interval(int lo, int hi) {
        this.lo = lo;
        this.hi = hi;
    }

    public String toString() {
        return String.format("[%d,%d]", lo, hi);
    }
}
