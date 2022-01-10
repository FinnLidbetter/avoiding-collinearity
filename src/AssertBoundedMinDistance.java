public class AssertBoundedMinDistance {

    public static void main(String[] args) {
        if (isHelpArgument(args[0])) {
            printHelp();
            System.exit(0);
        }
        if (args.length < 4) {
            printHelp();
            System.exit(1);
        }
        try {
            int gapMin = Integer.parseInt(args[0]);
            int gapMax = Integer.parseInt(args[1]);
            String numberSystem = args[2];
            if (!numberSystem.equals("wholeAndRt3") && !numberSystem.equals("double")) {
                printHelp();
                System.exit(1);
            }
            if (numberSystem.equals("wholeAndRt3") && args.length != 5) {
                printHelp();
                System.exit(1);
            }
            if (numberSystem.equals("double") && args.length != 4) {
                printHelp();
                System.exit(1);
            }
            if (numberSystem.equals("wholeAndRt3")) {
                Fraction<WholeAndRt3> upperBound = new Fraction<>(
                        new WholeAndRt3(Long.parseLong(args[3]), Long.parseLong(args[4])),
                        new WholeAndRt3(1, 0)
                );
                Point<Fraction<WholeAndRt3>> zeroPoint = new Point<>(
                        new Fraction<>(WholeAndRt3.ZERO, WholeAndRt3.ONE),
                        new Fraction<>(WholeAndRt3.ZERO, WholeAndRt3.ONE)
                );
                TrapezoidSequence<Fraction<WholeAndRt3>> trapSeq = new TrapezoidSequence<>(1, zeroPoint);
                int lastNewRelativePositioningIndex = trapSeq.indexOfLastNewRelativePositioning(gapMax + 1);
                boolean belowBound = trapSeq.assertBoundedMinDistance(
                        gapMin, gapMax, 0, lastNewRelativePositioningIndex, upperBound
                );
                if (belowBound) {
                    System.out.println("SUCCESS");
                    System.out.printf(
                            "The ratio of the index gap plus 1 to the smallest between \n" +
                            "trapezoids separated by at least %d indices and at most %d indices\n" +
                            "is less than %s\n", gapMin, gapMax, upperBound.num
                    );
                } else {
                    System.out.println("FAILURE");
                    System.out.printf(
                            "The ratio of the index gap plus 1 to the smallest distance between \n" +
                            "trapezoids separated by at least %d indices and at most %d indices\n" +
                            "is not less than %s\n", gapMin, gapMax, upperBound.num
                    );
                }
            } else {
                DoubleRep upperBound = new DoubleRep(Double.parseDouble(args[3]));
                Point<DoubleRep> zeroPoint = new Point<>(new DoubleRep(0), new DoubleRep(0));
                TrapezoidSequence<DoubleRep> trapSeq = new TrapezoidSequence<>(1, zeroPoint);
                int lastNewRelativePositioningIndex = trapSeq.indexOfLastNewRelativePositioning(gapMax+1);
                boolean belowBound = trapSeq.assertBoundedMinDistance(gapMin, gapMax, 0, lastNewRelativePositioningIndex, upperBound);
                if (belowBound) {
                    System.out.println("SUCCESS");
                    System.out.printf(
                            "The ratio of the index gap plus 1 to the smallest distance between \n" +
                            "trapezoids separated by at least %d indices and at most %d indices\n" +
                            "is less than %s\n", gapMin, gapMax, upperBound
                    );
                } else {
                    System.out.println("FAILURE");
                    System.out.printf(
                            "The ratio of the index gap plus 1 to the smallest distance between \n" +
                            "trapezoids separated by at least %d indices and at most %d indices\n" +
                            "is not less than %s\n", gapMin, gapMax, upperBound
                    );
                }
            }
        } catch (NumberFormatException e) {
            printHelp();
            System.exit(1);
        }
    }
    private static void printHelp() {
        System.out.println("Usage:");
        System.out.println("\tjava AssertBoundedMinDistance min_gap max_gap (\"wholeAndRt3\" ones rt3)|(\"double\" value)");
        System.out.println("Examples:");
        System.out.println("java AssertBoundedMinDistance 7 48 wholeAndRt3 9 0");
        System.out.println("java AssertBoundedMinDistance 7 48 double 4.44");
    }

    private static boolean isHelpArgument(String str) {
        return str.contains("help") || str.equals("-h");
    }


}
