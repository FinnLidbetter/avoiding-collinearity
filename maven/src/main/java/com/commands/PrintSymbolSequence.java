package com.commands;

import com.SymbolSequence;

public class PrintSymbolSequence {
    public static void main(String[] args) {
        if (args.length < 1 || isHelpArgument(args[0])) {
            printHelp();
            return;
        }
        try {
            int sequenceLength = Integer.parseInt(args[0]);
            boolean alphabetic = false;
            boolean oneIndexed = false;
            for (int i=1; i<args.length; i++) {
                if (args[i].equals("--alphabetic")) {
                    alphabetic = true;
                }
                if (args[i].equals("--one-indexed")) {
                    oneIndexed = true;
                }
            }
            SymbolSequence symbolSeq = new SymbolSequence(sequenceLength);
            if (oneIndexed) {
                System.out.println("1-indexed Symbol Sequence");
            } else {
                System.out.println("0-indexed Symbol Sequence");
            }
            for (int i=0; i < sequenceLength; i++) {
                int indexToPrint = i;
                if (oneIndexed) {
                    indexToPrint = i + 1;
                }
                String symbol = symbolSeq.sequence.get(i).toString();
                if (alphabetic) {
                    symbol = "" + (char)('a'+symbolSeq.sequence.get(i));
                }
                System.out.printf("\t%d: %s\n", indexToPrint, symbol);
            }

        } catch (NumberFormatException e) {
            printHelp();
        }
    }
    private static void printHelp() {
        System.out.println("Usage:");
        System.out.println("\tjava com.commands.PrintSymbolSequence sequence_length [--alphabetic] [--one-indexed]");
        System.out.println("Examples:");
        System.out.println("java com.commands.PrintSymbolSequence 7");
        System.out.println("java com.commands.PrintSymbolSequence 215");
    }

    private static boolean isHelpArgument(String str) {
        return str.contains("help") || str.equals("-h");
    }

}
