package org.zanata.util;

import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.junit.Test;

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

    @Test
    @SuppressFBWarnings("ES_COMPARING_STRINGS_WITH_EQ")
    public void testConfigurableShortenLongString() {
        int maximumLength = 15;
        String s1 =
                "string which is really quite long. string which is really quite long. string which is really quite long. string which is really quite long. string which is really quite long. ";
        String s2 = ShortString.shorten(s1, maximumLength);
        assertTrue(s2.length() <= maximumLength);
        String s3 = ShortString.shorten(s2, maximumLength);
        assertSame(s2, s3);
    }
}
