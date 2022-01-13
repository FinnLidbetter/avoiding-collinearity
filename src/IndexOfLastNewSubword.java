public class IndexOfLastNewSubword {
    public static void main(String[] args) {
        if (isHelpArgument(args[0]) || (args.length != 1 && args.length != 2)) {
            printHelp();
            System.exit(0);
        }
        try {
            int subwordLength = Integer.parseInt(args[0]);
            TrapezoidSequence<WholeNumber> trapSeq = new TrapezoidSequence<>(
                    560, new Point<>(WholeNumber.ZERO, WholeNumber.ZERO));
            if (args.length==2 && args[1].equals("--vector-sequence")) {
                int index = trapSeq.indexOfLastNewVectorSequence(subwordLength);
                StringBuilder vectorSequence = new StringBuilder();
                for (int i=index; i < index + subwordLength; i++) {
                   vectorSequence.append(trapSeq.vectorMap[trapSeq.symbolSequence.get(i)]);
                }
                System.out.printf("The (0-based) index of the last new vector sequence of length %d is %d.\n", subwordLength, index);
                System.out.printf("The vector sequence starting at this index is %s.\n", vectorSequence);
            } else {
                int index = trapSeq.indexOfLastNewSubword(subwordLength);
                StringBuilder subword = new StringBuilder();
                for (int i = index; i < index + subwordLength; i++) {
                    subword.append((char) ('a' + trapSeq.symbolSequence.get(i)));
                }
                System.out.printf("The (0-based) index of the last new subword of length %d is %d.\n", subwordLength, index);
                System.out.printf("The subword at this index is %s.\n", subword);
            }
        } catch (NumberFormatException e) {
            printHelp();
            System.exit(1);
        }
    }
    private static void printHelp() {
        System.out.println("Usage:");
        System.out.println("\tjava IndexOfLastNewSubword subword_length [--vector-sequence]");
        System.out.println("Examples:");
        System.out.println("java IndexOfLastNewSubword 2");
        System.out.println("java IndexOfLastNewSubword 8");
        System.out.println("java IndexOfLastNewSubword 8 --vector-sequence");
    }

    private static boolean isHelpArgument(String str) {
        return str.contains("help") || str.equals("-h");
    }
}
