import javax.swing.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class Main {
    static PointFactory pf = new PointFactory();

    static Point<Fraction<WholeAndRt3>> wholeAndRt3StartPoint =
            pf.makeWholeAndRt3Point(0, 0, 0, 0);
    static Point<DoubleRep> doubleRepStartPoint = pf.makeDoublePoint(0, 0);

    static TrapezoidSequence<Fraction<WholeAndRt3>> wholeAndRt3TrapezoidSequence = null;
    static TrapezoidSequence<DoubleRep> doubleRepTrapezoidSequence = null;

    public static void main(String[] args) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

        String line = br.readLine();
        boolean done = false;
        while (line != null && !done) {
            String[] commandArgs = line.split(" ");
            if (commandArgs.length < 2) {
                System.out.println("Not enough arguments");
                line = br.readLine();
                continue;
            }
            String command = commandArgs[0];
            String numberSystemStr = commandArgs[1];
            switch (command) {
                case "draw":
                    if (commandArgs.length < 3) {
                        System.out.println("At least three arguments are needed for draw.");
                        break;
                    }
                    String outputPath = commandArgs[2];
                    switch (numberSystemStr) {
                        case "double":
                            if (doubleRepTrapezoidSequence == null) {
                                System.out.println("The sequence must be built before it can be drawn.");
                                break;
                            }
                            drawDoubleRepSequence(outputPath);
                            break;
                        case "wholeAndRt3":
                            if (wholeAndRt3TrapezoidSequence == null) {
                                System.out.println("The sequence must be built before it can be drawn.");
                                break;
                            }
                            drawWholeAndRt3Sequence(outputPath);
                            break;
                        default:
                            System.out.println("Unknown number system.");
                    }
                    break;
                case "build":
                    if (commandArgs.length < 3) {
                        System.out.println("At least two arguments are needed for build.");
                        break;
                    }
                    int nTrapezoids = 0;
                    try {
                        nTrapezoids = Integer.parseInt(commandArgs[2]);
                    } catch(NumberFormatException e) {
                        System.out.println("The second argument for 'build' must be an integer.");
                        break;
                    }
                    switch (numberSystemStr) {
                        case "double":
                            doubleRepTrapezoidSequence = new TrapezoidSequence<>(
                                    nTrapezoids, doubleRepStartPoint);
                            System.out.println("Built a trapezoid sequence of length "
                                    + wholeAndRt3TrapezoidSequence.trapezoids.size());
                            break;
                        case "wholeAndRt3":
                            wholeAndRt3TrapezoidSequence = new TrapezoidSequence<>(
                                    nTrapezoids, wholeAndRt3StartPoint);
                            System.out.println("Built a trapezoid sequence of length "
                                    + wholeAndRt3TrapezoidSequence.trapezoids.size());
                            break;
                        default:
                            System.out.println("Unknown number system.");
                    }
                    break;
                case "assertBoundedDistanceRatio":
                    if (commandArgs.length != 7 && commandArgs.length != 8) {
                        System.out.println("The assertBoundedDistanceRatio command takes only 6 or 7 arguments.");
                        break;
                    }
                    int gapMin;
                    int gapMax;
                    int startIndex;
                    int endIndex;
                    try {
                        gapMin = Integer.parseInt(commandArgs[2]);
                        gapMax = Integer.parseInt(commandArgs[3]);
                        startIndex = Integer.parseInt(commandArgs[4]);
                        endIndex = Integer.parseInt(commandArgs[5]);
                    } catch(NumberFormatException e) {
                        System.out.println("The first and second arguments to 'assertBoundedDistanceRatio' must be integers.");
                        break;
                    }
                    numberSystemStr = commandArgs[1];
                    switch (numberSystemStr) {
                        case "double":
                            DoubleRep doubleUpperBound = new DoubleRep(Double.parseDouble(commandArgs[6]));
                            boolean result = doubleRepTrapezoidSequence.assertBoundedRatio(gapMin, gapMax, startIndex, endIndex, doubleUpperBound);
                            if (result) {
                                System.out.println("SUCCESS! All distance ratios are within the bound!");
                            } else {
                                System.out.println("FAILURE! Some distance ratio exceeds the bound!");
                            }
                            break;
                        case "wholeAndRt3":
                            WholeAndRt3 wrt3UpperBound = new WholeAndRt3(Integer.parseInt(commandArgs[6]), Integer.parseInt(commandArgs[7]));
                            Fraction<WholeAndRt3> fracUpperBound = new Fraction<>(wrt3UpperBound, wrt3UpperBound.one());
                            boolean wrt3Result = wholeAndRt3TrapezoidSequence.assertBoundedRatio(gapMin, gapMax, startIndex, endIndex, fracUpperBound);
                            if (wrt3Result) {
                                System.out.println("SUCCESS! All distance ratios are within the bound!");
                            } else {
                                System.out.println("FAILURE! Some distance ratio exceeds the bound!");
                            }
                            break;
                        default:
                            System.out.println("Unknown number system");
                    }
                    break;
                case "quit":
                case "exit":
                    done = true;
                    break;
                default:
                    System.out.println("Unknown command.");
            }

            line = br.readLine();
        }
    }

    public static void drawWholeAndRt3Sequence(String path) {
        SwingUtilities.invokeLater(
                () -> new DrawTrapezoids<>(wholeAndRt3TrapezoidSequence, path));
    }
    public static void drawDoubleRepSequence(String path) {
        SwingUtilities.invokeLater(
                () -> new DrawTrapezoids<>(doubleRepTrapezoidSequence, path));
    }
}
