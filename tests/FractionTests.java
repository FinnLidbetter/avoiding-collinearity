import org.junit.Assert;
import org.junit.Test;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

public class FractionTests {
    private static final FractionFactory f = new FractionFactory();
    private static final Fraction<WholeNumber> one = f.makeFraction(1, 1);
    private static final Fraction<WholeNumber> zero = f.makeFraction(0, 1);

    @Test
    public void testAdd() {
        long[][][] cases = {
                {{0, 1}, {0, 1}, {0, 1}},
                {{1, 1}, {0, 1}, {1, 1}},
                {{0, 1}, {1, 1}, {1, 1}},
                {{0, 1}, {-1, 1}, {-1, 1}},
                {{-1, 1}, {0, 1}, {-1, 1}},
                {{-1, 1}, {1, 1}, {0, 1}},
                {{1, 1}, {-1, 1}, {0, 1}},
                {{1, 2}, {1, 3}, {5, 6}},
                {{1, 3}, {1, 2}, {5, 6}},
                {{1, 2}, {-1, 3}, {1, 6}},
                {{1, 3}, {1, -2}, {-1, 6}},
                {{1, 4}, {1, 4}, {1, 2}},
                {{2, 10}, {2, 3}, {13, 15}}
        };
        for (long[][] additionCase: cases) {
            System.out.println(Arrays.deepToString(additionCase));
            Fraction<WholeNumber> leftSummand = f.makeFraction(
                    additionCase[0][0], additionCase[0][1]);
            Fraction<WholeNumber> rightSummand = f.makeFraction(
                    additionCase[1][0], additionCase[1][1]);
            Fraction<WholeNumber> expectedSum = f.makeFraction(
                    additionCase[2][0], additionCase[2][1]);
            Assert.assertEquals(expectedSum, leftSummand.add(rightSummand));
        }
    }

    @Test
    public void testSubtract() {
        long[][][] cases = {
                {{0, 1}, {0, 1}, {0, 1}},
                {{1, 1}, {1, 1}, {0, 1}},
                {{1, 1}, {0, 1}, {1, 1}},
                {{-1, 1}, {0, 1}, {-1, 1}},
                {{-1, 1}, {-1, 1}, {0, 1}},
                {{0, 1}, {-1, 1}, {1, 1}},
                {{0, 1}, {1, 1}, {-1, 1}},
                {{5, 6}, {1, 2}, {1, 3}},
                {{5, 6}, {1, 3}, {1, 2}},
                {{1, 6}, {1, 2}, {-1, 3}},
                {{-1, 6}, {1, 3}, {1, -2}},
                {{1, 2}, {1, 4}, {1, 4}},
                {{13, 15}, {2, 10}, {2, 3}}
        };
        for (long[][] subtractionCase: cases) {
            System.out.println(Arrays.deepToString(subtractionCase));
            Fraction<WholeNumber> leftSummand = f.makeFraction(
                    subtractionCase[0][0], subtractionCase[0][1]);
            Fraction<WholeNumber> rightSummand = f.makeFraction(
                    subtractionCase[1][0], subtractionCase[1][1]);
            Fraction<WholeNumber> expectedDifference = f.makeFraction(
                    subtractionCase[2][0], subtractionCase[2][1]);
            Assert.assertEquals(expectedDifference,
                    leftSummand.subtract(rightSummand));
        }
    }

    @Test
    public void testAdditiveInverse() {
        long[][][] cases = {
                {{0, 1}, {0, 1}},
                {{1, 1}, {-1, 1}},
                {{-1, 1}, {1, 1}},
                {{3, 5}, {-3, 5}},
                {{3, 6}, {-1, 2}}
        };
        for (long[][] additiveInverseCase: cases) {
            Fraction<WholeNumber> value = f.makeFraction(
                    additiveInverseCase[0][0], additiveInverseCase[0][1]);
            Fraction<WholeNumber> expectedResult = f.makeFraction(
                    additiveInverseCase[1][0], additiveInverseCase[1][1]);
            Assert.assertEquals(expectedResult, value.additiveInverse());
        }
    }

    @Test
    public void testMultiply() {
        long[][][] cases = {
                {{0, 1}, {0, 1}, {0, 1}},
                {{0, 1}, {1, 1}, {0, 1}},
                {{1, 1}, {0, 1}, {0, 1}},
                {{1, 1}, {1, 1}, {1, 1}},
                {{1, 1}, {-1, 1}, {-1, 1}},
                {{-1, 1}, {1, 1}, {-1, 1}},
                {{-1, 1}, {-1, 1}, {1, 1}},
                {{13, -3}, {-2, 51}, {26, 153}},
        };
        for (long[][] multiplicationCase: cases) {
            System.out.println(Arrays.deepToString(multiplicationCase));
            Fraction<WholeNumber> leftMultiplicand = f.makeFraction(
                    multiplicationCase[0][0], multiplicationCase[0][1]);
            Fraction<WholeNumber> rightMultiplicand = f.makeFraction(
                    multiplicationCase[1][0], multiplicationCase[1][1]);
            Fraction<WholeNumber> expectedProduct = f.makeFraction(
                    multiplicationCase[2][0], multiplicationCase[2][1]);
            Assert.assertEquals(expectedProduct,
                    leftMultiplicand.multiply(rightMultiplicand));
        }
    }

    @Test
    public void testCommonDivisor() {
        long[][][] cases = {
                {{0, 1}, {0, -1}, {0, 1}},
                {{0, 1}, {5, 2}, {1, 1}},
                {{5, 2}, {0, 1}, {1, 1}},
                {{1, 1}, {1, -1}, {1, 1}},
                {{-1, 1}, {-1, 1}, {1, 1}},
                {{23, 43}, {-34, 3}, {1, 1}},
                {{2, 2}, {4, 8}, {1, 1}}
        };
        for (long[][] commonDivisorCase: cases) {
            System.out.println(Arrays.deepToString(commonDivisorCase));
            Fraction<WholeNumber> leftTerm = f.makeFraction(
                    commonDivisorCase[0][0], commonDivisorCase[0][1]);
            Fraction<WholeNumber> rightTerm = f.makeFraction(
                    commonDivisorCase[1][0], commonDivisorCase[1][1]);
            Fraction<WholeNumber> expectedResult = f.makeFraction(
                    commonDivisorCase[2][0], commonDivisorCase[2][1]);
            Assert.assertEquals(expectedResult,
                    leftTerm.commonDivisor(rightTerm));
        }
    }

    @Test
    public void testDivide() {
        long[][][] cases = {
                {{0, 1}, {1, 1}, {0, 1}},
                {{1, 1}, {1, 1}, {1, 1}},
                {{-1, 1}, {1, 1}, {-1, 1}},
                {{-1, 1}, {-1, 1}, {1, 1}},
                {{1, 1}, {-1, 1}, {-1, 1}},
                {{26, 153}, {13, -3}, {-2, 51}},
        };
        for (long[][] multiplicationCase: cases) {
            System.out.println(Arrays.deepToString(multiplicationCase));
            Fraction<WholeNumber> dividend = f.makeFraction(
                    multiplicationCase[0][0], multiplicationCase[0][1]);
            Fraction<WholeNumber> divisor = f.makeFraction(
                    multiplicationCase[1][0], multiplicationCase[1][1]);
            Fraction<WholeNumber> expectedResult = f.makeFraction(
                    multiplicationCase[2][0], multiplicationCase[2][1]);
            Assert.assertEquals(expectedResult,
                    dividend.divide(divisor));
        }
        Assert.assertThrows(ArithmeticException.class,
                () -> { one.divide(zero); });
        Assert.assertThrows(ArithmeticException.class,
                () -> { zero.divide(zero); });
    }

    @Test
    public void testCompare() {
        long[][] arr = {
                {0, 1},
                {-1, 1},
                {2, 5},
                {6, 7},
                {1, 1},
                {1, 3},
                {-45, 7},
                {3, 2},
                {1, 4},
                {1, 2}
        };
        long[][] expectedOrder = {
                {-45, 7},
                {-1, 1},
                {0, 1},
                {1, 4},
                {1, 3},
                {2, 5},
                {1, 2},
                {6, 7},
                {1, 1},
                {3, 2}
        };
        ArrayList<Fraction<WholeNumber>> list = new ArrayList<>();
        ArrayList<Fraction<WholeNumber>> expectedList = new ArrayList<>();
        for (long[] fraction: arr) {
            list.add(f.makeFraction(fraction[0], fraction[1]));
        }
        for (long[] fraction: expectedOrder) {
            expectedList.add(f.makeFraction(fraction[0], fraction[1]));
        }
        Collections.sort(list);
        Assert.assertEquals(expectedList.size(), list.size());
        for (int index = 0; index < list.size(); index++) {
            Assert.assertEquals(expectedList.get(index), list.get(index));
        }
    }
}
