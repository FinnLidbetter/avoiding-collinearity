public class FractionFactory {
    public Fraction<WholeNumber> makeFraction(long numerator, long denominator) {
        return new Fraction<WholeNumber>(
                new WholeNumber(numerator), new WholeNumber(denominator));
    }
    public Fraction<WholeAndRt3> makeRt3Fraction(
            long numeratorOnes, long numeratorRt3s,
            long denominatorOnes, long denominatorRt3s) {
        return new Fraction<WholeAndRt3>(
                new WholeAndRt3(numeratorOnes, numeratorRt3s),
                new WholeAndRt3(denominatorOnes, denominatorRt3s));
    }
}
