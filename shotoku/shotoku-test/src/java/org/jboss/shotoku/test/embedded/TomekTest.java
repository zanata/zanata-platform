package org.jboss.shotoku.test.embedded;

import java.util.Set;
import java.util.HashSet;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

/**
 * @author Adam Warski (adamw@aster.pl)
 */
public class TomekTest {
    public static<T> Set<T> getSet(T... objs) {
        Set<T> ret = new HashSet<T>();
        for (T obj : objs) {
            ret.add(obj);
        }

        return ret;
    }

    public static void x(int[] a) {
        a[2] = 10;
    }

    /**
     * A pattern which matcher variables of the form "${text with numbers}".
     */
    private final static Pattern variablesPattern = Pattern.compile(
            Pattern.quote("${") + "[a-zA-Z0-9]*" + Pattern.quote("}"));

    private static void addVariablesFromNode(String text) {
        Matcher m = variablesPattern.matcher(text);

        while (m.find()) {
            System.out.println(text.substring(m.start(), m.end()));
        }
    }

    public static void main(String[] args) {
        /*int[] t = {1, 2, 3};
        System.out.println(t[2]);
        x(t);
        System.out.println(t[2]);*/

        addVariablesFromNode("templates/${type}-normal.vm");
    }
}
