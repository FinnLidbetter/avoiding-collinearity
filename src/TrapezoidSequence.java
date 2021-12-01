import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

public class TrapezoidSequence<T extends AbstractNumber<T>> {
    private final TrapezoidFactory<T> tf = new TrapezoidFactory<>();

    private static final int[][] morphism = {
        { 0, 4, 9, 0, 8, 9, 0},
        { 1, 5,10, 1, 6,10, 1},
        { 2, 3,11, 2, 7,11, 2},
        { 3, 2, 6, 3,10, 6, 3},
        { 4, 0, 7, 4,11, 7, 4},
        { 5, 1, 8, 5, 9, 8, 5},
        { 6, 3, 2, 6, 3,10, 6},
        { 7, 4, 0, 7, 4,11, 7},
        { 8, 5, 1, 8, 5, 9, 8},
        { 9, 0, 4, 9, 0, 8, 9},
        {10, 1, 5,10, 1, 6,10},
        {11, 2, 3,11, 2, 7,11},
    };
    private static final int[] trapezoidTypeMap = new int[]{0,3,5,1,2,4,1,2,4,0,3,5};
    private static final int NUM_SYMBOLS = morphism.length;

    ArrayList<Integer> symbolSequence;
    ArrayList<Trapezoid<T>> trapezoids;
    Point<T> startPoint;

    public TrapezoidSequence(int nTrapezoids, Point<T> startPoint) {
        trapezoids = new ArrayList<>(nTrapezoids);
        this.startPoint = startPoint;
        buildSequence(nTrapezoids);
    }

    private void buildSequence(int nTrapezoids) {
        symbolSequence = new ArrayList<>(nTrapezoids);
        int index = 0;
        while (symbolSequence.size() < nTrapezoids) {
            int currMorphismRule = 0;
            if (index != 0)
                currMorphismRule = symbolSequence.get(index);
            int ruleIndex = 0;
            while (symbolSequence.size() < nTrapezoids
                    && ruleIndex < morphism[currMorphismRule].length) {
                symbolSequence.add(morphism[currMorphismRule][ruleIndex]);
                ruleIndex++;
            }
            index++;
        }
        Point<T> prevPoint = startPoint;
        for (int symbol: symbolSequence) {
            int trapType = trapezoidTypeMap[symbol];
            trapezoids.add(tf.makeSequenceTrapezoid(trapType, prevPoint));
            prevPoint = trapezoids.get(trapezoids.size() - 1).vertices.get(3);
        }
    }

    public T getMinDistanceSq(int trapIndex1, int trapIndex2) {
        if (trapIndex1 >= trapezoids.size() || trapIndex2 >= trapezoids.size()) {
            throw new IndexOutOfBoundsException(
                    "Trapezoid index out of bounds.");
        }
        return trapezoids.get(trapIndex1).minDistanceSq(trapezoids.get(trapIndex2));
    }

    public T getMaxDistanceSq(int trapIndex1, int trapIndex2) {
        if (trapIndex1 >= trapezoids.size() || trapIndex2 >= trapezoids.size()) {
            throw new IndexOutOfBoundsException(
                    "Trapezoid index out of bounds.");
        }
        return trapezoids.get(trapIndex1).maxDistanceSq(trapezoids.get(trapIndex2));
    }

    public List<T> getBounds() {
        T xMin = null, xMax = null, yMin = null, yMax = null;
        for (Trapezoid<T> trap: trapezoids) {
            for (Point<T> pt: trap.vertices) {
                if (xMin == null || pt.x.compareTo(xMin) < 0) {
                    xMin = pt.x;
                }
                if (xMax == null || pt.x.compareTo(xMax) > 0) {
                    xMax = pt.x;
                }
                if (yMin == null || pt.y.compareTo(yMin) < 0) {
                    yMin = pt.y;
                }
                if (yMax == null || pt.y.compareTo(yMax) > 0) {
                    yMax = pt.y;
                }
            }
        }
        return Arrays.asList(xMin, yMin, xMax, yMax);
    }

    /**
     * Get the largest number of trapezoids intersected by a single line
     * where no two trapezoids are more than maxIndexDiff indices apart.
     *
     * Where n=maxIndex-minIndex and k=maxIndexDiff, this algorithm is O(nk^2)
     *
     * @param minIndex: the smallest index to consider.
     * @param maxIndex: the largest index to consider.
     * @param maxIndexDiff: the largest difference in indices for a set of
     *      collinear trapezoids.
     * @return The maximum number of trapezoids intersected by an infinite line
     *      subject to the bounds on the indices.
     */
    public int countCollinear(int minIndex, int maxIndex, int maxIndexDiff) {
        int maxCollinear = 0;
        for (int loIndex=minIndex; loIndex < maxIndex; loIndex++) {
            int hiUpperBound = Math.min(loIndex + maxIndexDiff, maxIndex);
            Trapezoid<T> trap1 = trapezoids.get(loIndex);
            for (int hiIndex=loIndex + 1; hiIndex <= hiUpperBound; hiIndex++) {
                Trapezoid<T> trap2 = trapezoids.get(hiIndex);
                //System.out.printf("Traps: %d, %d\n", loIndex, hiIndex);
                for (Point<T> p1: trap1.vertices) {
                    for (Point<T> p2: trap2.vertices) {
                        if (p1.equals(p2))
                            continue;
                        int minIter = Math.max(0, hiIndex - maxIndexDiff);
                        minIter = Math.max(minIndex, minIter);
                        int maxIter = Math.min(trapezoids.size() - 1, loIndex + maxIndexDiff);
                        maxIter = Math.min(maxIndex, maxIter);
                        LinkedList<Integer> intersectIndices = new LinkedList<>();
                        for (int iterIndex=minIter; iterIndex <= maxIter; iterIndex++) {
                            Trapezoid<T> testTrap = trapezoids.get(iterIndex);
                            if (testTrap.intersectsInfiniteLine(p1, p2)) {
                                intersectIndices.offer(iterIndex);
                            }
                            while (intersectIndices.size() > 0 &&
                                    intersectIndices.getLast() - intersectIndices.getFirst() > maxIndexDiff) {
                                intersectIndices.poll();
                            }
                            if (intersectIndices.size() > maxCollinear) {
                                maxCollinear = intersectIndices.size();
                                //System.out.printf("New max: %d\n",maxCollinear);
                            }
                        }
                    }
                }
            }
        }
        return maxCollinear;
    }

    /**
     * Get the largest number of trapezoids intersected by a single line
     * where no two trapezoids are more than maxIndexDiff indices apart.
     *
     * This algorithm uses a radial line sweep approach.
     * Where n=maxIndex-minIndex this algorithm is O(n^2 log^2 n).
     *
     * @param minIndex: the smallest index to consider.
     * @param maxIndex: the largest index to consider.
     * @param maxIndexDiff: the largest difference in indices for a set of
     *      collinear trapezoids.
     * @return The maximum number of trapezoids intersected by an infinite line
     *      subject to the bounds on the indices.
     */
    public int fastCountCollinear(int minIndex, int maxIndex, int maxIndexDiff) {
        // Iterate over all pivot vertices in trapezoids between minIndex and maxIndex.
        int maxCollinear = 0;
        for (int pivotTrapezoidIndex=minIndex; pivotTrapezoidIndex<=maxIndex; pivotTrapezoidIndex++) {
            if (pivotTrapezoidIndex % 100 == 0) {
                System.out.printf("Progress: considering vertices in trapezoid %d as pivots\n", pivotTrapezoidIndex);
            }
            for (Point<T> pivotVertex: trapezoids.get(pivotTrapezoidIndex).vertices) {
                SegmentTreeNode activeTrapezoidsRoot = new SegmentTreeNode(0, maxIndex + maxIndexDiff);
                Point<T> pivotPositiveDirectionPoint = new Point<>(pivotVertex.x.add(pivotVertex.x.one()), pivotVertex.y);
                ArrayList<EventPoint<T>> eventPoints = new ArrayList<>();
                PointComparator<T> positivePointComparator = new PointComparator<>(pivotVertex, true);
                PointComparator<T> negativePointComparator = new PointComparator<>(pivotVertex, false);
                EventPointComparator<T> eventPointComparator = new EventPointComparator<>(pivotVertex);
                // For each other trapezoid sort the 4 vertices relative to the pivot and identify enter and exit vertices.
                int currIndexMin = Math.max(pivotTrapezoidIndex - maxIndexDiff, minIndex);
                int currIndexMax = Math.min(pivotTrapezoidIndex + maxIndexDiff, maxIndex);
                for (int currTrapezoidIndex=currIndexMin; currTrapezoidIndex<=currIndexMax; currTrapezoidIndex++) {
                    Trapezoid<T> currentTrapezoid = trapezoids.get(currTrapezoidIndex);
                    if (currentTrapezoid.contains(pivotVertex)) {
                        // Every line through the pivot intersects this trapezoid, so
                        // there are no enter and exit vertices.
                        activeTrapezoidsRoot.update(currTrapezoidIndex, currTrapezoidIndex + maxIndexDiff, 1);
                        maxCollinear = Math.max(maxCollinear, activeTrapezoidsRoot.max(0, maxIndex + maxIndexDiff));
                        continue;
                    }
                    // Initialize a sortable list of the trapezoid vertices.
                    ArrayList<Point<T>> trapPoints = new ArrayList<>(4);
                    trapPoints.addAll(currentTrapezoid.vertices);
                    trapPoints.sort(positivePointComparator);
                    if (currentTrapezoid.intersectsSemiInfiniteLine(pivotVertex, pivotPositiveDirectionPoint)
                            && (trapPoints.get(0).y.compareTo(pivotVertex.y) != 0 || trapPoints.get(3).y.compareTo(pivotVertex.y) <= 0)) {

                        // If the trapezoid intersects the initial sweep line (asterisk), then sort relative
                        // to a sweep line starting pointing in the opposite direction, but still rotating
                        // in the same (counter-clockwise) direction.
                        //  Asterisk: the intersection is ignored if the whole trapezoid is at or above the sweep line.
                        trapPoints.sort(negativePointComparator);
                        // The sweep line starts intersecting the trapezoid, so increment the initial counter.
                        activeTrapezoidsRoot.update(currTrapezoidIndex, currTrapezoidIndex + maxIndexDiff, 1);
                        maxCollinear = Math.max(maxCollinear, activeTrapezoidsRoot.max(0, maxIndex + maxIndexDiff));
                    }
                    Point<T> startPoint = trapPoints.get(0);
                    Point<T> endPoint = trapPoints.get(trapPoints.size() - 1);
                    eventPoints.add(new EventPoint<>(startPoint, currTrapezoidIndex, true));
                    eventPoints.add(new EventPoint<>(endPoint, currTrapezoidIndex, false));
                }
                // Sort all enter and exit vertices relative to the pivot.
                eventPoints.sort(eventPointComparator);
                // Iterate over enter and exit vertices and insert trapezoid index into a fenwick tree
                for (EventPoint<T> eventPoint: eventPoints) {
                    if (eventPoint.isStart) {
                        // Insert the corresponding trapezoid into the Segment tree.
                        activeTrapezoidsRoot.update(eventPoint.trapezoidIndex, eventPoint.trapezoidIndex + maxIndexDiff, 1);
                        maxCollinear = Math.max(maxCollinear, activeTrapezoidsRoot.max(0, maxIndex + maxIndexDiff));
                        // Check the count of inserted vertices `maxIndexDiff` either side of the inserted index.
                    } else {
                        // Remove the corresponding trapezoid from the Fenwick tree.
                        activeTrapezoidsRoot.update(eventPoint.trapezoidIndex, eventPoint.trapezoidIndex + maxIndexDiff, -1);
                    }
                }
            }
        }
        return maxCollinear;
    }

    public int countSubwords(int length) {
        if (length >= symbolSequence.size())
            return 0;
        HashSet<BigInteger> wordSet = new HashSet<>();
        BigInteger base = new BigInteger("" + NUM_SYMBOLS);
        BigInteger maxPow = base.pow(length-1);
        BigInteger word = BigInteger.ZERO;
        for (int i=0; i < length; i++) {
            word = word.multiply(base);
            word = word.add(new BigInteger("" + symbolSequence.get(i)));
        }
        wordSet.add(word);
        for (int j=length; j < symbolSequence.size(); j++) {
            word = word.subtract(maxPow.multiply(new BigInteger("" + symbolSequence.get(j - length))));
            word = word.multiply(base);
            word = word.add(new BigInteger("" + symbolSequence.get(j)));
            wordSet.add(word);
        }
        return wordSet.size();
    }

    private T loDistanceSq(int minIndex, int maxIndex, int gap) {
        T minMinDistanceSq = null;
        for (int index=minIndex; index<=maxIndex; index++) {
            T minDistanceSq1 = getMinDistanceSq(index, index + gap);
            T minDistanceSq2 = getMinDistanceSq(index, index + gap + 1);
            if (minMinDistanceSq == null || minDistanceSq1.compareTo(minMinDistanceSq) < 0) {
                minMinDistanceSq = minDistanceSq1;
            }
            if (minDistanceSq2.compareTo(minMinDistanceSq) < 0){
                minMinDistanceSq = minDistanceSq2;
            }
        }
        return minMinDistanceSq;
    }
    private T hiDistanceSq(int minIndex, int maxIndex, int gap) {
        T maxMaxDistanceSq = null;
        for (int index=minIndex; index<=maxIndex; index++) {
            T maxDistanceSq1 = getMaxDistanceSq(index, index + gap);
            T maxDistanceSq2 = getMaxDistanceSq(index, index + gap + 1);
            if (maxMaxDistanceSq == null || maxDistanceSq1.compareTo(maxMaxDistanceSq) > 0) {
                maxMaxDistanceSq = maxDistanceSq1;
            }
            if (maxDistanceSq2.compareTo(maxMaxDistanceSq) > 0) {
                maxMaxDistanceSq = maxDistanceSq2;
            }
        }
        return maxMaxDistanceSq;
    }

    public boolean assertBoundedRatio(int gapMin, int gapMax, int startIndex, int endIndex, T baseUpperBound) {
        for (int cGap=gapMin; cGap<=gapMax; cGap++) {
            T loDistanceSq = loDistanceSq(startIndex, endIndex, cGap);
            for (int dGap=gapMin; dGap<=gapMax; dGap++) {
                T hiDistanceSq = hiDistanceSq(startIndex, endIndex, dGap);
                // Want to assert ((c+1)*sqrt(hiDistanceSq(d))) / (d*sqrt(loDistanceSq(c))) < baseUpperBound
                // All quantities are greater than 0, so evaluate:
                //    hiDistanceSq(d) / loDistanceSq(c) < ((d * baseUpperBound) / (c+1))^2
                T lhs = hiDistanceSq.divide(loDistanceSq);
                T rhsSqrt = baseUpperBound.multiply(baseUpperBound.whole(dGap)).divide(baseUpperBound.whole(cGap + 1));
                T rhs = rhsSqrt.multiply(rhsSqrt);
                if (lhs.compareTo(rhs) >= 0) {
                    return false;
                }
            }
        }
        return true;
    }
}
