package com.commands;

import com.SymbolSequence;

public class EarliestSubwordMatch {
    public static void main(String[] args) {

        if (isHelpArgument(args[0]) || args.length != 2) {
            printHelp();
            System.exit(0);
        }
        try {
            int startIndex = Integer.parseInt(args[0]);
            int subwordLength = Integer.parseInt(args[1]);
            SymbolSequence symbolSeq = new SymbolSequence(startIndex + subwordLength + 1);
            int earliestSubwordMatch = symbolSeq.earliestSubwordMatch(startIndex, subwordLength);
            System.out.println(earliestSubwordMatch);
        } catch (NumberFormatException e) {
            printHelp();
            System.exit(1);
        }
    }

    private static void printHelp() {
        System.out.println("Usage:");
        System.out.println("\tjava com.commands.EarliestSubwordMatch start_index subword_length");
        System.out.println("Examples:");
        System.out.println("java com.commands.EarliestSubwordMatch 2 3");
        System.out.println("java com.commands.EarliestSubwordMatch 8 1");
    }

    private static boolean isHelpArgument(String str) {
        return str.contains("help") || str.equals("-h");
    }
}
