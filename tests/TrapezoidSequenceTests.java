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
    public void testCountSubwords(){
        TrapezoidSequence<Fraction<WholeAndRt3>> trapSeq49 =
                new TrapezoidSequence<>(51001, zeroPt);
        Assert.assertEquals(16, trapSeq49.countSubwords(7));
    }
}
