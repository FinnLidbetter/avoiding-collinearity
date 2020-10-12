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
                    String path = commandArgs[2];
                    switch (numberSystemStr) {
                        case "double":
                            if (doubleRepTrapezoidSequence == null) {
                                System.out.println("The sequence must be built before it can be drawn.");
                                break;
                            }
                            drawDoubleRepSequence(path);
                            break;
                        case "wholeAndRt3":
                            if (wholeAndRt3TrapezoidSequence == null) {
                                System.out.println("The sequence must be built before it can be drawn.");
                                break;
                            }
                            drawWholeAndRt3Sequence(path);
                            break;
                        default:
                            System.out.println("Unknown number system.");
                    }
                    break;
                case "build":
                    if (commandArgs.length < 3) {
                        System.out.println("At least three arguments are needed for build.");
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
