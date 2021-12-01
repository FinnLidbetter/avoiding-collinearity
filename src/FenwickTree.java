import java.util.Arrays;

public class FenwickTree {

    private int[] arr;

    public FenwickTree(int n) {
        arr = new int[n + 1];
    }

    public int size() {
        return arr.length - 1;
    }

    public int sum(int i, int j) { // Get the sum of [i,j]
        return sum(j) - sum(i - 1);
    }

    private int sum(int i) { // Get the sum of [1,i]
        int sum = 0;
        while (i > 0) {
            sum += arr[i];
            i -= i & -i;
        }
        return sum;
    }

    public void add(int i, int delta) { // Add delta to value at i
        if (i <= 0) return;
        while (i < arr.length) {
            arr[i] += delta;
            i += i & -i;
        }
    }

    public String toString() {
        long[] vals = new long[arr.length];
        for (int i=1; i<arr.length; i++) {
            vals[i] = sum(i, i);
        }
        return Arrays.toString(vals);
    }
}
