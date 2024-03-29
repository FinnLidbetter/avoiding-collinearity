package com;

import com.numbers.AbstractNumber;

public class Vector<T extends AbstractNumber<T>> {
    T x;
    T y;
    public Vector(T x, T y) {
        this.x = x;
        this.y = y;
    }

    /**
     * Construct a vector from two points.
     * @param p1: the tail of the vector.
     * @param p2: the head of the vector.
     */
    public Vector(Point<T> p1, Point<T> p2) {
        this.x = p2.x.subtract(p1.x);
        this.y = p2.y.subtract(p1.y);
    }

    public Vector<T> add(Vector<T> v2) {
        return new Vector<>(x.add(v2.x), y.add(v2.y));
    }
    public Vector<T> subtract(Vector<T> v2) {
        return new Vector<>(x.subtract(v2.x), y.subtract(v2.y));
    }
    public Vector<T> additiveInverse() {
        return new Vector<>(x.additiveInverse(), y.additiveInverse());
    }
    public Vector<T> scale(T m) {
        return new Vector<>(x.multiply(m), y.multiply(m));
    }
    public T cross(Vector<T> v2) {
        return x.multiply(v2.y).subtract(y.multiply(v2.x));
    }
    public T dot(Vector<T> v2) {
        return x.multiply(v2.x).add(y.multiply(v2.y));
    }
    public Vector<T> perpendicular() {
        return new Vector<>(y, x.additiveInverse());
    }
    public boolean equals(Object other) {
        if (other instanceof Vector<?> v2) {
            return x.equals(v2.x) && y.equals(v2.y);
        }
        return false;
    }

    @Override
    public String toString() {
        return String.format("(%s, %s)", x.toString(), y.toString());
    }
}
