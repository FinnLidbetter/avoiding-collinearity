import java.util.ArrayList;
import java.util.List;

public class TrapezoidSequence {
    private static final TrapezoidFactory tf = new TrapezoidFactory();
    private static final WholeAndRt3 zero = new WholeAndRt3(0, 0);
    private static final WholeAndRt3 one = new WholeAndRt3(1, 0);
    private static final Fraction<WholeAndRt3> fracZero = new Fraction<>(zero, one);

    private static final int[][] morphism = {
        { 0, 4, 9, 0, 8, 9, 0},
        { 1, 5,10, 1, 6,10, 1},
        { 2, 3,11, 2, 7,11, 2},
        { 3, 2, 6, 3,10, 6, 3},
        { 4, 0, 7, 4,11, 7, 4},
        { 5, 1, 8, 5, 9, 8, 5},
        { 6, 3, 2, 6, 3,10, 6},
        { 7, 4, 0, 7, 4,11, 7},
        { 8, 5, 1, 8, 5, 9, 8},
        { 9, 0, 4, 9, 0, 8, 9},
        {10, 1, 5,10, 1, 6,10},
        {11, 2, 3,11, 2, 7,11},
    };
    private static final int[] trapezoidTypeMap = new int[]{0,3,5,1,2,4,1,2,4,0,3,5};


    List<Trapezoid<Fraction<WholeAndRt3>>> trapezoids;
    public TrapezoidSequence(int nTrapezoids) {
        trapezoids = new ArrayList<Trapezoid<T>>(nTrapezoids);
    }

    private void buildSequence(int nTrapezoids) {
        ArrayList<Integer> symbolSequence = new ArrayList<>(nTrapezoids);
        symbolSequence.add(0);
        int index = 0;
        while (symbolSequence.size() < nTrapezoids) {
            int currMorphismRule = symbolSequence.get(index);
            int ruleIndex = 0;
            while (symbolSequence.size() < nTrapezoids
                    && ruleIndex < morphism[currMorphismRule].length) {
                symbolSequence.add(morphism[currMorphismRule][ruleIndex]);
            }
            index++;
        }
        Point<Fraction<WholeAndRt3>> startPoint = new Point<>(fracZero, fracZero);
        for (int symbol: symbolSequence) {
            int trapType = trapezoidTypeMap[symbol];
            trapezoids.add(tf.makeSequenceTrapezoid(trapType, startPoint));
            startPoint = trapezoids.get(trapezoids.size() - 1).vertices.get(3);
        }
    }


}
