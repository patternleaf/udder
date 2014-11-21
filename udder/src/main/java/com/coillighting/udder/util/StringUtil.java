package com.coillighting.udder.util;

import java.util.List;

public class StringUtil {

    // TODO see if there are already Boon equivalents for these - there likely are.
    /** String.join is available only with Java 1.8. We support 1.7.
     *
     *  Format a list of items as strings, separated by the given conjunction.
     */
    static public String join(List<? extends Object> items, String conjunction) {
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (Object x : items) {
            if (first) {
                first = false;
            } else {
                sb.append(conjunction);
            }
            if(x == null) {
                sb.append("<null>");
            } else {
                sb.append(x.toString());
            }
        }
        return sb.toString();
    }

    /** Draw an ASCII slider representing the value of x in range [0..1.0]. */
    public static final String plot1D(double x) {
        if(x < 0.0) {
            return "[< MIN ERR ----------------------------------------] " + x;
        } else if(x > 1.0) {
            return "[---------------------------------------- ERR MAX >] " + x;
        } else {
            if(x <= 0.01) {
                x = 0.0;
            } else if(x >= 0.99) {
                x = 1.0;
            }
            int xi = (int)(x * 50.999999999);
            StringBuffer sb = new StringBuffer(80);
            sb.append('[');
            for(int i=0; i<xi; i++) {
                sb.append('-');
            }
            sb.append('|');
            for(int i=xi+1; i<=50; i++) {
                sb.append('-');
            }
            sb.append(']').append(' ').append(x);
            return sb.toString();
        }
    }

}
