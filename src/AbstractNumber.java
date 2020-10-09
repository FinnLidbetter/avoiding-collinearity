public abstract class AbstractNumber<T> implements Comparable<T>, Arithmetic<T> {
    public abstract int compareToZero();
    public abstract double toDouble();
    public abstract String toString();
    public abstract boolean equals(Object o);
}
