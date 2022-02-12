import com.Point;
import com.PointFactory;
import com.numbers.WholeNumber;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class PointTests {
    private static final PointFactory pf = new PointFactory();

    @Test
    public void testDistanceSq() {
        long[][][] cases = {
                {{0, 0}, {3, 4}, {25}},
                {{5, 6}, {4, 7}, {2}},
                {{-3, -3}, {-3, -3}, {0}},
                {{33, -3}, {76, 39}, {43 * 43 + 42*42}}
        };
        for (long[][] distanceCase: cases) {
            Point<WholeNumber> p1 = pf.makePoint(
                    distanceCase[0][0], distanceCase[0][1]);
            Point<WholeNumber> p2 = pf.makePoint(
                    distanceCase[1][0], distanceCase[1][1]);
            WholeNumber expectedDistanceSq =
                    new WholeNumber(distanceCase[2][0]);
            Assertions.assertEquals(expectedDistanceSq, p1.distanceSq(p2));
            Assertions.assertEquals(expectedDistanceSq, p2.distanceSq(p1));
        }
    }
}
