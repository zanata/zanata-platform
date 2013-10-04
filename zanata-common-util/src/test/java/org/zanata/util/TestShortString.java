package org.zanata.util;

import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import org.testng.annotations.Test;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

public class TestShortString {

    @Test
    public void testShortenShortString() {
        String s1 = "string which is already short";
        String s2 = ShortString.shorten(s1);
        assertSame(s1, s2);
    }

    @Test
    @SuppressFBWarnings("ES_COMPARING_STRINGS_WITH_EQ")
    public void testShortenLongString() {
        String s1 =
                "string which is really quite long. string which is really quite long. string which is really quite long. string which is really quite long. string which is really quite long. ";
        String s2 = ShortString.shorten(s1);
        assertTrue(s2.length() <= ShortString.MAX_LENGTH);
        String s3 = ShortString.shorten(s2);
        assertSame(s2, s3);
    }

}
