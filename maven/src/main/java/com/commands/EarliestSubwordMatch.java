package com.commands;

import com.SymbolSequence;

public class EarliestSubwordMatch {
    public static void main(String[] args) {

        if (isHelpArgument(args[0]) || args.length < 2) {
            printHelp();
            return;
        }
        try {
            if (args.length == 2) {
                int startIndex = Integer.parseInt(args[0]);
                int subwordLength = Integer.parseInt(args[1]);
                SymbolSequence symbolSeq = new SymbolSequence(startIndex + subwordLength + 1);
                int earliestSubwordMatch = symbolSeq.earliestSubwordMatch(startIndex, subwordLength);
                System.out.println(earliestSubwordMatch);
            } else if (args.length == 3) {
                int startIndex = Integer.parseInt(args[0]);
                int endIndex = Integer.parseInt(args[1]);
                int subwordLength = Integer.parseInt(args[2]);
                SymbolSequence symbolSeq = new SymbolSequence(endIndex + subwordLength + 1);
                int largestEarliestMatch = 0;
                for (int index=startIndex; index<=endIndex; index++) {
                    System.out.printf("Progress: index %d/%d\n", index, endIndex);
                    int earliestSubwordMatch = symbolSeq.earliestSubwordMatch(index, subwordLength);
                    if (earliestSubwordMatch == index) {
                        largestEarliestMatch = index;
                    }
                }
                System.out.println(largestEarliestMatch);

            }
        } catch (NumberFormatException e) {
            printHelp();
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
