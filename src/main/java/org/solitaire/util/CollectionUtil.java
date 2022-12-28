package org.solitaire.util;

import java.util.Collection;

public class CollectionUtil {

    public static boolean isNotEmpty(Collection<?> collection) {
        return collection.size() > 0;
    }

    public static <T, R extends Collection<T>> R add(R list, T item) {
        list.add(item);
        return list;
    }
}
