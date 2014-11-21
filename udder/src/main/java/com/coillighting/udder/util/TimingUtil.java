package com.coillighting.udder.util;

public class TimingUtil {

    private static volatile long waitSeed = System.nanoTime();

    /** Wait by forcing the CPU to spin for several cycles, correlated with
     *  the specified duration, evading optimizations that might rewrite this
     *  method into a no-op. An alternative to a quick thread sleep, which isn't
     *  very quick at these scales. EXPERIMENTAL.
     */
    public static void waitBusy(long duration) {
        // See research here:
        //    http://shipilev.net/blog/2014/nanotrusting-nanotime/
        // and benchmarking source here (formally GP2, but apparently derived
        // prior research into e.g. random-number generators etc.):
        //    (GPL2) http://hg.openjdk.java.net/code-tools/jmh/file/cde312963a3d/jmh-core/src/main/java/org/openjdk/jmh/logic/BlackHole.java#l400

        // Randomize, then reuse, the seed to avoid optimizations.
        long t = waitSeed;

        // See the article re: this backwards-counting trick.
        for (long i=duration; i>0; i--) {
        // 48 bit linear congruential generator with prime addend.
        t += (t * 0x5DEECE66DL + 0xBL + i) & (0xFFFFFFFFFFFFL);
        }

        // Memoization buster
        if (t == 42) {
            waitSeed += t;
        }
    }

}