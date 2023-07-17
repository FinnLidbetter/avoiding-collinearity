import com.Interval;
import com.SymbolSequence;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class SymbolSequenceTests {

    @Test
    public void testIndexOfLastNewSubword() {
        SymbolSequence sequence = new SymbolSequence(1000);
        Assertions.assertEquals(214, sequence.indexOfLastNewSubword(1));
        Assertions.assertEquals(557, sequence.indexOfLastNewSubword(2));
        Assertions.assertEquals(3904, sequence.indexOfLastNewSubword(3));
        Assertions.assertEquals(3904, sequence.indexOfLastNewSubword(4));
        Assertions.assertEquals(3904, sequence.indexOfLastNewSubword(5));
        Assertions.assertEquals(3904, sequence.indexOfLastNewSubword(6));
        Assertions.assertEquals(3904, sequence.indexOfLastNewSubword(7));
        Assertions.assertEquals(3904, sequence.indexOfLastNewSubword(8));
        Assertions.assertEquals(27334, sequence.indexOfLastNewSubword(9));
        Assertions.assertEquals(27334, sequence.indexOfLastNewSubword(10));
        Assertions.assertEquals(1339414, sequence.indexOfLastNewSubword(2401));
    }

    @Test
    public void testCollinearSearchIntervals() {
        SymbolSequence sequence = new SymbolSequence(1000);
        Interval[] searchIntervals = sequence.getCollinearSearchIntervals(1);
        Interval[] expectedSearchIntervals = new Interval[]{
                new Interval(0,3),
                new Interval(4, 5),
                new Interval(9, 10),
                new Interval(11, 12),
                new Interval(29, 31),
                new Interval(78, 80),
                new Interval(212, 213),
                new Interval(214, 215)
        };
        Assertions.assertArrayEquals(expectedSearchIntervals, searchIntervals);
        searchIntervals = sequence.getCollinearSearchIntervals(2);
        expectedSearchIntervals = new Interval[]{
                new Interval(0,6),
                new Interval(7, 13),
                new Interval(28, 35),
                new Interval(77, 84),
                new Interval(210, 216),
                new Interval(553, 559),
        };
        Assertions.assertArrayEquals(expectedSearchIntervals, searchIntervals);
    }
}
