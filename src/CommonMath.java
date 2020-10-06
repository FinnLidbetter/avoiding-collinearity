public class CommonMath {
    static long gcd(long a, long b) {
        if (a < 0)
            a *= -1;
        if (b < 0)
            b *= -1;
        return (b == 0) ? a : gcd(b, a%b);
    }
}
