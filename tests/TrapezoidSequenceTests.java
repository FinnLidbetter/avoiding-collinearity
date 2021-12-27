import org.junit.Assert;
import org.junit.Test;

public class TrapezoidSequenceTests {
    static PointFactory pf = new PointFactory();
    static Point<Fraction<WholeAndRt3>> zeroPt = pf.makeWholeAndRt3Point(0, 0, 0, 0);

    @Test
    public void testCollinearity() {
        TrapezoidSequence<Fraction<WholeAndRt3>> trapSeq7 =
                new TrapezoidSequence<>(7, zeroPt);
        TrapezoidSequence<Fraction<WholeAndRt3>> trapSeq49 =
                new TrapezoidSequence<>(49, zeroPt);
        Assert.assertEquals(5, trapSeq7.countCollinear(0, 6, 6));
        Assert.assertEquals(5, trapSeq49.countCollinear(0, 6, 6));

        Assert.assertEquals(6, trapSeq49.countCollinear(0, 48, 6));
        Assert.assertEquals(10, trapSeq49.countCollinear(0, 48, 13));
    }

    @Test
    public void testFastCollinearity() {
        //TrapezoidSequence<Fraction<WholeAndRt3>> trapSeq343 = new TrapezoidSequence<>(343, zeroPt);
        TrapezoidSequence<WholeNumber> trapSeq343 = new TrapezoidSequence<>(343, new Point<WholeNumber>(new WholeNumber(0), new WholeNumber(0)));
        for (int minIndex=0; minIndex<100; minIndex++) {
            for (int maxIndex=minIndex+1; maxIndex<200; maxIndex++) {
                for (int maxIndexDiff=1; maxIndexDiff<=Math.min(20, maxIndex - minIndex); maxIndexDiff++) {
                    System.out.printf("Testing (%d, %d, %d)\n", minIndex, maxIndex, maxIndexDiff);
                    Assert.assertEquals(
                            trapSeq343.countCollinear(minIndex, maxIndex, maxIndexDiff),
                            trapSeq343.fastCountCollinear(minIndex, maxIndex, maxIndexDiff)
                    );
                }
            }
        }
    }

    @Test
    public void testCountSubwords(){
        TrapezoidSequence<Fraction<WholeAndRt3>> trapSeq49 =
                new TrapezoidSequence<>(1000001, zeroPt);
        Assert.assertEquals(30, trapSeq49.countSubwords(2401));
    }

    @Test
    public void testSubwordIndex() {
        TrapezoidSequence<Fraction<WholeAndRt3>> trapSeq = new TrapezoidSequence<>(600, zeroPt);

        int upperBoundIndex = trapSeq.indexOfLastNewSubword(3401, null);
        System.out.println(upperBoundIndex);
        int lastNewPositioningIndex = trapSeq.indexOfLastNewRelativePositioning(3401, upperBoundIndex);
        System.out.println(lastNewPositioningIndex);
    }
}
