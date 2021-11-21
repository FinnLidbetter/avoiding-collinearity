import org.junit.Assert;
import org.junit.Test;

public class FenwickTreeTests {

    @Test
    public void testFenwickTree() {
        int len = 1000;
        int numInserts = 10000;
        int deltaMax = 1000;
        int deltaMin = -1000;
        FenwickTree ft = new FenwickTree(len);
        Assert.assertEquals(len, ft.size());
        int[] simpleArr = new int[len];
        for (int i=0; i<numInserts; i++) {
            int index = (int)(len * Math.random());
            int delta = deltaMin + (int)((deltaMax - deltaMin + 1) * Math.random());
            ft.add(index + 1, delta);
            simpleArr[index] += delta;
        }
        for (int lo=0; lo<len; lo++) {
            for (int hi=lo; hi<len; hi++) {
                long ftSum = ft.sum(lo + 1, hi + 1);
                long simpleSum = 0;
                for (int j = lo; j <= hi; j++) {
                    simpleSum += simpleArr[j];
                }
                Assert.assertEquals(simpleSum, ftSum);
            }
        }
    }
}
