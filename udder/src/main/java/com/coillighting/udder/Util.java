package com.coillighting.udder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

// TODO see if there are already Boon equivalents for these - there likely are.
public class Util {

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

    public static <T extends Comparable<? super T>> List<T> sorted(Collection<T> c) {
        ArrayList<T> list = new ArrayList<T>(c);
        Collections.sort(list);
        return list;
    }

}
