import com.Vector;
import com.VectorFactory;
import com.numbers.WholeNumber;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import java.util.Arrays;

public class VectorTests {
    private static final VectorFactory vf = new VectorFactory();

    /**
     * Test adding two vectors.
     */
    @Test
    public void testAdd() {
        long[][][] cases = {
                {{14, 23}, {0, 0}, {14, 23}},
                {{0, 0}, {14, 23}, {14, 23}},
                {{0, 0}, {0, 0}, {0, 0}},
                {{-3, -5}, {-2, -1}, {-5, -6}},
                {{18, -1}, {-15, 4}, {3, 3}}
        };
        for (long[][] additionCase: cases) {
            System.out.println(Arrays.deepToString(additionCase));
            Vector<WholeNumber> leftSummand = vf.makeVector(
                    additionCase[0][0], additionCase[0][1]);
            Vector<WholeNumber> rightSummand = vf.makeVector(
                    additionCase[1][0], additionCase[1][1]);
            Vector<WholeNumber> expectedSum = vf.makeVector(
                    additionCase[2][0], additionCase[2][1]);
            Assertions.assertEquals(expectedSum, leftSummand.add(rightSummand));
        }
    }

    /**
     * Test subtracting one vector from another.
     */
    @Test
    public void testSubtract() {
        long[][][] cases = {
                {{14, 23}, {0, 0}, {14, 23}},
                {{0, 0}, {14, 23}, {-14, -23}},
                {{0, 0}, {0, 0}, {0, 0}},
                {{-3, -5}, {-2, -1}, {-1, -4}},
                {{18, -1}, {-15, 4}, {33, -5}}
        };
        for (long[][] subtractionCase: cases) {
            System.out.println(Arrays.deepToString(subtractionCase));
            Vector<WholeNumber> leftSummand = vf.makeVector(
                    subtractionCase[0][0], subtractionCase[0][1]);
            Vector<WholeNumber> rightSummand = vf.makeVector(
                    subtractionCase[1][0], subtractionCase[1][1]);
            Vector<WholeNumber> expectedDifference = vf.makeVector(
                    subtractionCase[2][0], subtractionCase[2][1]);
            Assertions.assertEquals(
                    expectedDifference, leftSummand.subtract(rightSummand));
        }
    }

    /**
     * Test getting the additive inverse of a vector.
     */
    @Test
    public void testAdditiveInverse() {
        long[][][] cases = {
                {{14, 23}, {-14, -23}},
                {{0, 0}, {0, 0}},
                {{-3, -5}, {3, 5}},
                {{18, -1}, {-18, 1}},
                {{-18, 1}, {18, -1}},
        };
        for (long[][] additiveInverseCase: cases) {
            System.out.println(Arrays.deepToString(additiveInverseCase));
            Vector<WholeNumber> value = vf.makeVector(
                    additiveInverseCase[0][0], additiveInverseCase[0][1]);
            Vector<WholeNumber> expectedInverse = vf.makeVector(
                    additiveInverseCase[1][0], additiveInverseCase[1][1]);
            Assertions.assertEquals(
                    expectedInverse, value.additiveInverse());
        }
    }

    /**
     * Test multiplying a vector by a scalar.
     */
    @Test
    public void testScale() {
        long[][][] cases = {
                {{0, 0}, {0}, {0, 0}},
                {{0, 0}, {3}, {0, 0}},
                {{0, 0}, {-2}, {0, 0}},
                {{3, 2}, {3}, {9, 6}},
                {{3, 2}, {-4}, {-12, -8}},
                {{-5, -7}, {2}, {-10, -14}},
                {{-5, -7}, {-3}, {15, 21}},
                {{3, -2}, {5}, {15, -10}},
                {{3, -2}, {-5}, {-15, 10}},
                {{-3, 2}, {5}, {-15, 10}},
                {{-3, 2}, {-5}, {15, -10}}
        };
        for (long[][] scaleCase: cases) {
            System.out.println(Arrays.deepToString(scaleCase));
            Vector<WholeNumber> vector = vf.makeVector(
                    scaleCase[0][0], scaleCase[0][1]);
            WholeNumber scalar = new WholeNumber(scaleCase[1][0]);
            Vector<WholeNumber> expectedResult = vf.makeVector(
                    scaleCase[2][0], scaleCase[2][1]);
            Assertions.assertEquals(
                    expectedResult, vector.scale(scalar));
        }
    }

    /**
     * Test getting the cross product of a pair of 2D vectors.
     *
     * The input vectors have a third component with value 0.
     * The result of this cross product is given just as the third component
     * of the cross product of the vectors.
     */
    @Test
    public void testCross() {
        long[][][] cases = {
                {{0, 0}, {0, 0}, {0}},
                // Collinear vectors have a cross product of 0.
                {{1, 1}, {1, 1}, {0}},
                {{4, 2}, {-2, -1}, {0}},
                // Cross product with the zero vector.
                {{3, 4}, {0, 0}, {0}},
                // General cases.
                {{4, 3}, {0, 4}, {16}},
                {{2, -1}, {-3, 4}, {5}},
                {{-2, -5}, {3, 10}, {-5}}
        };
        for (long[][] crossCase: cases) {
            System.out.println(Arrays.deepToString(crossCase));
            Vector<WholeNumber> leftTerm = vf.makeVector(
                    crossCase[0][0], crossCase[0][1]);
            Vector<WholeNumber> rightTerm = vf.makeVector(
                    crossCase[1][0], crossCase[1][1]);
            WholeNumber expectedResult = new WholeNumber(crossCase[2][0]);
            Assertions.assertEquals(
                    expectedResult, leftTerm.cross(rightTerm));
        }
    }

    /**
     * Test getting a perpendicular vector in the plane.
     */
    @Test
    public void testPerpendicular() {
        long[][][] cases = {
                {{0, 0}, {0, 0}},
                {{13, 2}, {2, -13}},
                {{4, 0}, {0, -4}},
                {{0, 4}, {4, 0}},
                {{-4, 0}, {0, 4}},
                {{0, -4}, {-4, 0}}
        };
        for (long[][] perpendicularCase: cases) {
            System.out.println(Arrays.deepToString(perpendicularCase));
            Vector<WholeNumber> vector = vf.makeVector(
                    perpendicularCase[0][0], perpendicularCase[0][1]);
            Vector<WholeNumber> expectedPerpendicular = vf.makeVector(
                    perpendicularCase[1][0], perpendicularCase[1][1]);
            Assertions.assertEquals(expectedPerpendicular, vector.perpendicular());
        }
    }
}
