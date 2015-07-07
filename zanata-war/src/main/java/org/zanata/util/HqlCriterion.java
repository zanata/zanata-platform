package org.zanata.util;

import org.hibernate.criterion.MatchMode;

/**
 * To make writing HQL easier.
 * @author Patrick Huang
 *         <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
public class HqlCriterion {

    public static String eq(String property, String namedParam) {
        return property + "=" + namedParam;
    }

    public static String ne(String property, String namedParam) {
        return property + "<>" + namedParam;
    }

    public static String isNull(String property) {
        return property + " is null";
    }

    public static String ilike(String property, String namedParam) {
        return "lower(" + property + ") like " + namedParam +"";
    }

    public static String like(String property, String namedParam) {
        return property + " like " + namedParam;
    }

    public static String like(String property, boolean caseSensitive, String namedParam) {
        if (caseSensitive) {
            return like(property, namedParam);
        } else {
            return ilike(property, namedParam);
        }
    }

    public static String escapeWildcard(String value) {
        return value.replaceAll("%", "\\\\%").replaceAll("_", "\\\\_");
    }

    public static String match(String pattern, MatchMode matchMode) {
        return matchMode.toMatchString(pattern);
    }

    public static String gt(String property, String namedParam) {
        return property + ">" + namedParam;
    }

    public static String lt(String property, String namedParam) {
        return property + "<" + namedParam;
    }
}
