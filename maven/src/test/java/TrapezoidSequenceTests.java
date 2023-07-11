import com.Interval;
import com.Point;
import com.PointFactory;
import com.TrapezoidSequence;
import com.numbers.DoubleRep;
import com.numbers.Fraction;
import com.numbers.WholeAndRt3;
import com.numbers.WholeNumber;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

public class TrapezoidSequenceTests {
    static PointFactory pf = new PointFactory();
    static Point<Fraction<WholeAndRt3>> zeroPt = pf.makeWholeAndRt3Point(0, 0, 0, 0);

    @Test
    public void testCollinearity() {
        TrapezoidSequence<Fraction<WholeAndRt3>> trapSeq7 =
                new TrapezoidSequence<>(7, zeroPt);
        TrapezoidSequence<Fraction<WholeAndRt3>> trapSeq49 =
                new TrapezoidSequence<>(49, zeroPt);
        Assertions.assertEquals(5, trapSeq7.countCollinear(0, 6, 6));
        Assertions.assertEquals(5, trapSeq49.countCollinear(0, 6, 6));

        Assertions.assertEquals(6, trapSeq49.countCollinear(0, 48, 6));
        Assertions.assertEquals(6, trapSeq49.radialSweepCountCollinear(0, 48, 6).numTrapezoidsIntersected);
        Assertions.assertEquals(10, trapSeq49.countCollinear(0, 48, 13));
        Assertions.assertEquals(10, trapSeq49.radialSweepCountCollinear(0, 48, 13).numTrapezoidsIntersected);
    }

    /*
    @Test
    public void testFastCollinearity() {
        TrapezoidSequence<WholeNumber> trapSeq343 = new TrapezoidSequence<>(343, new Point<WholeNumber>(new WholeNumber(0), new WholeNumber(0)));
        for (int minIndex=0; minIndex<100; minIndex++) {
            for (int maxIndex=minIndex+1; maxIndex<200; maxIndex++) {
                for (int maxIndexDiff=1; maxIndexDiff<=Math.min(20, maxIndex - minIndex); maxIndexDiff++) {
                    System.out.printf("Testing (%d, %d, %d)\n", minIndex, maxIndex, maxIndexDiff);
                    Assertions.assertEquals(
                            trapSeq343.countCollinear(minIndex, maxIndex, maxIndexDiff),
                            trapSeq343.radialSweepCountCollinear(minIndex, maxIndex, maxIndexDiff).numTrapezoidsIntersected
                    );
                }
            }
        }
    }
    */

    @Test
    public void testIntervals() {
        TrapezoidSequence<Fraction<WholeAndRt3>> trapSeq = new TrapezoidSequence<>(2, zeroPt);
        Interval[] collinearSearchIntervals = trapSeq.getCollinearSearchIntervals(2401);
        // Expected values computed by an earlier implementation.
        Interval[] expectedIntervals = new Interval[]{
                new Interval(0, 14061),
                new Interval(16809, 30868),
                new Interval(67229, 76831),
                new Interval(76833, 83689),
                new Interval(184879, 194480),
                new Interval(194482, 201339),
                new Interval(504212, 518270),
                new Interval(1327757, 1341814)
        };
        Assertions.assertArrayEquals(expectedIntervals, collinearSearchIntervals);
    }

    /*
    @Test
    public void testLoDistanceSquared() {
        Point<DoubleRep> pt = new Point<>(new DoubleRep(0), new DoubleRep(0));
        int n = 1000000;
        TrapezoidSequence<DoubleRep> trapSeq = new TrapezoidSequence<>(n, pt);
        DoubleRep maxVal = new DoubleRep(0);
        for (int i=0; i<n-16898; i++) {
            if (i%10000==0) {
                System.out.printf("Progress %d\n", i);
            }
            for (int j=2401; j<16897; j++) {
                DoubleRep curr = (new DoubleRep(j+1)).divide(trapSeq.getMinDistanceSq(i, i+j).sqrt());
                if (curr.compareTo(maxVal) > 0) {
                    maxVal = curr;
                }
            }
        }
        System.out.println(maxVal);
    }
    */

    /*
    @Test
    public void testHiDistanceSquared() {
        Point<DoubleRep> pt = new Point<>(new DoubleRep(0), new DoubleRep(0));
        int n = 1000000;
        TrapezoidSequence<DoubleRep> trapSeq = new TrapezoidSequence<>(n, pt);
        DoubleRep maxVal = new DoubleRep(0);
        for (int i=0; i<n-16898; i++) {
            if (i%10000==0) {
                System.out.printf("Progress %d\n", i);
            }
            for (int j=2401; j<16897; j++) {
                DoubleRep curr = trapSeq.getMaxDistanceSq(i, i+j).sqrt().divide(new DoubleRep(j));
                if (curr.compareTo(maxVal) > 0) {
                    maxVal = curr;
                }
            }
        }
        System.out.println(maxVal);
    }
     */

    @Test
    public void testRadialSweep() {
        TrapezoidSequence<Fraction<WholeAndRt3>> trapSeq = new TrapezoidSequence<>(6002, zeroPt);
        int result = trapSeq.radialSweepCountCollinear(0, 345, 343).numTrapezoidsIntersected;
        Assertions.assertEquals(62, result);
    }

    /*
    @Test
    public void testAllIntervalsCollinearity() {
        TrapezoidSequence<Fraction<WholeAndRt3>> trapSeq = new TrapezoidSequence<>(2, zeroPt);
        Interval[] collinearSearchIntervals = trapSeq.getCollinearSearchIntervals(2401);
        int overallMaxCollinear = 0;
        System.out.println(Arrays.toString(collinearSearchIntervals));
        long startTime = System.currentTimeMillis();
        for (Interval interval: collinearSearchIntervals) {
            System.out.printf("Starting interval %s\n", interval);
            int maxCollinear =  trapSeq.radialSweepCountCollinear(interval.getLo(), interval.getHi(), 2401).numTrapezoidsIntersected;
            if (maxCollinear > overallMaxCollinear) {
                overallMaxCollinear = maxCollinear;
            }
            long checkpoint = System.currentTimeMillis();
            System.out.printf("Expended %d seconds\n", (checkpoint - startTime) / 1000);
            System.out.printf("Overall max collinear is currently %d\n", overallMaxCollinear);
        }
        System.out.printf("Final max collinear is %d\n", overallMaxCollinear);
    }
     */
}
