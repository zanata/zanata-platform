package org.zanata.search;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class LevenshteinTokenUtil {
    private static final String SPLIT_REGEX = "[,.]?[\\s]+";

    private static final Set<String> stopwords;

    static {
        Set<String> stopwordsSet = new HashSet<String>();
        try {
            BufferedReader reader =
                    new BufferedReader(new InputStreamReader(
                            LevenshteinTokenUtil.class
                                    .getResourceAsStream("stopwords.txt")));
            try {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (!line.startsWith("#")) {
                        stopwordsSet.add(line);
                    }
                }
            } finally {
                reader.close();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        stopwords = Collections.unmodifiableSet(stopwordsSet);
    }

    /**
     * Compute Levenshtein distance. Taken from
     * http://web.archive.org/web/20110720093554
     * /http://www.merriampark.com/ldjava.htm (public domain)
     */
    public static int getLevenshteinDistanceInWords(String[] s, String[] t) {
        if (s == null || t == null) {
            throw new IllegalArgumentException("Strings must not be null");
        }

        /*
         * The difference between this impl. and the previous is that, rather
         * than creating and retaining a matrix of size s.length()+1 by
         * t.length()+1, we maintain two single-dimensional arrays of length
         * s.length()+1. The first, d, is the 'current working' distance array
         * that maintains the newest distance cost counts as we iterate through
         * the characters of String s. Each time we increment the index of
         * String t we are comparing, d is copied to p, the second int[]. Doing
         * so allows us to retain the previous cost counts as required by the
         * algorithm (taking the minimum of the cost count to the left, up one,
         * and diagonally up and to the left of the current cost count being
         * calculated). (Note that the arrays aren't really copied anymore, just
         * switched...this is clearly much better than cloning an array or doing
         * a System.arraycopy() each time through the outer loop.)
         *
         * Effectively, the difference between the two implementations is this
         * one does not cause an out of memory condition when calculating the LD
         * over two very large strings.
         */

        int n = s.length; // length of s
        int m = t.length; // length of t

        if (n == 0) {
            return m;
        } else if (m == 0) {
            return n;
        }

        int p[] = new int[n + 1]; // 'previous' cost array, horizontally
        int d[] = new int[n + 1]; // cost array, horizontally
        int _d[]; // placeholder to assist in swapping p and d

        // indexes into strings s and t
        int i; // iterates through s
        int j; // iterates through t

        String t_j; // jth token of t

        int cost; // cost

        for (i = 0; i <= n; i++) {
            p[i] = i;
        }

        for (j = 1; j <= m; j++) {
            t_j = t[j - 1];
            d[0] = j;

            for (i = 1; i <= n; i++) {
                cost = s[i - 1].equals(t_j) ? 0 : 1;
                // minimum of cell to the left+1, to the top+1, diagonally left
                // and up +cost
                d[i] =
                        Math.min(Math.min(d[i - 1] + 1, p[i] + 1), p[i - 1]
                                + cost);
            }

            // copy current distance counts to 'previous row' distance counts
            _d = p;
            p = d;
            d = _d;
        }

        // our last action in the above loop was to switch d and p, so p now
        // actually has the most recent cost counts
        return p[n];
    }

    public static double getSimilarity(final String s1, final String s2) {
        String[] s1s = tokenise(s1);
        String[] s2s = tokenise(s2);

        int levDistance = getLevenshteinDistanceInWords(s1s, s2s);
        int maxDistance = Math.max(s1s.length, s2s.length);
        double similarity = (maxDistance - levDistance) / (double) maxDistance;
        return similarity;
    }

    /**
     * Splits into tokens (lower-case).
     *
     * @param s
     * @return
     */
    static String[] tokenise(String s) {
        String[] tokens = s.toLowerCase().split(SPLIT_REGEX);
        ArrayList<String> list = new ArrayList<String>(tokens.length);
        for (String tok : tokens) {
            if (!stopwords.contains(tok)) {
                list.add(tok);
            }
        }
        return list.toArray(new String[list.size()]);
    }

    private static int countExtraStringLengths(List<String> strings,
            int fromIndex) {
        int total = 0;
        for (int i = fromIndex; i < strings.size(); i++) {
            String s = strings.get(i);
            String[] ss = tokenise(s);
            total += ss.length;
        }
        return total;
    }

    /**
     * Quick and dirty similarity score for a query string against a list of
     * strings. Returns the mean similarity of s1 against each string in the
     * list.
     *
     * @param s1
     * @param strings2
     * @return
     */
    public static double getSimilarity(final String s1,
            final List<String> strings2) {
        double totalSimilarity = 0.0;
        int stringCount = strings2.size();
        for (int i = 0; i < stringCount; i++) {
            String s2 = strings2.get(i);
            totalSimilarity += getSimilarity(s1, s2);
        }
        double meanSimilarity = totalSimilarity / stringCount;
        return meanSimilarity;
    }

    public static double getSimilarity(final List<String> strings1,
            final List<String> strings2) {
        // length of the shorter list
        int minListSize;

        // count the extra strings first:
        int extraStringLengths; // total of "extra" strings in the longer list
        if (strings1.size() < strings2.size()) {
            minListSize = strings1.size();
            extraStringLengths = countExtraStringLengths(strings2, minListSize);
        } else {
            minListSize = strings2.size();
            extraStringLengths = countExtraStringLengths(strings1, minListSize);
        }

        // total of Levenshtein distance between corresponding strings in the
        // two lists, plus the length of any extra strings if one list is longer
        int totalLevDistance = extraStringLengths;
        // total of max editing distance between all the corresponding strings,
        // plus length of extra strings
        int totalMaxDistance = extraStringLengths;

        // now count the strings which correspond between both lists
        for (int i = 0; i < minListSize; i++) {
            String[] s1 = tokenise(strings1.get(i));
            String[] s2 = tokenise(strings2.get(i));
            int levenshteinDistance = getLevenshteinDistanceInWords(s1, s2);
            totalLevDistance += levenshteinDistance;
            totalMaxDistance += Math.max(s1.length, s2.length);
        }
        double similarity =
                (totalMaxDistance - totalLevDistance)
                        / (double) totalMaxDistance;
        return similarity;
    }

}
