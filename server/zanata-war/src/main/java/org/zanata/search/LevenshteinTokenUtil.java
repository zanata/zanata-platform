package org.zanata.search;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class LevenshteinTokenUtil {
    private static final String SPLIT_REGEX = "[,.]?[\\s]+";

    private static final Set<String> stopwords;

    static {
        Set<String> stopwordsSet = new HashSet<>();
        try (BufferedReader reader =
                    new BufferedReader(new InputStreamReader(
                            LevenshteinTokenUtil.class
                                    .getResourceAsStream("stopwords.txt"),
                            StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.startsWith("#")) {
                    stopwordsSet.add(line);
                }
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
        // FIXME maxDistance can be 0, leading to divide-by-zero
        double similarity = (maxDistance - levDistance) / (double) maxDistance;
        return similarity;
    }

    /**
     * Splits into tokens (lower-case).
     *
     * @param s the string to tokenise
     * @return an array of lowercase tokens (words)
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
     * @param s1 string to compare against each other string
     * @param strings2 other strings to compare s1 against
     * @return mean similarity between s1 and each of strings2
     */
    public static double getSimilarity(final String s1,
            final List<String> strings2) {
        double totalSimilarity = 0.0;
        for (String s2 : strings2) {
            totalSimilarity += getSimilarity(s1, s2);
        }
        return totalSimilarity / strings2.size();
    }

    /**
     * Calculate the word-based case-insensitive similarity of two lists of
     * strings (range 0.0 to 1.0).
     *
     *  - Strings at the same index are compared.
     *  - Stop-words are ignored in comparisons. See #stopwords.
     *  - When both lists are empty, they are considered identical (returns 1.0)
     *  - Empty strings are considered identical to other empty strings.
     *
     * If a string is made up only of stop-words, the similarity will always be
     * 0.0 regardless of the actual similarity of the stop-words.
     *
     * TODO review use of stop-words in these comparisons, since results can
     * often be confusing to end-users.
     *
     * @param strings1 a list of strings to compare
     * @param strings2 the other list of strings to compare
     * @return average similarity between the strings, between 0.0 and 1.0
     */
    public static double getSimilarity(final List<String> strings1,
            final List<String> strings2) {
        // all empty lists are identical
        if (strings1.isEmpty() && strings2.isEmpty()) {
            return 1.0;
        }

        // length of the shorter list
        final int minListSize = Math.min(strings1.size(), strings2.size());
        final List<String> longestList = strings1.size() > minListSize ?
                strings1 : strings2;

        // total of "extra" strings in the longer list
        final int extraStringLengths =
                countExtraStringLengths(longestList, minListSize);

        // running total of Levenshtein distance between corresponding strings
        // in the two lists
        int cumulativeLevDistance = 0;

        // running total of max editing distance between all the corresponding
        // strings.
        int cumulativeMaxDistance = 0;

        // count the strings which correspond between both lists
        for (int i = 0; i < minListSize; i++) {
            final String string1 = strings1.get(i);
            final String string2 = strings2.get(i);
            String[] tokens1 = tokenise(string1);
            String[] tokens2 = tokenise(string2);
            final int levenshteinDistance =
                    getLevenshteinDistanceInWords(tokens1, tokens2);
            cumulativeLevDistance += levenshteinDistance;

            // When a string contains only stop words, tokenise returns an empty
            // array, so this value can remain at 0.
            cumulativeMaxDistance += Math.max(tokens1.length, tokens2.length);
        }
        final int totalLevDistance = cumulativeLevDistance + extraStringLengths;
        final int totalMaxDistance = cumulativeMaxDistance + extraStringLengths;

        // if there would be a divide-by-zero situation due to all strings being
        // only stop-words, return 0 instead.
        if (totalMaxDistance == 0) {
            return 0.0;
        }

        return (totalMaxDistance - totalLevDistance) / (double) totalMaxDistance;
    }

}
