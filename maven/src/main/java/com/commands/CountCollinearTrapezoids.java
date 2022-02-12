package com.commands;

import com.Interval;
import com.Point;
import com.TrapezoidSequence;
import com.numbers.WholeAndRt3;
import com.numbers.WholeNumber;

import java.util.Arrays;

public class CountCollinearTrapezoids {

    public static void main(String[] args) {
        if (isHelpArgument(args[0])) {
            printHelp();
            System.exit(0);
        }
        if (args.length != 2) {
            printHelp();
            System.exit(1);
        }
        try {
            int maxIndexGap = Integer.parseInt(args[0]);
            String numberSystem = args[1];
            if (numberSystem.equals("wholeAndRt3")) {
                Point<WholeAndRt3> zeroPoint = new Point<>(WholeAndRt3.ZERO, WholeAndRt3.ZERO);
                TrapezoidSequence<WholeAndRt3> trapSeq = new TrapezoidSequence<>(2, zeroPoint);
                Interval[] searchIntervals = trapSeq.getCollinearSearchIntervals(maxIndexGap);
                System.out.printf("Intervals to search: %s.\n", Arrays.toString(searchIntervals));
                int maxCollinearCount = 0;
                for (Interval searchInterval: searchIntervals) {
                    int intervalMaxCollinearCount = trapSeq.radialSweepCountCollinear(
                            searchInterval.getLo(), searchInterval.getHi(), maxIndexGap
                    );
                    if (intervalMaxCollinearCount > maxCollinearCount) {
                        maxCollinearCount = intervalMaxCollinearCount;
                    }
                }
                System.out.printf(
                        "The largest number of trapezoids separated by at most %d indices that\n" +
                        "are intersected by a single straight line is %d.\n", maxIndexGap, maxCollinearCount
                );
            } else if (numberSystem.equals("wholeNumber")) {
                Point<WholeNumber> zeroPoint = new Point<>(WholeNumber.ZERO, WholeNumber.ZERO);
                TrapezoidSequence<WholeNumber> trapSeq = new TrapezoidSequence<>(2, zeroPoint);
                Interval[] searchIntervals = trapSeq.getCollinearSearchIntervals(maxIndexGap);
                System.out.printf("Intervals to search: %s.\n", Arrays.toString(searchIntervals));
                int maxCollinearCount = 0;
                for (Interval searchInterval: searchIntervals) {
                    int intervalMaxCollinearCount = trapSeq.radialSweepCountCollinear(
                            searchInterval.getLo(), searchInterval.getHi(), maxIndexGap
                    );
                    if (intervalMaxCollinearCount > maxCollinearCount) {
                        maxCollinearCount = intervalMaxCollinearCount;
                    }
                }
                System.out.printf(
                        "The largest number of trapezoids separated by at most %d indices that\n" +
                        "are intersected by a single straight line is %d.\n", maxIndexGap, maxCollinearCount
                );
            } else {
                printHelp();
                System.exit(1);
            }
        } catch (NumberFormatException e) {
            printHelp();
            System.exit(1);
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
