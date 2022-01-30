import javax.swing.*;

public class DrawTrapezoids {

    public static TrapezoidSequence<DoubleRep> doubleRepTrapezoidSequence;
    public static TrapezoidSequence<WholeAndRt3> wholeAndRt3TrapezoidSequence;
    public static TrapezoidSequence<WholeNumber> wholeNumberTrapezoidSequence;

    public static void main(String[] args) {
        if (isHelpArgument(args[0])) {
            printHelp();
            System.exit(0);
        }
        if (args.length != 3 && args.length != 4) {
            printHelp();
            System.exit(1);
        }
        String numberSystem = args[0];
        String outputPath = args[2];
        boolean recursive = false;
        if (args.length == 4 && args[3].equals("--recursive")) {
            recursive = true;
        }
        try {
            int sequenceLength = Integer.parseInt(args[1]);
            switch (numberSystem) {
                case "wholeAndRt3" -> {
                    Point<WholeAndRt3> zeroPoint = new Point<>(WholeAndRt3.ZERO, WholeAndRt3.ZERO);
                    wholeAndRt3TrapezoidSequence = new TrapezoidSequence<>(sequenceLength, zeroPoint);
                    drawWholeAndRt3Sequence(outputPath, recursive);
                    break;
                }
                case "double" -> {
                    Point<DoubleRep> zeroPoint = new Point<>(new DoubleRep(0), new DoubleRep(0));
                    doubleRepTrapezoidSequence = new TrapezoidSequence<>(sequenceLength, zeroPoint);
                    drawDoubleRepSequence(outputPath, recursive);
                    break;
                }
                case "wholeNumber" -> {
                    Point<WholeNumber> zeroPoint = new Point<>(new WholeNumber(0), new WholeNumber(0));
                    wholeNumberTrapezoidSequence = new TrapezoidSequence<>(sequenceLength, zeroPoint);
                    drawWholeNumberSequence(outputPath, recursive);
                    break;
                }
                default -> {
                    printHelp();
                    System.exit(1);
                }
            }
        } catch (NumberFormatException e) {
            printHelp();
            System.exit(1);
        }
    }

    public static void drawWholeAndRt3Sequence(String path, boolean recursive) {
        SwingUtilities.invokeLater(
                () -> new TrapezoidDrawer<>(wholeAndRt3TrapezoidSequence, recursive, path));
    }

    public static void drawDoubleRepSequence(String path, boolean recursive) {
        SwingUtilities.invokeLater(
                () -> new TrapezoidDrawer<>(doubleRepTrapezoidSequence, recursive, path));
    }

    public static void drawWholeNumberSequence(String path, boolean recursive) {
        SwingUtilities.invokeLater(
                () -> new TrapezoidDrawer<>(wholeNumberTrapezoidSequence, recursive, path));
    }

    private static void printHelp() {
        System.out.println("Usage:");
        System.out.println("\tjava DrawTrapezoids \"wholeAndRt3\"|\"double\" num_trapezoids output_path [--recursive]");
        System.out.println("Examples:");
        System.out.println("java DrawTrapezoids wholeAndRt3 343 /Users/finn/trapezoids.png");
        System.out.println("java DrawTrapezoids double 343 /Users/finn/trapezoids.png --recursive");
        System.out.println("java DrawTrapezoids wholeNumber 78 /Users/finn/trapezoids.png");
    }

    private static boolean isHelpArgument(String str) {
        return str.contains("help") || str.equals("-h");
    }
}
