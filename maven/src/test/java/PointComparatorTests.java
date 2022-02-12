import com.Point;
import com.PointComparator;
import com.PointFactory;
import com.numbers.WholeNumber;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Tests for the com.PointComparator class.
 */
public class PointComparatorTests {
    PointFactory pf = new PointFactory();

    Point<WholeNumber> p00 = pf.makePoint(0, 0);
    Point<WholeNumber> p21 = pf.makePoint(2, 1);
    Point<WholeNumber> p42 = pf.makePoint(4, 2);
    Point<WholeNumber> p10 = pf.makePoint(1, 0);
    Point<WholeNumber> p30 = pf.makePoint(3, 0);
    Point<WholeNumber> p02 = pf.makePoint(0, 2);
    Point<WholeNumber> p09 = pf.makePoint(0, 9);
    Point<WholeNumber> pn19 = pf.makePoint(-1, 9);
    Point<WholeNumber> pn11 = pf.makePoint(-1, 1);
    Point<WholeNumber> pn10 = pf.makePoint(-1, 0);
    Point<WholeNumber> pn50 = pf.makePoint(-5, 0);
    Point<WholeNumber> pn4n1 = pf.makePoint(-4, -1);
    Point<WholeNumber> pn2n3 = pf.makePoint(-2, -3);
    Point<WholeNumber> p0n7 = pf.makePoint(0, -7);
    Point<WholeNumber> p0n8 = pf.makePoint(0, -8);
    Point<WholeNumber> p1n6 = pf.makePoint(1, -6);
    Point<WholeNumber> p3n3 = pf.makePoint(3, -3);

    @Test
    public void testZeroPointStartRightComparator() {
        Point<WholeNumber> pivot = p00;
        PointComparator<WholeNumber> rightComparator00 = new PointComparator<>(pivot, true);
        List<Point<WholeNumber>> pts = Arrays.asList(
            p21, p42, p10, p30, p02, p09, pn19, pn11, pn10, pn50, pn4n1, pn2n3, p0n7, p0n8, p1n6, p3n3
        );
        List<Point<WholeNumber>> expectedOrder = Arrays.asList(
            p10, p30, p21, p42, p02, p09, pn19, pn11, pn10, pn50, pn4n1, pn2n3, p0n7, p0n8, p1n6, p3n3
        );

        for (int i=0; i<pts.size(); i++) {
            for (int j=0; j<pts.size(); j++) {
                int expectedComparisonResult = (int) Math.signum(i-j);
                Assertions.assertEquals(
                        expectedComparisonResult,
                        rightComparator00.compare(expectedOrder.get(i), expectedOrder.get(j))
                );
            }
        }
        Assertions.assertEquals(pts.size(), expectedOrder.size());
        for (int shuffles=0; shuffles<10; shuffles++) {
            Collections.shuffle(pts);
            pts.sort(rightComparator00);
            for (int i = 0; i < pts.size(); i++) {
                Assertions.assertEquals(expectedOrder.get(i), pts.get(i));
            }
        }
    }

    @Test
    public void testNonzerPointStartRightComparator() {
        Point<WholeNumber> pivot = p21;
        PointComparator<WholeNumber> rightComparator21 = new PointComparator<>(pivot, true);
        List<Point<WholeNumber>> pts = Arrays.asList(
                p00, p42, p10, p30, p02, p09, pn19, pn11, pn10, pn50, pn4n1, pn2n3, p0n7, p0n8, p1n6, p3n3
        );
        List<Point<WholeNumber>> expectedOrder = Arrays.asList(
                p42, p09, pn19, p02, pn11, pn50, pn10, pn4n1, p00, p10, pn2n3, p0n7, p0n8, p1n6, p3n3, p30
        );
        for (int i=0; i<pts.size(); i++) {
            for (int j=0; j<pts.size(); j++) {
                int expectedComparisonResult = (int) Math.signum(i-j);
                Assertions.assertEquals(
                        expectedComparisonResult,
                        rightComparator21.compare(expectedOrder.get(i), expectedOrder.get(j))
                );
            }
        }
        Assertions.assertEquals(pts.size(), expectedOrder.size());
        for (int shuffles=0; shuffles<10; shuffles++) {
            Collections.shuffle(pts);
            pts.sort(rightComparator21);
            for (int i = 0; i < pts.size(); i++) {
                Assertions.assertEquals(expectedOrder.get(i), pts.get(i));
            }
        }
    }

    @Test
    public void testZeroPointStartLeftComparator() {
        Point<WholeNumber> pivot = p00;
        PointComparator<WholeNumber> leftComparator00 = new PointComparator<>(pivot, false);
        List<Point<WholeNumber>> pts = Arrays.asList(
                p21, p42, p10, p30, p02, p09, pn19, pn11, pn10, pn50, pn4n1, pn2n3, p0n7, p0n8, p1n6, p3n3
        );
        List<Point<WholeNumber>> expectedOrder = Arrays.asList(
                pn10, pn50, pn4n1, pn2n3, p0n7, p0n8, p1n6, p3n3, p10, p30, p21, p42, p02, p09, pn19, pn11
                );

        for (int i=0; i<pts.size(); i++) {
            for (int j=0; j<pts.size(); j++) {
                int expectedComparisonResult = (int) Math.signum(i-j);
                Assertions.assertEquals(
                        expectedComparisonResult,
                        leftComparator00.compare(expectedOrder.get(i), expectedOrder.get(j))
                );
            }
        }
        Assertions.assertEquals(pts.size(), expectedOrder.size());
        for (int shuffles=0; shuffles<10; shuffles++) {
            Collections.shuffle(pts);
            pts.sort(leftComparator00);
            for (int i = 0; i < pts.size(); i++) {
                Assertions.assertEquals(expectedOrder.get(i), pts.get(i));
            }
        }
    }

    @Test
    public void testNonzerPointStartLeftComparator() {
        Point<WholeNumber> pivot = p21;
        PointComparator<WholeNumber> leftComparator21 = new PointComparator<>(pivot, false);
        List<Point<WholeNumber>> pts = Arrays.asList(
                p00, p42, p10, p30, p02, p09, pn19, pn11, pn10, pn50, pn4n1, pn2n3, p0n7, p0n8, p1n6, p3n3
        );
        List<Point<WholeNumber>> expectedOrder = Arrays.asList(
                pn11, pn50, pn10, pn4n1, p00, p10, pn2n3, p0n7, p0n8, p1n6, p3n3, p30, p42, p09, pn19, p02
                );
        for (int i=0; i<pts.size(); i++) {
            for (int j=0; j<pts.size(); j++) {
                int expectedComparisonResult = (int) Math.signum(i-j);
                Assertions.assertEquals(
                        expectedComparisonResult,
                        leftComparator21.compare(expectedOrder.get(i), expectedOrder.get(j))
                );
            }
        }
        Assertions.assertEquals(pts.size(), expectedOrder.size());
        for (int shuffles=0; shuffles<10; shuffles++) {
            Collections.shuffle(pts);
            pts.sort(leftComparator21);
            for (int i = 0; i < pts.size(); i++) {
                Assertions.assertEquals(expectedOrder.get(i), pts.get(i));
            }
        }
    }
}
