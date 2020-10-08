import org.junit.Assert;
import org.junit.Test;
import java.util.Arrays;

/**
 * Tests for the WholeAndRt3 abstract number class operations.
 */
public class WholeAndRt3Tests {
    private static final WholeAndRt3 zero = new WholeAndRt3(0, 0);
    private static final WholeAndRt3 one = new WholeAndRt3(1, 0);
    private static final WholeAndRt3 minusOne = new WholeAndRt3(-1, 0);
    private static final WholeAndRt3 rt3 = new WholeAndRt3(0, 1);
    private static final WholeAndRt3 minusRt3 = new WholeAndRt3(0, -1);
    private static final WholeAndRt3 onePlusRt3 = new WholeAndRt3(1, 1);
    private static final WholeAndRt3 oneMinusRt3 = new WholeAndRt3(1, -1);
    private static final WholeAndRt3 largePositive = new WholeAndRt3(
            Long.MAX_VALUE - 1, Long.MAX_VALUE - 1);
    private static final WholeAndRt3 maxValue = new WholeAndRt3(
            Long.MAX_VALUE, Long.MAX_VALUE);
    private static final WholeAndRt3 largeNegative = new WholeAndRt3(
            Long.MIN_VALUE + 1, Long.MIN_VALUE + 1);
    private static final WholeAndRt3 minValue = new WholeAndRt3(
            Long.MIN_VALUE, Long.MIN_VALUE);
    private static final WholeAndRt3 two = new WholeAndRt3(2, 0);
    private static final WholeAndRt3 minusTwo = new WholeAndRt3(-2, 0);
    private static final WholeAndRt3 twoRt3 = new WholeAndRt3(0, 2);
    private static final WholeAndRt3 minusTwoRt3 = new WholeAndRt3(0, -2);

    @Test
    public void testValidAddition() {
        WholeAndRt3[][] cases = {
                // Adding zero has no effect.
                {zero, zero, zero},
                {one, zero, one},
                {rt3, zero, rt3},
                {onePlusRt3, zero, onePlusRt3},
                // Adding a value to zero gives the value back.
                {zero, one, one},
                {zero, minusOne, minusOne},
                {zero, oneMinusRt3, oneMinusRt3},
                // Nonzero cases.
                {one, minusOne, zero},
                {minusOne, one, zero},
                {rt3, minusRt3, zero},
                {one, onePlusRt3, new WholeAndRt3(2, 1)},
                {new WholeAndRt3(73, -15), new WholeAndRt3(24, 46),
                        new WholeAndRt3(73 + 24, -15 + 46)},
                {largePositive, onePlusRt3, maxValue},
                {onePlusRt3, largePositive, maxValue},
                {largeNegative, new WholeAndRt3(-1, -1), minValue},
                {new WholeAndRt3(-1, -1), largeNegative, minValue}
        };
        for (WholeAndRt3[] additionCase : cases) {
            System.out.println(Arrays.toString(additionCase));
            WholeAndRt3 leftSummand = additionCase[0];
            WholeAndRt3 rightSummand = additionCase[1];
            WholeAndRt3 expectedSum = additionCase[2];
            Assert.assertEquals(leftSummand.add(rightSummand), expectedSum);
        }
    }

    @Test
    public void testOverflowAddition() {
        WholeAndRt3[][] cases = {
                {largePositive, two},
                {largePositive, twoRt3},
                {two, largePositive},
                {twoRt3, largePositive},
                {largeNegative, minusTwo},
                {largeNegative, minusTwoRt3},
                {minusTwo, largeNegative},
                {minusTwoRt3, largeNegative}
        };
        for (WholeAndRt3[] overflowCase: cases) {
            System.out.println(Arrays.toString(overflowCase));
            WholeAndRt3 leftSummand = overflowCase[0];
            WholeAndRt3 rightSummand = overflowCase[1];
            Assert.assertThrows(RuntimeException.class,
                    () -> { leftSummand.add(rightSummand); });
        }
    }

    @Test
    public void testValidSubtraction() {
        WholeAndRt3[][] cases = {
                // Subtracting zero has no effect.
                {zero, zero, zero},
                {one, zero, one},
                {rt3, zero, rt3},
                {onePlusRt3, zero, onePlusRt3},
                // Subtracting a value from zero gives the negation of the value.
                {zero, one, minusOne},
                {zero, minusOne, one},
                {zero, oneMinusRt3, new WholeAndRt3(-1, 1)},
                // Nonzero cases.
                {one, minusOne, two},
                {minusOne, one, minusTwo},
                {rt3, minusRt3, twoRt3},
                {one, onePlusRt3, minusRt3},
                {new WholeAndRt3(73, -15), new WholeAndRt3(24, 46),
                        new WholeAndRt3(73 - 24, -15 - 46)},
                {largePositive, new WholeAndRt3(-1, -1), maxValue},
                {new WholeAndRt3(-2, -2), largePositive, minValue},
                {new WholeAndRt3(-1, -1), maxValue, minValue},
                {largeNegative, onePlusRt3, minValue},
                {zero, largeNegative, maxValue},
                {onePlusRt3,
                        new WholeAndRt3(Long.MIN_VALUE + 2, Long.MIN_VALUE + 2),
                        maxValue},
                {zero, maxValue, largeNegative},
        };
        for (WholeAndRt3[] subtractionCase : cases) {
            System.out.println(Arrays.toString(subtractionCase));
            WholeAndRt3 leftTerm = subtractionCase[0];
            WholeAndRt3 rightTerm = subtractionCase[1];
            WholeAndRt3 expectedDifference = subtractionCase[2];
            Assert.assertEquals(leftTerm.subtract(rightTerm),
                    expectedDifference);
        }
    }

    @Test
    public void testOverflowSubtraction() {
        WholeAndRt3[][] cases = {
                {minusTwo, maxValue},
                {largePositive, minusTwoRt3},
                {zero, minValue},
                // This case technically should work, but we disallow
                // subtracting Long.MIN_VALUE since
                // -Long.MIN_VALUE != Long.MAX_VALUE.
                {new WholeAndRt3(-1, -1), minValue},
                {new WholeAndRt3(-2, -2), maxValue},
                {rt3, largeNegative},
                {largePositive, minusTwo},
                {largePositive, minusTwoRt3},
                {one, largeNegative},
                {rt3, largeNegative}
        };
        for (WholeAndRt3[] overflowCase: cases) {
            System.out.println(Arrays.toString(overflowCase));
            WholeAndRt3 leftSummand = overflowCase[0];
            WholeAndRt3 rightSummand = overflowCase[1];
            Assert.assertThrows(RuntimeException.class,
                    () -> { leftSummand.subtract(rightSummand); });
        }
    }

    @Test
    public void testMultiplication() {
        WholeAndRt3[][] cases = {
                // Multiplication by zero gives zero.
                {one, zero, zero},
                {zero, one, zero},
                {onePlusRt3, zero, zero},
                {zero, onePlusRt3, zero},
                {maxValue, zero, zero},
                {zero, maxValue, zero},
                {minValue, zero, zero},
                {zero, minValue, zero},
                // Multiplication by one gives the same value back (if nonzero).
                {maxValue, one, maxValue},
                {one, maxValue, maxValue},
                {minValue, one, minValue},
                {one, minValue, minValue},
                {minusOne, one, minusOne},
                {one, minusOne, minusOne},
                {one, onePlusRt3, onePlusRt3},
                {onePlusRt3, one, onePlusRt3},
                // General cases.
                {onePlusRt3, oneMinusRt3, new WholeAndRt3(-2, 0)},
                {new WholeAndRt3(3, 0), new WholeAndRt3(42, -3),
                        new WholeAndRt3(42 * 3, -3 * 3)},
                {new WholeAndRt3(5, 6), new WholeAndRt3(-1, 3),
                        new WholeAndRt3(5 * -1 + 3 * 3 * 6, 5 * 3 + 6 * -1)},
                {two, new WholeAndRt3(Long.MAX_VALUE / 2, Long.MAX_VALUE / 2),
                        new WholeAndRt3(Long.MAX_VALUE - 1, Long.MAX_VALUE - 1)},
                {new WholeAndRt3(Long.MAX_VALUE / 2, Long.MAX_VALUE / 2), two,
                        new WholeAndRt3(Long.MAX_VALUE - 1, Long.MAX_VALUE - 1)},
                {two, new WholeAndRt3(Long.MIN_VALUE / 2, Long.MIN_VALUE / 2),
                        new WholeAndRt3(Long.MIN_VALUE, Long.MIN_VALUE)},
                {new WholeAndRt3(Long.MIN_VALUE / 2, Long.MIN_VALUE / 2), two,
                        new WholeAndRt3(Long.MIN_VALUE, Long.MIN_VALUE)}
        };
        for (WholeAndRt3[] multiplicationCase: cases) {
            System.out.println(Arrays.toString(multiplicationCase));
            WholeAndRt3 leftMultiplicand = multiplicationCase[0];
            WholeAndRt3 rightMultiplicand = multiplicationCase[1];
            WholeAndRt3 expectedProduct = multiplicationCase[2];
            Assert.assertEquals(leftMultiplicand.multiply(rightMultiplicand),
                    expectedProduct);
        }
    }

    @Test
    public void testOverflowMultiplication() {
        WholeAndRt3[][] cases = {
                {two, new WholeAndRt3(Long.MAX_VALUE / 2 + 1, Long.MAX_VALUE / 2 + 1)},
                {new WholeAndRt3(Long.MAX_VALUE / 2 + 1, Long.MAX_VALUE / 2 + 1), two},
                {two, new WholeAndRt3(Long.MIN_VALUE / 2 - 1, Long.MIN_VALUE / 2 - 1)},
                {new WholeAndRt3(Long.MIN_VALUE / 2 - 1, Long.MIN_VALUE/ 2 - 1), two},
                {largePositive, onePlusRt3},
                {minusOne, minValue},
                {minValue, minusOne},
                {new WholeAndRt3(-2, -2), maxValue},
                {largePositive, largeNegative},
        };
        for (WholeAndRt3[] overflowCase: cases) {
            System.out.println(Arrays.toString(overflowCase));
            WholeAndRt3 leftMultiplicand = overflowCase[0];
            WholeAndRt3 rightMultiplicand = overflowCase[1];
            Assert.assertThrows(RuntimeException.class,
                    () -> { leftMultiplicand.multiply(rightMultiplicand); });
        }
    }

    @Test
    public void testCompare() {
        WholeAndRt3[] arr = {
                new WholeAndRt3(0, 0),      // 0
                new WholeAndRt3(-1, -1),    // -2.7320508...
                new WholeAndRt3(1, 1),      // 2.7320508...
                new WholeAndRt3(5, -4),     // -1.92820323...
                new WholeAndRt3(23, 0),     // 23
                new WholeAndRt3(94, -41),   // 22.98591689...
                new WholeAndRt3(-3, 15),    // 22.98076211...
                new WholeAndRt3(535, 214),  // 905.6588728...
                new WholeAndRt3(5, -4)      // -1.92820323...
        };
        WholeAndRt3[] expectedOrder = {
                new WholeAndRt3(-1, -1),    // -2.7320508...
                new WholeAndRt3(5, -4),     // -1.92820323...
                new WholeAndRt3(5, -4),     // -1.92820323...
                new WholeAndRt3(0, 0),      // 0
                new WholeAndRt3(1, 1),      // 2.7320508...
                new WholeAndRt3(-3, 15),    // 22.98076211...
                new WholeAndRt3(94, -41),   // 22.98591689...
                new WholeAndRt3(23, 0),     // 23
                new WholeAndRt3(535, 214)   // 905.6588728...
        };
        Arrays.sort(arr);
        Assert.assertArrayEquals(expectedOrder, arr);
    }

    @Test
    public void testPrecisionComparison() {
        // 2672280.000000374...
        WholeAndRt3 almost2672280 = new WholeAndRt3(1, 1542841);
        WholeAndRt3 exactly2672280 = new WholeAndRt3(2672280, 0);
        Assert.assertThrows(ArithmeticException.class,
                () -> { almost2672280.compareTo(exactly2672280); });
        Assert.assertThrows(ArithmeticException.class,
                () -> { exactly2672280.compareTo(almost2672280); });
    }

    @Test
    public void testCommonDivisor() {
        WholeAndRt3[][] cases = {
                {new WholeAndRt3(12, 8), new WholeAndRt3(40, 44),
                        new WholeAndRt3(4, 0)},
                {new WholeAndRt3(15, 7), new WholeAndRt3(105, 105),
                        new WholeAndRt3(1, 0)},
                {new WholeAndRt3(35, -35), new WholeAndRt3(-35, 35),
                        new WholeAndRt3(35, 0)},
                {new WholeAndRt3(0, 40), new WholeAndRt3(80, 120),
                        new WholeAndRt3(40, 0)},
                {new WholeAndRt3(40, 0), new WholeAndRt3(80, 120),
                        new WholeAndRt3(40, 0)},
                {new WholeAndRt3(80, 120), new WholeAndRt3(40, 0),
                        new WholeAndRt3(40, 0)},
                {new WholeAndRt3(80, 120), new WholeAndRt3(0, 40),
                        new WholeAndRt3(40, 0)},
                {new WholeAndRt3(-12, -60), new WholeAndRt3(-120, -72),
                        new WholeAndRt3(12, 0)},
                {new WholeAndRt3(5, 0), new WholeAndRt3(0, 0),
                        new WholeAndRt3(5, 0)},
                {new WholeAndRt3(0, 5), new WholeAndRt3(0, 0),
                        new WholeAndRt3(5, 0)},
                {new WholeAndRt3(0, 0), new WholeAndRt3(5, 0),
                        new WholeAndRt3(5, 0)},
                {new WholeAndRt3(0, 0), new WholeAndRt3(0, 5),
                        new WholeAndRt3(5, 0)},
                {new WholeAndRt3(0, 0), new WholeAndRt3(0, 0),
                        new WholeAndRt3(1, 0)}
        };
        for (WholeAndRt3[] commonDivisorCase: cases) {
            System.out.println(Arrays.toString(commonDivisorCase));
            WholeAndRt3 leftTerm = commonDivisorCase[0];
            WholeAndRt3 rightTerm = commonDivisorCase[1];
            WholeAndRt3 expectedResult = commonDivisorCase[2];
            Assert.assertEquals(leftTerm.commonDivisor(rightTerm),
                    expectedResult);
        }
    }

    @Test
    public void testDivide() {
        WholeAndRt3[][] cases = {
                {zero, one, zero},
                {zero, minusOne, zero},
                {zero, onePlusRt3, zero},
                {zero, minValue, zero},
                {zero, maxValue, zero},
                {maxValue, new WholeAndRt3(Long.MAX_VALUE, 0), onePlusRt3},
                {maxValue, one, maxValue},
                {minValue, one, minValue},
                {minValue, new WholeAndRt3(Long.MIN_VALUE, 0), onePlusRt3},
                {new WholeAndRt3(42, 35), new WholeAndRt3(7, 0),
                        new WholeAndRt3(6, 5)},
                {new WholeAndRt3(-2, 0), onePlusRt3, oneMinusRt3},
                {new WholeAndRt3(126, -9), new WholeAndRt3(3, 0), new WholeAndRt3(42, -3)},
                {new WholeAndRt3(49, 9), new WholeAndRt3(5, 6), new WholeAndRt3(-1, 3)},
                {new WholeAndRt3(Long.MAX_VALUE - 1, Long.MAX_VALUE - 1), two,
                        new WholeAndRt3(Long.MAX_VALUE / 2, Long.MAX_VALUE / 2)},
                {new WholeAndRt3(0, 3), new WholeAndRt3(0, 1),
                        new WholeAndRt3(3, 0)}
        };
        for (WholeAndRt3[] divisionCase: cases) {
            System.out.println(Arrays.toString(divisionCase));
            WholeAndRt3 dividend = divisionCase[0];
            WholeAndRt3 divisor = divisionCase[1];
            WholeAndRt3 expectedResult = divisionCase[2];
            Assert.assertEquals(dividend.divide(divisor), expectedResult);
        }
    }

    @Test
    public void testUnevenDivide() {
        WholeAndRt3[][] cases = {
                {new WholeAndRt3(34, 35), zero},
                {new WholeAndRt3(34, 35), new WholeAndRt3(3, 2)},
                {new WholeAndRt3(0, 3), new WholeAndRt3(1, 3)}
        };
        for (WholeAndRt3[] divisionCase: cases) {
            WholeAndRt3 dividend = divisionCase[0];
            WholeAndRt3 divisor = divisionCase[1];
            Assert.assertThrows(ArithmeticException.class,
                    () -> { dividend.divide(divisor); });
        }
    }

    @Test
    public void testAdditiveInverse() {
        WholeAndRt3[][] cases = {
                {one, minusOne},
                {minusOne, one},
                {zero, zero},
                {onePlusRt3, new WholeAndRt3(-1, -1)},
                {maxValue, largeNegative},
                {largeNegative, maxValue},
                {new WholeAndRt3(23, -4), new WholeAndRt3(-23, 4)}
        };
        for (WholeAndRt3[] additiveInverseCase: cases) {
            WholeAndRt3 value = additiveInverseCase[0];
            WholeAndRt3 expectedResult = additiveInverseCase[1];
            Assert.assertEquals(value.additiveInverse(), expectedResult);
        }
        // We cannot get the additive inverse if Long.MIN_VALUE is used since
        // this will cause overflow.
        Assert.assertThrows(RuntimeException.class,
                () -> {  minValue.additiveInverse(); });
    }
}
