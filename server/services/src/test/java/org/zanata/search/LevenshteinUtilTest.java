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
import org.junit.Assert;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.data.Offset.offset;

/**
 * @author Sean Flanigan <a
 *         href="mailto:sflaniga@redhat.com">sflaniga@redhat.com</a>
 *
 */
public class LevenshteinUtilTest {
    private static final Offset<Double> DELTA = offset(0.0001);

    @Test
    public void testVarious() {
        String s1 = "one two";
        String s2 = "one two three four five";
        String s3 = "one";
        String s4 = "dbnoicgjnedbitnhjudbioe";

        double similarity = LevenshteinUtil.getSimilarity(s1, s1);
        Assert.assertTrue(similarity > 0.999f);

        similarity = LevenshteinUtil.getSimilarity(s1, s2);
        Assert.assertTrue(similarity > 0.3f);
        Assert.assertTrue(similarity < 0.4f);

        similarity = LevenshteinUtil.getSimilarity(s1, s3);
        Assert.assertTrue(similarity > 0.4f);
        Assert.assertTrue(similarity < 0.5f);

        similarity = LevenshteinUtil.getSimilarity(s1, s4);
        Assert.assertTrue(similarity < 0.3f);
    }

    @Test
    public void testPoint996Similarity() {
        String s1 =
                "1234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789";
        String s2 =
                "12345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890";

        double similarity = LevenshteinUtil.getSimilarity(s1, s2);
        Assert.assertTrue(similarity > 0.99f);
        Assert.assertTrue(similarity < 1.0f);
    }

    @Test
    public void testDifferentSizedLists() {
        List<String> strings1 = Arrays.asList("1234567890", "abcdefghij");
        List<String> strings2 = Arrays.asList("1234567890abcdefghij");
        double similarity = LevenshteinUtil.getSimilarity(strings1, strings2);
        Assert.assertTrue(similarity > 0.3);
        Assert.assertTrue(similarity < 0.4);
    }

    @Test
    public void testSimilarLists() {
        List<String> strings1 = Arrays.asList("1234567890", "abcdefghij");
        List<String> strings2 = Arrays.asList("123456789", "bcdefghij");
        double similarity = LevenshteinUtil.getSimilarity(strings1, strings2);
        assertThat(similarity).isEqualTo(0.9, DELTA);
    }

    @Test
    public void testMisorderedLists() {
        List<String> strings1 = Arrays.asList("1234567890", "abcdefghij");
        List<String> strings2 = Arrays.asList("abcdefghij", "1234567890");
        double similarity = LevenshteinUtil.getSimilarity(strings1, strings2);
        assertThat(similarity).isEqualTo(0.0, DELTA);
    }

    @Test
    public void testIdenticalLists() {
        List<String> strings1 = Arrays.asList("one", "two");
        List<String> strings2 = Arrays.asList("one", "two");
        double similarity = LevenshteinUtil.getSimilarity(strings1, strings2);
        assertThat(similarity).isEqualTo(1.0, DELTA);
    }

}
