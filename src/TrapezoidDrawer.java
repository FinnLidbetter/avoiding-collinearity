import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;

public class TrapezoidDrawer<T extends AbstractNumber<T>> extends JFrame {

    static final int imageWidth = 1400;
    static final int imageHeight = 600;
    static final int border = 50;
    TrapezoidSequence<T> trapezoidSequence;
    boolean recursive;

    public TrapezoidDrawer(TrapezoidSequence<T> trapezoidSequence, boolean recursive, String path) {
        super("Trapezoid sequence");
        this.trapezoidSequence = trapezoidSequence;
        this.recursive = recursive;

        setSize(imageWidth, imageHeight);
        setVisible(true);
        setBackground(Color.WHITE);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        BufferedImage bImg = new BufferedImage(
                getWidth(), getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D cg = bImg.createGraphics();
        paintAll(cg);
        try {
            if (ImageIO.write(bImg, "png", new File(path))) {
                System.out.println("-- saved");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void drawLines(Graphics g) {
        Graphics2D canvas = (Graphics2D) g;

        List<T> bounds = trapezoidSequence.getBounds();
        double xMin = bounds.get(0).toDouble();
        double yMin = bounds.get(1).toDouble();
        double xMax = bounds.get(2).toDouble();
        double yMax = bounds.get(3).toDouble();
        double xOffset = border;
        double yOffset = imageHeight - border;
        double xScalar = (imageWidth - 2 * border) / (xMax - xMin);
        double yScalar = (imageHeight - 2 * border) / (yMax - yMin);
        xScalar = Math.min(xScalar, yScalar);
        yScalar = Math.min(xScalar, yScalar);
        yScalar *= -1;
        double trapezoidOrderScalar = 1;
        int upperBound = trapezoidSequence.trapezoids.size();
        do {
            for (int i = 0; i < upperBound; i++) {
                Trapezoid<T> trap = trapezoidSequence.trapezoids.get(i);
                for (LineSegment<T> line : trap.sides) {
                    int x1 = (int) (line.p1.x.toDouble() * xScalar * trapezoidOrderScalar + xOffset);
                    int y1 = (int) (line.p1.y.toDouble() * yScalar * trapezoidOrderScalar + yOffset);
                    int x2 = (int) (line.p2.x.toDouble() * xScalar * trapezoidOrderScalar + xOffset);
                    int y2 = (int) (line.p2.y.toDouble() * yScalar * trapezoidOrderScalar + yOffset);
                    canvas.drawLine(x1, y1, x2, y2);
                }
            }
            upperBound /= 7;
            trapezoidOrderScalar *= 4.0;
        } while (upperBound>0 && this.recursive);
    }

    public void paint(Graphics g) {
        super.paint(g);
        drawLines(g);
    }
}
