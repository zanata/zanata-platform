/*
 * Copyright 2011, Red Hat, Inc. and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.zanata.search;

import java.util.Arrays;
import java.util.List;

import org.assertj.core.data.Offset;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.data.Offset.offset;
import static org.junit.Assert.assertTrue;

/**
 * @author Sean Flanigan <a
 *         href="mailto:sflaniga@redhat.com">sflaniga@redhat.com</a>
 *
 */
public class LevenshteinTokenUtilTest {
    private static final Offset<Double> DELTA = offset(0.0001);

    @Test
    public void testVarious() {
        String s1 = "one two";
        String s2 = "one two three four five";
        String s3 = "one";
        String s4 = "dbnoicgjnedbitnhjudbioe";

        double similarity = LevenshteinTokenUtil.getSimilarity(s1, s1);
        assertTrue(similarity > 0.999f);

        similarity = LevenshteinTokenUtil.getSimilarity(s1, s2);
        assertTrue(similarity > 0.3f);
        assertTrue(similarity < 0.4f);

        similarity = LevenshteinTokenUtil.getSimilarity(s1, s3);
        assertThat(similarity).isEqualTo(0.5, DELTA);

        similarity = LevenshteinTokenUtil.getSimilarity(s1, s4);
        assertThat(similarity).isEqualTo(0.0, DELTA);
    }

    @Test
    public void testPoint96Similarity() {
        String s1 =
                "1234567890 1234567890 1234567890 1234567890 1234567890 1234567890 1234567890 1234567890 1234567890 1234567890 1234567890 1234567890 1234567890 1234567890 1234567890 1234567890 1234567890 1234567890 1234567890 1234567890 1234567890 1234567890 1234567890 1234567890 1234567890 1234567890 1234567890 1234567890 123456789";
        String s2 =
                "1234567890 1234567890 1234567890 1234567890 1234567890 1234567890 1234567890 1234567890 1234567890 1234567890 1234567890 1234567890 1234567890 1234567890 1234567890 1234567890 1234567890 1234567890 1234567890 1234567890 1234567890 1234567890 1234567890 1234567890 1234567890 1234567890 1234567890 1234567890 1234567890";

        double similarity = LevenshteinTokenUtil.getSimilarity(s1, s2);
        assertTrue(similarity > 0.95f);
        assertTrue(similarity < 1.0f);
    }

    @Test
    public void testDifferentSizedLists() {
        List<String> strings1 = Arrays.asList("1234567890", "abcdefghij");
        List<String> strings2 = Arrays.asList("1234567890abcdefghij");
        double similarity =
                LevenshteinTokenUtil.getSimilarity(strings1, strings2);
        assertThat(similarity).isEqualTo(0.0, DELTA);
    }

    @Test
    public void testSimilarLists() {
        List<String> strings1 =
                Arrays.asList("123 456 78 90", "a_ bc_ def ghi j");
        List<String> strings2 = Arrays.asList("123 456 78 9", "bc_ def ghi j");
        double similarity =
                LevenshteinTokenUtil.getSimilarity(strings1, strings2);
        assertTrue(similarity > 0.7);
        assertTrue(similarity < 0.8);
    }

    @Test
    public void testMisorderedLists() {
        List<String> strings1 = Arrays.asList("1234567890", "abcdefghij");
        List<String> strings2 = Arrays.asList("abcdefghij", "1234567890");
        double similarity =
                LevenshteinTokenUtil.getSimilarity(strings1, strings2);
        assertThat(similarity).isEqualTo(0.0, DELTA);
    }

    @Test
    public void testIdenticalLists() {
        List<String> strings1 = Arrays.asList("one", "two");
        List<String> strings2 = Arrays.asList("one", "two");
        double similarity =
                LevenshteinTokenUtil.getSimilarity(strings1, strings2);
        assertThat(similarity).isEqualTo(1.0, DELTA);
    }

    @Test
    public void testTokenise() {
        String[] foobar = LevenshteinTokenUtil.tokenise("foo bar baz");
        assert foobar.length == 3;

        String[] thefoobar =
                LevenshteinTokenUtil.tokenise("The foo IS NOT bar");
        assert thefoobar.length == 2;
    }

    @Test
    public void testStopWordsIgnoredWhenOtherWordsPresent() {
        assertDifferentStringSimilarity("The foo is not bar", "A foo bar", 1.0);
        assertDifferentStringSimilarity("An bar is an baz", "My bar is foo", 0.5);
    }

    /**
     * TODO review this behaviour, it is confusing to users.
     */
    @Test
    public void testStopWordsOnlyAlwaysHaveZeroSimilarity() {
        assertDifferentStringSimilarity("The is not", "A", 0.0);
        assertDifferentStringSimilarity("The not is and", "It not is but", 0.0);
    }

    @Test
    public void testIdenticalStringsSimilarity() {
        assertIdenticalStringsAreSimilar(
                "I am the very model of a modern major general");
    }

    @Test
    public void testEmptyListOfStringsSimilarity() {
        double similarity = LevenshteinTokenUtil.getSimilarity(
                Arrays.<String>asList(), Arrays.<String>asList());
        assertThat(similarity).isEqualTo(1.0, DELTA);
    }

    @Test
    public void testEmptyStringsSimilarity() {
        assertIdenticalStringsAreSimilar("");
    }

    /**
     * Asserts that getSimilarity gives 1.00 when matching the given string
     * against itself.
     *
     * @param s the string to test
     */
    private void assertIdenticalStringsAreSimilar(String s) {
        double similarity = LevenshteinTokenUtil.getSimilarity(Arrays.asList(s),
                Arrays.asList(s));
        assertThat(similarity).isEqualTo(1.0, DELTA);
    }

    /**
     * Asserts that getSimilarity gives the expected value when matching the
     * given strings against each other.
     *
     * @param s1 the first string to test
     * @param s2 the other string to test
     */
    private void assertDifferentStringSimilarity(String s1, String s2,
            double expected) {
        double similarity = LevenshteinTokenUtil
                .getSimilarity(Arrays.asList(s1), Arrays.asList(s2));
        assertThat(similarity).isEqualTo(expected, DELTA);
    }


}
