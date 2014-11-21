package com.coillighting.udder.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class CollectionUtil {

    // TODO see if there are already Boon equivalents for these - there likely are.
    public static <T extends Comparable<? super T>> List<T> sorted(Collection<T> c) {
        ArrayList<T> list = new ArrayList<T>(c);
        Collections.sort(list);
        return list;
    }

}
