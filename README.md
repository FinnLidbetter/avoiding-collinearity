# avoiding-collinearity
Code and tests for determining an upper bound on the number of collinear points on an infinite walk on a finite set of vectors in Z^3.

Draw a sequence of trapezoids using:
  ```java DrawTrapezoids wholeAndRt3 343 /home/finn/trapezoids.png```

Assert a bound on the ratio of largest and smallest distances between trapezoids separated by a minimum and maximum number of indices using:
  ```java AssertBoundedDistanceRatio 7 48 wholeAndRt3 9 0```
  
Find the largest count of trapezoids separated by at most a given number of indices that are intersected by a single straight line using:
  ```java CountCollinearTrapezoids 7 wholeAndRt3```
