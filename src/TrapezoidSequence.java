import java.util.ArrayList;
import java.util.List;

public class TrapezoidSequence<T extends AbstractNumber<T>> {
    List<Trapezoid<T>> trapezoids;
    public TrapezoidSequence(int nTrapezoids) {
        trapezoids = new ArrayList<Trapezoid<T>>(nTrapezoids);
    }
}
