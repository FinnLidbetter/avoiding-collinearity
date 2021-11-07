/**
 * Class for combining the methods needed by different number systems.
 *
 * One improvement to this would be to have a separate abstract class for
 * implementing Trapezoid dimensions. If that abstract class were to extend
 * the AbstractNumber class, then the extending class could provide concrete
 * implementations for two(), three(), four(), five(), six(), twoRt3(), and
 * threeRt3(), using the implementations of one() and rt3().
 * @param <T>
 */
public abstract class AbstractNumber<T> implements Comparable<T>, Arithmetic<T>, TrapezoidDimensions<T>, Wholes<T> {
    public abstract int compareToZero();
    public abstract double toDouble();
    public abstract String toString();
    public abstract boolean equals(Object o);
}
