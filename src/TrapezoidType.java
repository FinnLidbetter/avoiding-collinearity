/**
 * The trapezoid types form a group isomorphic to the dihedral group of order 6.
 */
public enum TrapezoidType {
    ZERO(0),
    ONE(1),
    TWO(2),
    THREE(3),
    FOUR(4),
    FIVE(5);

    private final int typeNumber;

    // Define the Cayley table with the group operations.
    private final static TrapezoidType[][] cayleyTable = {
            {TrapezoidType.ZERO, TrapezoidType.ONE, TrapezoidType.TWO, TrapezoidType.THREE, TrapezoidType.FOUR, TrapezoidType.FIVE},
            {TrapezoidType.ONE, TrapezoidType.ZERO, TrapezoidType.THREE, TrapezoidType.TWO, TrapezoidType.FIVE, TrapezoidType.FOUR},
            {TrapezoidType.TWO, TrapezoidType.FOUR, TrapezoidType.ZERO, TrapezoidType.FOUR, TrapezoidType.THREE, TrapezoidType.ONE},
            {TrapezoidType.THREE, TrapezoidType.FOUR, TrapezoidType.ONE, TrapezoidType.FIVE, TrapezoidType.TWO, TrapezoidType.ZERO},
            {TrapezoidType.FOUR, TrapezoidType.THREE, TrapezoidType.FIVE, TrapezoidType.ONE, TrapezoidType.ZERO, TrapezoidType.TWO},
            {TrapezoidType.FIVE, TrapezoidType.TWO, TrapezoidType.FOUR, TrapezoidType.ZERO, TrapezoidType.ONE, TrapezoidType.THREE},
    };

    TrapezoidType(int typeNumber) {
        this.typeNumber = typeNumber;
    }

    public TrapezoidType multiply(TrapezoidType t2) {
        return cayleyTable[this.index()][t2.index()];
    }

    public TrapezoidType inverse() {
        return switch (this) {
            case ZERO -> TrapezoidType.ZERO;
            case ONE -> TrapezoidType.ONE;
            case TWO -> TrapezoidType.TWO;
            case THREE -> TrapezoidType.FIVE;
            case FOUR -> TrapezoidType.FOUR;
            case FIVE -> TrapezoidType.THREE;
        };
    }

    /**
     * Get the index of the element for use in the 2D Cayley table array.
     *
     * @return the index of the group element in the Cayley table.
     */
    public int index() {
        return typeNumber;
    }

    public String toString() {
        return "" + typeNumber;
    }
}
