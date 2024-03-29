package com;

import com.numbers.AbstractNumber;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

public class TrapezoidSequence<T extends AbstractNumber<T>> {
    private final TrapezoidFactory<T> tf = new TrapezoidFactory<>();

    public static final TrapezoidType[] trapezoidTypeMap = {
            TrapezoidType.ZERO, TrapezoidType.TWO, TrapezoidType.FIVE, TrapezoidType.ONE, TrapezoidType.THREE, TrapezoidType.FOUR,
            TrapezoidType.ONE, TrapezoidType.THREE, TrapezoidType.FOUR, TrapezoidType.ZERO, TrapezoidType.TWO, TrapezoidType.FIVE
    };

    SymbolSequence symbolSequence;
    ArrayList<TrapezoidType> trapezoidTypeSequence;
    ArrayList<Trapezoid<T>> trapezoids;
    Point<T> startPoint;

    public TrapezoidSequence(int nTrapezoids, Point<T> startPoint) {
        trapezoids = new ArrayList<>(nTrapezoids);
        this.startPoint = startPoint;
        symbolSequence = new SymbolSequence(nTrapezoids);
        buildTrapezoidTypeSequenceFromSymbolSequence();
        buildTrapezoidSequenceFromTrapezoidTypeSequence();
    }

    /**
     * Construct the trapezoid type sequence from the symbol sequence.
     */
    private void buildTrapezoidTypeSequenceFromSymbolSequence() {
        trapezoidTypeSequence = new ArrayList<>(symbolSequence.sequence.size());
        for (int symbol: symbolSequence.sequence) {
            trapezoidTypeSequence.add(trapezoidTypeMap[symbol]);
        }
    }

    /**
     * Construct trapezoids from the trapezoid type sequence.
     */
    private void buildTrapezoidSequenceFromTrapezoidTypeSequence() {
        Point<T> prevPoint = startPoint;
        trapezoids = new ArrayList<>();
        for (TrapezoidType trapezoidType: trapezoidTypeSequence) {
            trapezoids.add(tf.makeSequenceTrapezoid(trapezoidType, prevPoint));
            prevPoint = trapezoids.get(trapezoids.size() - 1).vertices.get(3);
        }
    }


    /**
     * Get a canonical string representation for a trapezoid sequence that captures relative positioning information.
     *
     * The first element is normalized to the identity element in the group. All others are normalized by
     * applying the same group operation to each element of the sequence, i.e., by applying the inverse of
     * the first element.
     * @param startIndex: the start index for the sequence.
     * @param sequenceLength: the length of the sequence.
     * @return A string canonical representation for the subsequence.
     */
    private String positioningCanonicalString(int startIndex, int sequenceLength) {
        StringBuilder sb = new StringBuilder();
        TrapezoidType normalizer = trapezoidTypeSequence.get(startIndex).inverse();
        for (int i=0; i<sequenceLength; i++) {
            sb.append(trapezoidTypeSequence.get(startIndex + i).multiply(normalizer).toString());
        }
        return sb.toString();
    }

    /**
     * Get the index of the last new relative positioning of a sequence of the specified length.
     *
     * @param sequenceLength: the length of the sequence.
     * @return the index of the last new relative positioning.
     */
    public int indexOfLastNewRelativePositioning(int sequenceLength) {
        int upperBoundIndex = symbolSequence.indexOfLastNewSubword(sequenceLength) + sequenceLength;
        if (upperBoundIndex + sequenceLength > trapezoidTypeSequence.size()) {
            symbolSequence.extendSequenceToLength(upperBoundIndex + sequenceLength);
            buildTrapezoidTypeSequenceFromSymbolSequence();
        }
        HashSet<String> canonicalPositionings = new HashSet<>();
        int lastNewIndex = 0;
        for (int i=0; i<upperBoundIndex; i++) {
            int prevSize = canonicalPositionings.size();
            canonicalPositionings.add(positioningCanonicalString(i, sequenceLength));
            if (canonicalPositionings.size() > prevSize) {
                lastNewIndex = i;
            }
        }
        return lastNewIndex;
    }

    /**
     * Get intervals of indices to check for the largest number of collinear trapezoids.
     *
     * Indices that are not within sequenceLength indices of the first occurrence of a
     * distinct canonical positioning of trapezoids are not included. Some indices that
     * are redundant to check are excluded.
     * @param sequenceLength: the length of the sequence to check collinearity for
     *     (the maximum difference in indices allowed for two of the trapezoids
     *      intersected by a single line).
     * @return the intervals to check for collinear trapezoids.
     */
    public Interval[] getCollinearSearchIntervals(int sequenceLength) {
        int upperBoundIndex = symbolSequence.indexOfLastNewSubword(sequenceLength) + sequenceLength;
        if (upperBoundIndex + sequenceLength > trapezoidTypeSequence.size()) {
            symbolSequence.extendSequenceToLength(upperBoundIndex + sequenceLength);
            buildTrapezoidTypeSequenceFromSymbolSequence();
        }
        HashSet<String> canonicalPositionings = new HashSet<>();
        ArrayList<Interval> collinearSearchIntervals = new ArrayList<>();
        Interval activeInterval = new Interval(0, sequenceLength);
        for (int i=0; i<upperBoundIndex; i++) {
            String positioningString = positioningCanonicalString(i, sequenceLength);
            int currLo = i;
            int currHi = i + sequenceLength;
            if (!canonicalPositionings.contains(positioningString)) {
                if (activeInterval.hi >= currLo) {
                    activeInterval = new Interval(activeInterval.lo, currHi);
                } else {
                    collinearSearchIntervals.add(activeInterval);
                    activeInterval = new Interval(currLo, currHi);
                }
            }
            canonicalPositionings.add(positioningString);
        }
        collinearSearchIntervals.add(activeInterval);
        Interval[] result = new Interval[collinearSearchIntervals.size()];
        for (int i=0; i<collinearSearchIntervals.size(); i++) {
            result[i] = collinearSearchIntervals.get(i);
        }
        return result;
    }

    /**
     * Get the smallest distance squared between points in a pair of trapezoids.
     */
    public T getMinDistanceSq(int trapIndex1, int trapIndex2) {
        if (trapIndex1 >= trapezoids.size() || trapIndex2 >= trapezoids.size()) {
            throw new IndexOutOfBoundsException(
                    "com.Trapezoid index out of bounds.");
        }
        return trapezoids.get(trapIndex1).minDistanceSq(trapezoids.get(trapIndex2));
    }

    /**
     * Get the largest distance squared between points in a pair of trapezoids.
     */
    public T getMaxDistanceSq(int trapIndex1, int trapIndex2) {
        if (trapIndex1 >= trapezoids.size() || trapIndex2 >= trapezoids.size()) {
            throw new IndexOutOfBoundsException(
                    "com.Trapezoid index out of bounds.");
        }
        return trapezoids.get(trapIndex1).maxDistanceSq(trapezoids.get(trapIndex2));
    }

    /**
     * Get the smallest and largest x and y coordinates of constructed trapezoids.
     */
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
     * Where n=maxIndex-minIndex and maxIndexDiff<n, this algorithm is O(n^2 log n).
     *
     * @param minIndex: the smallest index to consider.
     * @param maxIndex: the largest index to consider.
     * @param maxIndexDiff: the largest difference in indices for a set of
     *      collinear trapezoids.
     * @return The maximum number of trapezoids intersected by an infinite line
     *      subject to the bounds on the indices.
     */
    public TrapezoidIntersectionPair<T> radialSweepCountCollinear(int minIndex, int maxIndex, int maxIndexDiff) {
        ArrayList<Trapezoid<T>> trapezoidRange = new ArrayList<>(maxIndex - minIndex + 1);
        if (maxIndex + 1 > trapezoidTypeSequence.size()) {
            symbolSequence.extendSequenceToLength(2 * maxIndex);
            buildTrapezoidTypeSequenceFromSymbolSequence();
        }
        Point<T> prevPoint = startPoint;
        for (int i=minIndex; i<=maxIndex; i++) {
            Trapezoid<T> nextTrapezoid = tf.makeSequenceTrapezoid(trapezoidTypeSequence.get(i), prevPoint);
            trapezoidRange.add(nextTrapezoid);
            prevPoint = nextTrapezoid.vertices.get(3);
        }
        // Iterate over all pivot vertices in trapezoids between minIndex and maxIndex.
        TrapezoidIntersectionPair<T> bestTrapezoidIntersectionPair = null;
        for (int pivotTrapezoidIndex=minIndex; pivotTrapezoidIndex<=maxIndex; pivotTrapezoidIndex++) {
            if (pivotTrapezoidIndex % 200 == 0) {
                System.out.printf("Progress: considering vertices in trapezoid %d as pivots\n", pivotTrapezoidIndex);
            }
            boolean firstVertex = true;
            for (Point<T> pivotVertex: trapezoidRange.get(pivotTrapezoidIndex - minIndex).vertices) {
                if (firstVertex && pivotTrapezoidIndex != minIndex) {
                    firstVertex = false;
                    continue;
                }
                int pivotCurrentCollinear = 0;
                // Initialize a segment tree.
                // Each index of the segment tree corresponds to an interval of size maxIndexDiff. Incrementing
                // the count at an index corresponds to increasing the number of active trapezoids in a particular
                // interval of trapezoid indices.
                SegmentTreeNode activeTrapezoidsRoot = new SegmentTreeNode(minIndex, maxIndex + maxIndexDiff);

                Point<T> pivotPositiveDirectionPoint = new Point<>(pivotVertex.x.add(pivotVertex.x.one()), pivotVertex.y);
                ArrayList<EventPoint<T>> eventPoints = new ArrayList<>();
                PointComparator<T> positivePointComparator = new PointComparator<>(pivotVertex, true);
                PointComparator<T> negativePointComparator = new PointComparator<>(pivotVertex, false);
                EventPointComparator<T> eventPointComparator = new EventPointComparator<>(pivotVertex);
                // For each other trapezoid sort the 4 vertices relative to the pivot and identify enter and exit vertices.
                int currIndexMin = Math.max(pivotTrapezoidIndex - maxIndexDiff, minIndex);
                int currIndexMax = Math.min(pivotTrapezoidIndex + maxIndexDiff, maxIndex);
                for (int currTrapezoidIndex=currIndexMin; currTrapezoidIndex<=currIndexMax; currTrapezoidIndex++) {
                    Trapezoid<T> currentTrapezoid = trapezoidRange.get(currTrapezoidIndex - minIndex);
                    if (currentTrapezoid.contains(pivotVertex)) {
                        // Every line through the pivot intersects this trapezoid, so
                        // there are no enter and exit vertices.
                        activeTrapezoidsRoot.update(currTrapezoidIndex, currTrapezoidIndex + maxIndexDiff, 1);
                        pivotCurrentCollinear = Math.max(pivotCurrentCollinear, activeTrapezoidsRoot.max(0, maxIndex + maxIndexDiff));
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
                        pivotCurrentCollinear = Math.max(pivotCurrentCollinear, activeTrapezoidsRoot.max(0, maxIndex + maxIndexDiff));
                    }
                    Point<T> startPoint = trapPoints.get(0);
                    Point<T> endPoint = trapPoints.get(trapPoints.size() - 1);
                    eventPoints.add(new EventPoint<>(startPoint, currTrapezoidIndex, true));
                    eventPoints.add(new EventPoint<>(endPoint, currTrapezoidIndex, false));
                }
                // Sort all enter and exit vertices relative to the pivot.
                eventPoints.sort(eventPointComparator);
                TrapezoidIntersectionPair<T> pivotBestIntersectionPair = new TrapezoidIntersectionPair<>(
                        pivotCurrentCollinear,
                        pivotTrapezoidIndex,
                        eventPoints.get(0).trapezoidIndex,
                        pivotVertex,
                        eventPoints.get(0).point
                );
                // Iterate over enter and exit vertices and insert trapezoid index into the Segment tree
                for (EventPoint<T> eventPoint: eventPoints) {
                    if (eventPoint.isStart) {
                        // Insert the corresponding trapezoid into the Segment tree.
                        activeTrapezoidsRoot.update(eventPoint.trapezoidIndex, eventPoint.trapezoidIndex + maxIndexDiff, 1);
                        pivotCurrentCollinear = Math.max(pivotCurrentCollinear, activeTrapezoidsRoot.max(0, maxIndex + maxIndexDiff));
                        if (pivotCurrentCollinear > pivotBestIntersectionPair.numTrapezoidsIntersected) {
                            pivotBestIntersectionPair = new TrapezoidIntersectionPair<>(
                                    pivotCurrentCollinear,
                                    pivotTrapezoidIndex,
                                    eventPoint.trapezoidIndex,
                                    pivotVertex,
                                    eventPoint.point
                            );
                        }
                    } else {
                        // Remove the corresponding trapezoid from the Segment tree.
                        activeTrapezoidsRoot.update(eventPoint.trapezoidIndex, eventPoint.trapezoidIndex + maxIndexDiff, -1);
                    }
                }
                if (bestTrapezoidIntersectionPair == null || pivotBestIntersectionPair.numTrapezoidsIntersected > bestTrapezoidIntersectionPair.numTrapezoidsIntersected) {
                    bestTrapezoidIntersectionPair = pivotBestIntersectionPair;
                }
            }
        }
        return bestTrapezoidIntersectionPair;
    }

    /**
     * Get the smallest value for the square of a distance between trapezoids separated by
     * `gap` or `gap`+1 indices where the smaller index is in the interval [minIndex, maxIndex].
     *
     * O(maxIndex - minIndex)
     */
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

    /**
     * Get the largest value for the square of a distance between trapezoids separated by
     * `gap` or `gap`+1 indices, where the smaller index is in the interval [minIndex, maxIndex].
     *
     * O(maxIndex - minIndex)
     */
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

    /**
     * Assert that distance ratios between trapezoid pairs are below a given upper bound.
     *
     * @param gapMin: the minimum number of indices separating a pair of trapezoids.
     * @param gapMax: the maximum number of indices separating a pair of trapezoids.
     * @param startIndex: the first index to consider in the sequence of trapezoids.
     * @param endIndex: the last index to consider in the sequence of trapezoids.
     * @param baseUpperBound: the upper bound to compare against.
     * @return Considering pairs of trapezoids that are separated by at least gapMin indices and at most
     *  gapMax indices, return True iff the result of dividing the largest distance between points in a
     *  pair of trapezoids and the smallest distance between points in another pair of trapezoids is less
     *  than baseUpperBound.
     */
    public boolean assertBoundedRatio(int gapMin, int gapMax, int startIndex, int endIndex, T baseUpperBound) {
        if (trapezoids.size() <= endIndex + gapMax + 1) {
            symbolSequence.extendSequenceToLength(endIndex + gapMax + 2);
            buildTrapezoidTypeSequenceFromSymbolSequence();
            buildTrapezoidSequenceFromTrapezoidTypeSequence();
        }
        DistanceSqIndexRatio maxLoDistanceSqRatio = maxLoDistanceSqIndexRatio(gapMin, gapMax, startIndex, endIndex);
        DistanceSqIndexRatio maxHiDistanceSqRatio = maxHiDistanceSqIndexRatio(gapMin, gapMax, startIndex, endIndex);
        System.out.printf("maxLoDistanceRatio: %s\n", maxLoDistanceSqRatio);
        System.out.printf("maxHiDistanceRatio: %s\n", maxHiDistanceSqRatio);
        T lhs = maxHiDistanceSqRatio.distanceSq.divide(maxLoDistanceSqRatio.distanceSq);
        T rhsSqrt = baseUpperBound.multiply(baseUpperBound.whole(maxHiDistanceSqRatio.indexGap)).divide(
                baseUpperBound.whole(maxLoDistanceSqRatio.indexGap));
        T rhs = rhsSqrt.multiply(rhsSqrt);
        return lhs.compareTo(rhs) < 0;
    }

    /**
     * Assert that max distance divided by the index gap for all trapezoid pairs is below a given upper bound.
     *
     * @param gapMin: the minimum number of indices separating a pair of trapezoids.
     * @param gapMax: the maximum number of indices separating a pair of trapezoids.
     * @param startIndex: the first index to consider in the sequence of trapezoids.
     * @param endIndex: the last index to consider in the sequence of trapezoids.
     * @return Considering pairs of trapezoids that are separated by at least gapMin indices and at most
     *  gapMax indices, return the largest result of dividing the largest distance between points in a
     *  trapezoid by the index gap.
     */
    private DistanceSqIndexRatio maxHiDistanceSqIndexRatio(int gapMin, int gapMax, int startIndex, int endIndex) {
        if (trapezoids.size() <= endIndex + gapMax + 1) {
            symbolSequence.extendSequenceToLength(endIndex + gapMax + 2);
            buildTrapezoidTypeSequenceFromSymbolSequence();
            buildTrapezoidSequenceFromTrapezoidTypeSequence();
        }
        DistanceSqIndexRatio maxHiDistanceSqIndexRatio = null;
        for (int gap=gapMin; gap<=gapMax; gap++) {
            System.out.printf("Progress: considering gap %d in range [%d,%d]\n", gap, gapMin, gapMax);
            T hiDistanceSq = hiDistanceSq(startIndex, endIndex, gap);
            DistanceSqIndexRatio curr = new DistanceSqIndexRatio(hiDistanceSq, gap, false);
            if (maxHiDistanceSqIndexRatio == null || maxHiDistanceSqIndexRatio.compareTo(curr) < 0) {
                maxHiDistanceSqIndexRatio = curr;
            }
        }
        return maxHiDistanceSqIndexRatio;
    }

    /**
     * O((gapMax-gapMin) * (endIndex - startIndex))
     */
    private DistanceSqIndexRatio maxLoDistanceSqIndexRatio(int gapMin, int gapMax, int startIndex, int endIndex) {
        if (trapezoids.size() <= endIndex + gapMax + 1) {
            symbolSequence.extendSequenceToLength(endIndex + gapMax + 2);
            buildTrapezoidTypeSequenceFromSymbolSequence();
            buildTrapezoidSequenceFromTrapezoidTypeSequence();
        }
        DistanceSqIndexRatio maxLoDistanceSqIndexRatio = null;
        for (int gap=gapMin; gap<=gapMax; gap++) {
            System.out.printf("Progress: considering gap %d in range [%d,%d]\n", gap, gapMin, gapMax);
            T loDistanceSq = loDistanceSq(startIndex, endIndex, gap);
            DistanceSqIndexRatio curr = new DistanceSqIndexRatio(loDistanceSq, gap+1, true);
            if (maxLoDistanceSqIndexRatio == null || maxLoDistanceSqIndexRatio.compareTo(curr) < 0) {
                maxLoDistanceSqIndexRatio = curr;
            }
        }
        return maxLoDistanceSqIndexRatio;
    }

    /**
     * Assert that max distance divided by the index gap for all trapezoid pairs is below a given upper bound.
     *
     * @param gapMin: the minimum number of indices separating a pair of trapezoids.
     * @param gapMax: the maximum number of indices separating a pair of trapezoids.
     * @param startIndex: the first index to consider in the sequence of trapezoids.
     * @param endIndex: the last index to consider in the sequence of trapezoids.
     * @param baseUpperBound: the upper bound to compare against.
     * @return Considering pairs of trapezoids that are separated by at least gapMin indices and at most
     *  gapMax indices, return True iff the result of dividing the largest distance between points in a
     *  trapezoid by the index gap is at most baseUpperBound.
     */
    public boolean assertBoundedMaxDistance(int gapMin, int gapMax, int startIndex, int endIndex, T baseUpperBound) {
        if (trapezoids.size() <= endIndex + gapMax + 1) {
            symbolSequence.extendSequenceToLength(endIndex + gapMax + 2);
            buildTrapezoidTypeSequenceFromSymbolSequence();
            buildTrapezoidSequenceFromTrapezoidTypeSequence();
        }
        DistanceSqIndexRatio maxHiDistanceSqRatio = maxHiDistanceSqIndexRatio(gapMin, gapMax, startIndex, endIndex);
        System.out.printf("maxHiDistanceRatio: %s\n", maxHiDistanceSqRatio.toString());
        T rhsSqrt = baseUpperBound.multiply(baseUpperBound.whole(maxHiDistanceSqRatio.indexGap));
        T rhs = rhsSqrt.multiply(rhsSqrt);
        return maxHiDistanceSqRatio.distanceSq.compareTo(rhs) < 0;
    }

    /**
     * Assert that max distance divided by the index gap for all trapezoid pairs is below a given upper bound.
     *
     * O((gapMax-gapMin) * (endIndex - startIndex))
     *
     * @param gapMin: the minimum number of indices separating a pair of trapezoids.
     * @param gapMax: the maximum number of indices separating a pair of trapezoids.
     * @param startIndex: the first index to consider in the sequence of trapezoids.
     * @param endIndex: the last index to consider in the sequence of trapezoids.
     * @param baseUpperBound: the upper bound to compare against.
     * @return Considering pairs of trapezoids that are separated by at least gapMin indices and at most
     *  gapMax indices, return True iff the result of dividing the index gap plus 1 by the smallest
     *  distance between points in a pair of trapezoids is at most baseUpperBound.
     */
    public boolean assertBoundedMinDistance(int gapMin, int gapMax, int startIndex, int endIndex, T baseUpperBound) {
        if (trapezoids.size() <= endIndex + gapMax + 1) {
            symbolSequence.extendSequenceToLength(endIndex + gapMax + 2);
            buildTrapezoidTypeSequenceFromSymbolSequence();
            buildTrapezoidSequenceFromTrapezoidTypeSequence();
        }
        DistanceSqIndexRatio maxLoDistanceSqRatio = maxLoDistanceSqIndexRatio(gapMin, gapMax, startIndex, endIndex);
        System.out.printf("maxLoDistanceRatio: %s\n", maxLoDistanceSqRatio.toString());
        T lhsSqrt = baseUpperBound.whole(maxLoDistanceSqRatio.indexGap);
        T lhs = lhsSqrt.multiply(lhsSqrt);
        T rhs = maxLoDistanceSqRatio.distanceSq.multiply(baseUpperBound.multiply(baseUpperBound));
        return lhs.compareTo(rhs) < 0;
    }

    /**
     * Inner class for storing squared distances and index gaps.
     */
    class DistanceSqIndexRatio implements Comparable<DistanceSqIndexRatio> {
        T distanceSq;
        int indexGap;
        boolean inverse;
        public DistanceSqIndexRatio(T distanceSq, int indexGap, boolean inverse) {
            this.distanceSq = distanceSq;
            this.indexGap = indexGap;
            this.inverse = inverse;
        }
        public int compareTo(DistanceSqIndexRatio r2) {
            if (inverse ^ r2.inverse) {
                throw new RuntimeException("Should not compare an inverse ratio to a non-inverse ratio.");
            }
            if (inverse) {
                T lhs = r2.distanceSq.divide(distanceSq);
                T tIndexGap1 = distanceSq.whole(indexGap);
                T tIndexGap2 = r2.distanceSq.whole(r2.indexGap);
                T rhsSqrt = tIndexGap2.divide(tIndexGap1);
                T rhs = rhsSqrt.multiply(rhsSqrt);
                return lhs.compareTo(rhs);
            } else {
                T lhs = distanceSq.divide(r2.distanceSq);
                T tIndexGap1 = distanceSq.whole(indexGap);
                T tIndexGap2 = r2.distanceSq.whole(r2.indexGap);
                T rhsSqrt = tIndexGap1.divide(tIndexGap2);
                T rhs = rhsSqrt.multiply(rhsSqrt);
                return lhs.compareTo(rhs);
            }
        }

        public String toString() {
            if (inverse) {
                return String.format("%d / sqrt(%s)", indexGap, distanceSq.toString());
            } else {
                return String.format("sqrt(%s) / %d", distanceSq.toString(), indexGap);
            }
        }
    }
}
