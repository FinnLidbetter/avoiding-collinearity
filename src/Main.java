import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

public class Main {
    static PointFactory pf = new PointFactory();

    static Point<Fraction<WholeAndRt3>> wholeAndRt3StartPoint =
            pf.makeWholeAndRt3Point(0, 0, 0, 0);
    static Point<DoubleRep> doubleRepStartPoint = pf.makeDoublePoint(0, 0);

    static TrapezoidSequence<Fraction<WholeAndRt3>> wholeAndRt3TrapezoidSequence = null;
    static TrapezoidSequence<DoubleRep> doubleRepTrapezoidSequence = null;

    static final int imageWidth = 2000;
    static final int imageHeight = 1400;
    static final int border = 50;

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
                            System.out.println("Built a trapezoid sequence of length " + wholeAndRt3TrapezoidSequence.trapezoids.size());
                            break;
                        case "wholeAndRt3":
                            wholeAndRt3TrapezoidSequence = new TrapezoidSequence<>(
                                    nTrapezoids, wholeAndRt3StartPoint);
                            System.out.println("Built a trapezoid sequence of length " + wholeAndRt3TrapezoidSequence.trapezoids.size());
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
        List<Fraction<WholeAndRt3>> bounds =
                wholeAndRt3TrapezoidSequence.getBounds();
        BufferedImage bi = new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D canvas = bi.createGraphics();
        canvas.setPaint(Color.black);
        canvas.setStroke(new BasicStroke(2f));
        double xMin = bounds.get(0).toDouble();
        double yMin = bounds.get(1).toDouble();
        double xMax = bounds.get(2).toDouble();
        double yMax = bounds.get(3).toDouble();
        double xOffset = border, yOffset = border;
        double xScalar = (xMax - xMin) / (imageWidth - 2 * border);
        double yScalar = (yMax - yMin) / (imageHeight - 2 * border);
        xScalar = Math.min(xScalar, yScalar);
        yScalar = Math.min(xScalar, yScalar);
        for (Trapezoid<Fraction<WholeAndRt3>> trap: wholeAndRt3TrapezoidSequence.trapezoids) {
            for (LineSegment<Fraction<WholeAndRt3>> line: trap.sides) {
                canvas.drawLine(
                        (int)(line.p1.x.toDouble() * xScalar + xOffset),
                        (int)(line.p1.y.toDouble() * yScalar + yOffset),
                        (int)(line.p2.x.toDouble() * xScalar + xOffset),
                        (int)(line.p2.y.toDouble() * yScalar + yOffset));
            }
        }
        try {
            ImageIO.write(bi, "png", new File(path));
        } catch(IOException ie) {
            ie.printStackTrace();
        }
    }
    public static void drawDoubleRepSequence(String path) {
        System.out.println("Not implemented");
    }
}
