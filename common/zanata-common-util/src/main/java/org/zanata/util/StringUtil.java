package org.zanata.util;

import com.google.common.base.Function;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

public class StringUtil {
    private StringUtil() {
    }

    public static <T> String concat(Iterable<T> objects, char delim,
            Function<T, String> toString) {
        List<String> strings = new ArrayList<String>();
        for (T obj : objects) {
            strings.add(toString.apply(obj));
        }

        return concat(strings, delim);
    }

    public static String concat(Iterable<String> strings, char delim) {
        StringBuilder sb = new StringBuilder();
        for (Iterator<String> it = strings.iterator(); it.hasNext();) {
            sb.append(it.next());
            if (it.hasNext())
                sb.append(delim);
        }
        return sb.toString();
    }

    public static List<String> split(String s, String delim) {
        return Arrays.asList(s.split(","));
    }

    /**
     * NB: Use allNonEmpty(Collection<String> strings, int nPlurals) if nPlurals
     * is known.
     *
     * @param strings
     * @return
     */
    public static boolean allNonEmpty(Collection<String> strings) {
        return allNonEmpty(strings, strings.size());
    }

    public static boolean allNonEmpty(Collection<String> strings, int nPlurals) {
        if (strings.size() < nPlurals) {
            return false;
        }
        for (String s : strings) {
            if (s == null || s.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    public static boolean allEmpty(Collection<String> strings) {
        for (String s : strings) {
            if (s != null && !s.isEmpty()) {
                return false;
            }
        }
        return true;
    }

}
