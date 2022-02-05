# avoiding-collinearity
Code and tests for determining an upper bound on the number of collinear points on an infinite walk on a finite set of vectors in Z^3.

Navigate to the `src` folder and compile everything with `javac *.java`

Get the index of the last new subword of a given length using:

```java IndexOfLastNewSubword 9```

Draw a sequence of trapezoids using:

  ```java DrawTrapezoids wholeAndRt3 343 /home/finn/trapezoids.png```

Assert a bound on the ratio of largest and smallest distances between trapezoids separated by a minimum and maximum number of indices using:

  ```java AssertBoundedDistanceRatio 7 48 wholeAndRt3 9 0```
  
Find the largest count of trapezoids separated by at most a given number of indices that are intersected by a single straight line using:

  ```java CountCollinearTrapezoids 7 wholeAndRt3```

In the count-collinear subdirectory, the Rust program has been run for the first 211,800 points considering all points up to index 10 million. This computation has found that there is no set of 7 collinear points in the first 10 million points of the Gerver-Ramsey construction where at least one point is in the first 211,800 points. This computation has taken approximately one month so far. The computation has been paused and this note is to serve as a reminder for where it should be continued from.
