package com.numbers;

/**
 * Interface for ensuring that com.Trapezoid sequences can be built with
 * appropriate dimensions.
 *
 * Ideally these methods would be static in the classes that implement them,
 * but there does not seem to be a way to achieve this.
 * @param <T>: a number system.
 */
public interface TrapezoidDimensions<T> {
    T one();
    T two();
    T three();
    T four();
    T five();
    T six();
    T rt3();
    T twoRt3();
    T threeRt3();
}
