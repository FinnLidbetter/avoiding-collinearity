public class VectorFactory {
    public Vector<WholeNumber> makeVector(long x, long y) {
        return new Vector<WholeNumber>(new WholeNumber(x), new WholeNumber(y));
    }
}
