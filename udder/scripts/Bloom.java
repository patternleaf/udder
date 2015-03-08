/** Exploratory prototype for Blooming Series|Leaf|Cube|Tesseract effect.
 *  Run tests with a routine command-line invocation.
 */
public class Bloom extends Object {

    /** An implementation of triangular_number(float) that accepts ints. */
    public static final double triangularNumber(int n) throws ArithmeticException {
        return Bloom.triangularNumber((double)n);
    }

    /** An implementation of triangular_number(float) that accepts longs. */
    public static final double triangularNumber(long n) throws ArithmeticException {
        return Bloom.triangularNumber((double)n);
    }

    /** Return the nth triangular number.
     *  Definition: http://en.wikipedia.org/wiki/Triangular_number
     */
    public static final double triangularNumber(double n) throws ArithmeticException {
        if(n < 0.0) {
            throw new ArithmeticException("Cannot return the nth triangular number for negative n.");
        } else {
            double a = 2.0*n + 1.0;
            return (a*a - 1.0) / 8.0;
        }
    }

    /** Inverse of triangular_number(n). Given a triangular number x,
     *  return n, such that the nth triangular number is x. Since values of
     * x that are not perfectly triangular will not have an integer n
     * value, but will fall between two integer n-offsets, we return a
     * double. The fractional portion indicates that x falls between two
     * triangular numbers.
     *
     * Beware: float point imprecision applies. This implementation is just
     * good enough for oscillate_triangular_root_color.
     */
    public static final double triangularRoot(double x) throws ArithmeticException {
        if(x < 0.0) {
            throw new ArithmeticException("Cannot take the triangular root of a negative number.");
        } else {
            return (Math.sqrt(8.0*x + 1.0) - 1.0) / 2.0;
        }
    }

    /** Map the pixel at offset to a thread colors (where color cycle frequency
     *  = len(palette)), given some scale multiplier (in space per thread).
     *
     *  offset: In pixel space. This is the spatial distance of the pixel you
     *      want to color. Must be positive. TODO: support negative offsets?
     *
     *  scale: Distince in pixels allocated to the spatial range
     *      corresponding to x in [1, frequency). Must be positive.
     *
     *  palette: Integer keys to some external collection of color elements.
     *      A palette of length 1 will yield monotone results. Length 2 creates
     *      a binary blinker. 3 is a three-way blinker, and so on.
     *
     *  Impl schematic:
     *       | dxnorm ||||||||||||||||||||||
     *      x0------->x1
     *      x0----------------------------->x2
     *      n0                              n1
     */
    public static final int oscillatingTriangularRootColor(double offset,
        double scale, int[] palette) throws ArithmeticException
    {
        double x1  = offset / scale;
        double n0 = (double)(int)Bloom.triangularRoot(x1);
        double x0 = Bloom.triangularNumber(n0);
        double n1 = n0 + 1.0;
        double x2 = Bloom.triangularNumber(n1);
        double dxnorm = (x1 - x0) / (x2 - x0);

        double color_index = dxnorm * (double)palette.length;

        // Because 3.0 * 0.333333333 is rounding down to 0.0, not up to 1.0...
        // In case color_index_float % 1 is ~= 0.999999999998, harmless otherwise.
        // Bump it up to the next color if it's very close in order to make up for
        // floating point imprecision:
        color_index += 0.01;
        return palette[(int)color_index];
    }

    public static final int[] triangularSequence(int length, int[] palette)
        throws ArithmeticException
    {
        return Bloom.triangularSequence(length, (double)palette.length, palette);
    }

    public static final int[] triangularSequence(int length, double scale,
        int[] palette) throws ArithmeticException
    {
        int[] seq = new int[length];
        for(int i=0; i<length; i++) {
            seq[i] = Bloom.oscillatingTriangularRootColor((double)i, scale, palette);
        }
        return seq;
    }

    public static void testTriangularNumber() {
        for(int x0=0; x0<3000; x0++) {
            double n = Bloom.triangularRoot(x0);
            double x1 = Bloom.triangularNumber(n);
            A.assertApproxEquals(x0, x1, 0.000000001);
        }

        try {
            Bloom.triangularNumber(-0.1);
            throw new AssertionError("Negative triangular numbers should fail.");
        } catch(ArithmeticException e) {}

        try {
            Bloom.triangularNumber(-1.0);
            throw new AssertionError("Negative triangular numbers should fail.");
        } catch(ArithmeticException e) {}

        try {
            Bloom.triangularNumber(-2.0);
            throw new AssertionError("Negative triangular numbers should fail.");
        } catch(ArithmeticException e) {}

        try {
            Bloom.triangularNumber(-2);
            throw new AssertionError("Negative triangular numbers should fail.");
        } catch(ArithmeticException e) {}

        try {
            Bloom.triangularNumber(-4);
            throw new AssertionError("Negative triangular numbers should fail.");
        } catch(ArithmeticException e) {}
    }

    public static void testTriangularRoot() {
        double tolerance = 0.00000000001; // floating point slop

        try {
            Bloom.triangularRoot(-0.1);
            throw new AssertionError("Negative roots should fail.");
        } catch(ArithmeticException e) {}

        try {
            Bloom.triangularRoot(-1.0);
            throw new AssertionError("Negative roots should fail.");
        } catch(ArithmeticException e) {}

        try {
            Bloom.triangularRoot(-2.0);
            throw new AssertionError("Negative roots should fail.");
        } catch(ArithmeticException e) {}

        try {
            Bloom.triangularRoot(-2);
            throw new AssertionError("Negative roots should fail.");
        } catch(ArithmeticException e) {}

        try {
            Bloom.triangularRoot(-4);
            throw new AssertionError("Negative roots should fail.");
        } catch(ArithmeticException e) {}

        A.assertEquals(Bloom.triangularRoot(0.0), 0.0);
        A.assertEquals(Bloom.triangularRoot(0), 0.0);

        A.assertApproxEquals(Bloom.triangularRoot(0.5), 0.61803398875, tolerance);

        A.assertEquals(Bloom.triangularRoot(1.0), 1.0);
        A.assertEquals(Bloom.triangularRoot(1), 1.0);

        A.assertApproxEquals(Bloom.triangularRoot(1.5), 1.30277563773, tolerance);

        A.assertApproxEquals(Bloom.triangularRoot(2.0), 1.56155281281, tolerance);
        A.assertApproxEquals(Bloom.triangularRoot(2), 1.56155281281, tolerance);

        A.assertEquals(Bloom.triangularRoot(3.0), 2.0);
        A.assertEquals(Bloom.triangularRoot(3), 2.0);

        A.assertEquals(Bloom.triangularRoot(6.0), 3.0);
        A.assertEquals(Bloom.triangularRoot(6), 3.0);

        A.assertEquals(Bloom.triangularRoot(10.0), 4.0);
        A.assertEquals(Bloom.triangularRoot(10), 4.0);

        A.assertEquals(Bloom.triangularRoot(15.0), 5.0);
        A.assertEquals(Bloom.triangularRoot(15), 5.0);

        A.assertApproxEquals(Bloom.triangularRoot(18.0), 5.5207972894, tolerance);
        A.assertApproxEquals(Bloom.triangularRoot(18), 5.5207972894, tolerance);

        A.assertEquals(Bloom.triangularRoot(21.0), 6.0);
        A.assertEquals(Bloom.triangularRoot(21), 6.0);

        // verify inverse operations
        int[] xs = {
            0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17,
        };
        double[] ns = {
            0.0, 1.0, 3.0, 6.0, 10.0, 15.0, 21.0, 28.0, 36.0, 45.0,
            55.0, 66.0, 78.0, 91.0, 105.0, 120.0, 136.0, 153.0,
        };
        for(int i=0; i<xs.length; i++) {
            int x = xs[i];
            double n = Bloom.triangularNumber(x);
            A.assertApproxEquals(n, ns[i], tolerance);
            A.assertApproxEquals(Bloom.triangularRoot(n), (double)x, tolerance);
        }
    }

    public static void testOscillatingTriangularRootColor() {
        int[] seq = null;

        int[] palette1 = {0,};
        // print ''.join(oscillate_triangular_root_color(x, 1.0, palette1).ljust(6) for x in xs)
        seq = Bloom.triangularSequence(18, palette1);
        A.assertSeqEquivalent(seq, "aaaaaaaaaaaaaaaaaa");

        int[] palette2 = {0, 1,};
        seq = Bloom.triangularSequence(42, palette2);
        A.assertSeqEquivalent(seq, "abaabbaaabbbaaaabbbbaaaaabbbbbaaaaaabbbbbb");

        int[] palette3 = {0, 1, 2,};
        seq = Bloom.triangularSequence(63, palette3);
        A.assertSeqEquivalent(seq, "abcaabbccaaabbbcccaaaabbbbccccaaaaabbbbbcccccaaaaaabbbbbbcccccc");

        int[] palette4 = {0, 1, 2, 3,};
        seq = Bloom.triangularSequence(84, palette4);
        A.assertSeqEquivalent(seq, "abcdaabbccddaaabbbcccdddaaaabbbbccccddddaaaaabbbbbcccccdddddaaaaaabbbbbbccccccdddddd");

        // scale > len(palette) by factor of 2 - Verify every symbol appears 2x.
        seq = Bloom.triangularSequence(84*2, 8.0, palette4);
        A.assertSeqEquivalent(seq, "aabbccddaaaabbbbccccddddaaaaaabbbbbbccccccddddddaaaaaaaabbbbbbbbccccccccddddddddaaaaaaaaaabbbbbbbbbbccccccccccddddddddddaaaaaaaaaaaabbbbbbbbbbbbccccccccccccdddddddddddd");

        // scale < len(palette) by half - Verify every 2nd b and d get skipped.
        seq = Bloom.triangularSequence(84/2, 2.0, palette4);
        A.assertSeqEquivalent(seq, "acabcdaabccdaabbccddaaabbcccddaaabbbcccddd");

        // scale == len(palette) with a longer len - Check both variants.
        int[] palette5 = {0, 1, 2, 3, 4};
        seq = Bloom.triangularSequence(105, 5.0, palette5);
        String expected = "abcdeaabbccddeeaaabbbcccdddeeeaaaabbbbccccddddeeeeaaaaabbbbbcccccdddddeeeeeaaaaaabbbbbbccccccddddddeeeeee";
        A.assertSeqEquivalent(seq, expected);
        seq = Bloom.triangularSequence(105, palette5);
        A.assertSeqEquivalent(seq, expected);

        A.assertEquals(Bloom.oscillatingTriangularRootColor(21.0, 3.0, palette3), 0);
        A.assertEquals(Bloom.oscillatingTriangularRootColor(22.0, 3.0, palette3), 1);
        A.assertEquals(Bloom.oscillatingTriangularRootColor(23.0, 3.0, palette3), 1);
    }

    public static void testTest() {
        A.assertEquals(0.0, 0.0);
        A.assertEquals(-1.0, -1.0);
        A.assertEquals(1.5, 1.5);

        try {
            A.assertEquals(1.0, 2.0);
            throw new RuntimeException("Failed to raise assertion where 1.0 != 2.0.");
        } catch(AssertionError e) {}

        A.assertEquals("abc", "abc");
        A.assertEquals(null, null);
        A.assertEquals("", "");

        try {
            A.assertEquals("ab", "cde");
            throw new RuntimeException("Failed to raise assertion where ab != cde.");
        } catch(AssertionError e) {}

        A.assertApproxEquals(1.0, 1.0, 0.0);
        A.assertApproxEquals(-1.0, -2.0, 2.0);
        A.assertApproxEquals(1.000001, 1.000002, 0.0000011);

        try {
            A.assertApproxEquals(2.0, 2.5, 0.4999);
            throw new RuntimeException("Failed to raise assertion where 2.0 !~= 2.5.");
        } catch(AssertionError e) {}

        A.assertNaN(Double.NaN);
        A.assertNaN(Math.sqrt(-1.0));
        A.assertNaN(Math.sqrt(-2.3));
        A.assertEquals(123.0/0.0, Double.POSITIVE_INFINITY);
        A.assertEquals(-124.0/0.0, Double.NEGATIVE_INFINITY);

        A.assertSeqEquivalent(null, null);
        int[] empty = {};
        A.assertSeqEquivalent(empty, "");

        int[] single0 = {0,};
        A.assertSeqEquivalent(single0, "a");
        int[] single1 = {1,};
        A.assertSeqEquivalent(single1, "b");
        int[] single2 = {2,};
        A.assertSeqEquivalent(single2, "c");

        int[] double0 = {0,1};
        A.assertSeqEquivalent(double0, "ab");
        int[] double1 = {1,2};
        A.assertSeqEquivalent(double1, "bc");
        int[] double2 = {2,3};
        A.assertSeqEquivalent(double2, "cd");

        int[] mult6 = {0,1,2,3,4,5};
        A.assertSeqEquivalent(mult6, "abcdef");
        int[] mult7 = {2,2,2,2,0,0,0};
        A.assertSeqEquivalent(mult7, "ccccaaa");
        int[] mult8 = {1,2,2,3,1,4,5,0};
        A.assertSeqEquivalent(mult8, "bccdbefa");

        try {
            A.assertSeqEquivalent(single0, "b");
            throw new RuntimeException("Failed to raise assertion where 0 !=> b.");
        } catch(AssertionError e) {}

        try {
            A.assertSeqEquivalent(single1, "a");
            throw new RuntimeException("Failed to raise assertion where 1 !=> a.");
        } catch(AssertionError e) {}

        try {
            A.assertSeqEquivalent(double1, "aa");
            throw new RuntimeException("Failed to raise assertion where 01 !=> aa.");
        } catch(AssertionError e) {}

        try {
            A.assertSeqEquivalent(mult6, "abcde");
            throw new RuntimeException("Failed to raise assertion where 012345 !=> abcde.");
        } catch(AssertionError e) {}

        try {
            A.assertSeqEquivalent(mult6, "abcdefg");
            throw new RuntimeException("Failed to raise assertion where 012345 !=> abcdefg.");
        } catch(AssertionError e) {}

        try {
            A.assertSeqEquivalent(mult6, "");
            throw new RuntimeException("Failed to raise assertion where 012345 !=> ''.");
        } catch(AssertionError e) {}

        try {
            A.assertSeqEquivalent(mult8, "ccdbefab");
            throw new RuntimeException("Failed to raise assertion where 12231450 !=> ccdbefab.");
        } catch(AssertionError e) {}
    }

    public static void main(String[] args) {
        System.out.println("testTest");
        Bloom.testTest();
        System.out.println("testTriangularRoot");
        Bloom.testTriangularRoot();
        System.out.println("testTriangularNumber");
        Bloom.testTriangularNumber();
        System.out.println("testOscillatingTriangularRootColor");
        Bloom.testOscillatingTriangularRootColor();
    }
}

/** TEMP TODO: fix Maven config so we can integrate JUnit. */
class A {

    public static void assertEquals(double a, double b) {
        if(a != b) {
            throw new AssertionError("" + a + " != " + b);
        }
    }

    public static void assertEquals(String a, String b) {
        if(a == null && b== null) {
            return;
        } else if(a == null || !a.equals(b)) {
            throw new AssertionError("" + a + " != " + b);
        }
    }

    public static void assertApproxEquals(double a, double b, double tolerance) {
        if(Math.abs(a - b) > tolerance) {
            throw new AssertionError("" + a + " !~= " + b + " within tolerance "
                + tolerance);
        }
    }

    public static void assertNaN(double a) {
        if(!Double.isNaN(a)) {
            throw new AssertionError("" + a + " is a number, but we expected NaN.");
        }
    }

    /** Test expectations are easier to read and spell as strings, but in
     *  production we'll want to use ints beause we're indexing into a list of
     *  colors. This assertion allows us to compute on ints but expect strings.
     *  Currently supports palettes up to 10 elements in length.
     */
    public static void assertSeqEquivalent(int[] seq, String str) {
        final String[] trans = {"a", "b", "c", "d", "e", "f", "g", "h", "i", "j"};
        if(seq == null && str == null) {
            return;
        } else if(seq != null && str == null) {
            throw new AssertionError("" + seq + " != null String");
        } else {
            A.assertEquals(seq.length, str.length());
            for(int i=0; i<seq.length; i++) {
                A.assertEquals(trans[seq[i]], str.substring(i, i+1));
            }
        }
    }
}

