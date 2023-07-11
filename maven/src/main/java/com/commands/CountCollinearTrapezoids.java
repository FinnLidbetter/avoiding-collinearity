package com.commands;

import com.Interval;
import com.Point;
import com.TrapezoidIntersectionPair;
import com.TrapezoidSequence;
import com.numbers.WholeAndRt3;
import com.numbers.WholeNumber;

import java.util.Arrays;

public class CountCollinearTrapezoids {

    public static void main(String[] args) {
        if (isHelpArgument(args[0])) {
            printHelp();
            return;
        }
        if (args.length != 2) {
            printHelp();
            return;
        }
        try {
            int maxIndexGap = Integer.parseInt(args[0]);
            String numberSystem = args[1];
            if (numberSystem.equals("wholeAndRt3")) {
                Point<WholeAndRt3> zeroPoint = new Point<>(WholeAndRt3.ZERO, WholeAndRt3.ZERO);
                TrapezoidSequence<WholeAndRt3> trapSeq = new TrapezoidSequence<>(2, zeroPoint);
                Interval[] searchIntervals = trapSeq.getCollinearSearchIntervals(maxIndexGap);
                System.out.printf("Intervals to search: %s.\n", Arrays.toString(searchIntervals));
                TrapezoidIntersectionPair<WholeAndRt3> bestIntersectionPair = null;
                for (Interval searchInterval: searchIntervals) {
                    TrapezoidIntersectionPair<WholeAndRt3> intervalBestIntersectionPair = trapSeq.radialSweepCountCollinear(
                            searchInterval.getLo(), searchInterval.getHi(), maxIndexGap
                    );
                    if (bestIntersectionPair == null || intervalBestIntersectionPair.numTrapezoidsIntersected > bestIntersectionPair.numTrapezoidsIntersected) {
                        bestIntersectionPair = intervalBestIntersectionPair;
                    }
                }
                System.out.printf(
                        "The largest number of trapezoids separated by at most %d indices that\n" +
                        "are intersected by a single straight line is %d.\n" +
                        "The intersection is through trapezoids %d and %d (0-based indexing) at points\n" +
                        "%s and %s.\n",
                        maxIndexGap, bestIntersectionPair.numTrapezoidsIntersected,
                        bestIntersectionPair.trapezoidIndex1,
                        bestIntersectionPair.trapezoidIndex2,
                        bestIntersectionPair.p1,
                        bestIntersectionPair.p2
                );
            } else if (numberSystem.equals("wholeNumber")) {
                Point<WholeNumber> zeroPoint = new Point<>(WholeNumber.ZERO, WholeNumber.ZERO);
                TrapezoidSequence<WholeNumber> trapSeq = new TrapezoidSequence<>(2, zeroPoint);
                Interval[] searchIntervals = trapSeq.getCollinearSearchIntervals(maxIndexGap);
                System.out.printf("Intervals to search: %s.\n", Arrays.toString(searchIntervals));
                TrapezoidIntersectionPair<WholeNumber> bestIntersectionPair = null;
                for (Interval searchInterval: searchIntervals) {
                    TrapezoidIntersectionPair<WholeNumber> intervalBestIntersectionPair = trapSeq.radialSweepCountCollinear(
                            searchInterval.getLo(), searchInterval.getHi(), maxIndexGap
                    );
                    if (bestIntersectionPair == null || intervalBestIntersectionPair.numTrapezoidsIntersected > bestIntersectionPair.numTrapezoidsIntersected) {
                        bestIntersectionPair = intervalBestIntersectionPair;
                    }
                }
                System.out.printf(
                        "The largest number of trapezoids separated by at most %d indices that\n" +
                                "are intersected by a single straight line is %d.\n" +
                                "The intersection is through trapezoids %d and %d (0-based indexing) at points\n" +
                                "%s and %s.\n",
                        maxIndexGap, bestIntersectionPair.numTrapezoidsIntersected,
                        bestIntersectionPair.trapezoidIndex1,
                        bestIntersectionPair.trapezoidIndex2,
                        bestIntersectionPair.p1,
                        bestIntersectionPair.p2
                );
            } else {
                printHelp();
                return;
            }
        } catch (NumberFormatException e) {
            printHelp();
        }

    }
    private static void printHelp() {
        System.out.println("Usage:");
        System.out.println("\tjava com.commands.CountCollinearTrapezoids maxIndexGap \"wholeAndRt3\"|\"wholeNumber\"");
        System.out.println("Examples:");
        System.out.println("java com.commands.CountCollinearTrapezoids 7 wholeAndRt3");
        System.out.println("java com.commands.CountCollinearTrapezoids 7 wholeNumber");
    }

    private static boolean isHelpArgument(String str) {
        return str.contains("help") || str.equals("-h");
    }
}
