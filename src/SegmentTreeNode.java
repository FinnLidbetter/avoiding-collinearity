import java.util.Arrays;

public class SegmentTreeNode {
    static final int NEG_INF = Integer.MIN_VALUE;

    int indexMin;
    int indexMax;
    int nodeMax = 0;
    int nodeSum = 0;
    int lazy = 0;
    SegmentTreeNode leftChild, rightChild;

    public SegmentTreeNode(int lo, int hi) {
        indexMin = lo;
        indexMax = hi;
        if (lo == hi) {
            leftChild = null;
            rightChild = null;
        } else {
            int mid = lo + (hi - lo) / 2;
            leftChild = new SegmentTreeNode(lo, mid);
            rightChild = new SegmentTreeNode(mid + 1, hi);
        }
    }
    public void update(int lo, int hi, int delta) { // Adjust [lo, hi]
        propagate(); // Do lazy updates to children
        if (lo <= indexMin && indexMax <= hi) { // Node's range fits in query range
            nodeSum += delta * (indexMax - indexMin + 1);
            nodeMax += delta;
            // Lazily propagate update to children
            if (leftChild != null) {
                leftChild.lazy += delta;
            }
            if (rightChild != null) {
                rightChild.lazy += delta;
            }
        } else if (lo <= indexMax && hi>=indexMin) { // Partial overlap
            if (leftChild != null) {
                leftChild.update(lo, hi, delta);
            }
            if (rightChild != null) {
                rightChild.update(lo, hi, delta);
            }
            nodeSum = (leftChild == null ? 0 : leftChild.nodeSum) + (rightChild == null ? 0 : rightChild.nodeSum);
            nodeMax = Math.max(
                    (leftChild == null ? NEG_INF : leftChild.nodeMax),
                    (rightChild == null ? NEG_INF : rightChild.nodeMax));
        }
    }
    public int max(int lo, int hi) { // Get max value in interval [lo, hi]
        propagate(); // Do lazy updates to children
        if (lo <= indexMin && indexMax <= hi) {
            return nodeMax;
        } else if (hi < indexMin || lo > indexMax) {
            return NEG_INF;
        }
        return Math.max(
                (leftChild == null ? NEG_INF : leftChild.max(lo, hi)),
                (rightChild == null ? NEG_INF : rightChild.max(lo, hi))
        );
    }
    private void propagate() {
        if (lazy != 0) {
            nodeSum += lazy * (indexMax - indexMin);
            nodeMax += lazy;
            if (leftChild != null)  {
                leftChild.lazy += lazy;
            }
            if (rightChild != null) {
                rightChild.lazy += lazy;
            }
            lazy = 0;
        }
    }

    public String toString() {
        int[] values = new int[indexMax - indexMin + 1];
        for (int i=indexMin; i<=indexMax; i++) {
            values[i - indexMin] = max(i, i);
        }
        return Arrays.toString(values);
    }
}
