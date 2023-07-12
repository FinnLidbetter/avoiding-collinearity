package com.commands;

import java.util.Arrays;
import com.Interval;
import com.SymbolSequence;

public class DistinctSubwordIntervals {
    public static void main(String[] args) {

        if (isHelpArgument(args[0]) || args.length != 1) {
            printHelp();
            return;
        }
        try {
            int wordLength = Integer.parseInt(args[0]);
            SymbolSequence sequence = new SymbolSequence(2);
            Interval[] intervals = sequence.getCollienarSearchIntervals(wordLength);
            System.out.println(Arrays.toString(intervals));
        } catch (NumberFormatException e) {
            printHelp();
        }
    }

    private static void printHelp() {
        System.out.println("Usage:");
        System.out.println("\tjava com.commands.DistinctSubwordIntervals word_length");
        System.out.println("Examples:");
        System.out.println("java com.commands.DistinctSubwordIntervals 7");
        System.out.println("java com.commands.DistinctSubwordIntervals 10");
    }

    private static boolean isHelpArgument(String str) {
        return str.contains("help") || str.equals("-h");
    }
}
