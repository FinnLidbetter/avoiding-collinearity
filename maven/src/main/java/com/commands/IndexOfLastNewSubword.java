package com.commands;

import com.SymbolSequence;

public class IndexOfLastNewSubword {
    public static void main(String[] args) {
        if (isHelpArgument(args[0]) || (args.length != 1 && args.length != 2)) {
            printHelp();
            return;
        }
        try {
            int subwordLength = Integer.parseInt(args[0]);
            SymbolSequence symbolSeq = new SymbolSequence(560);
            if (args.length==2 && args[1].equals("--vector-sequence")) {
                int index = symbolSeq.indexOfLastNewVectorSequence(subwordLength);
                StringBuilder vectorSequence = new StringBuilder();
                for (int i=index; i < index + subwordLength; i++) {
                   vectorSequence.append(SymbolSequence.vectorMap[symbolSeq.sequence.get(i)]);
                }
                System.out.printf("The (0-based) index of the last new vector sequence of length %d is %d.\n", subwordLength, index);
                System.out.printf("The vector sequence starting at this index is %s.\n", vectorSequence);
            } else {
                int index = symbolSeq.indexOfLastNewSubword(subwordLength);
                StringBuilder subword = new StringBuilder();
                for (int i = index; i < index + subwordLength; i++) {
                    subword.append((char) ('a' + symbolSeq.sequence.get(i)));
                }
                System.out.printf("The (0-based) index of the last new subword of length %d is %d.\n", subwordLength, index);
                System.out.printf("The subword at this index is %s.\n", subword);
            }
        } catch (NumberFormatException e) {
            printHelp();
        }
    }
    private static void printHelp() {
        System.out.println("Usage:");
        System.out.println("\tjava com.commands.IndexOfLastNewSubword subword_length [--vector-sequence]");
        System.out.println("Examples:");
        System.out.println("java com.commands.IndexOfLastNewSubword 2");
        System.out.println("java com.commands.IndexOfLastNewSubword 8");
        System.out.println("java com.commands.IndexOfLastNewSubword 8 --vector-sequence");
    }

    private static boolean isHelpArgument(String str) {
        return str.contains("help") || str.equals("-h");
    }
}
