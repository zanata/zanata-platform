package org.zanata.util;

import org.junit.Assert;
import org.junit.Test;

import static org.zanata.util.OkapiUtil.countWords;

public class OkapiUtilTest {
    // @formatter:off
    private String[] strings = {
        "<author><firstname>Emmanuel</firstname> <surname>Bernard</surname></author>",
        "Graphic Design",
        "the object is passed up to the UI tier",

        "// in the first session\n" +
        "Cat cat = (Cat) firstSession.load(Cat.class, catID);\n" +
        "\n" +
        "// in a higher tier of the application\n" +
        "Cat mate = new Cat();\n" +
        "cat.setMate(mate);\n" +
        "\n" +
        "// later, in a new session\n" +
        "secondSession.saveOrUpdate(cat);   // update existing state (cat has a non-null id)\n" +
        "secondSession.saveOrUpdate(mate);  // save the new instance (mate has a null id)",

        "<filename class=\"directory\">/var/lib/ricci</filename>",

        // NB okapi says 2, but looks like 4. perhaps
        // something.ext counts as one word?
        "https://cdn.redhat.com",

        // NB okapi says 3, but looks like 4
        "/etc/rhsm/rhsm.conf",
    };
    // @formatter:on
    // These counts represent the expected word counts for the strings listed
    // above.
    private long[] count = {
            // Includes tags
            8,
            // Simple text
            2, 9,
            // Okapi says the fourth string has 50 words, but it looks like 54
            // non-null is considered 2 words, while firstSession.load is 1
            50,
            // Includes tags
            7,
            // https and cdn.redhat.com
            2,
            // Slashes
            3 };

    @Test
    public void testCountWords() {
        countWords(null, "en-US");
        int i = 0;
        for (String s : strings) {
            long expected = count[i++];
            System.out.println(s + ": Expecting " + expected + " words");
            Assert.assertEquals(expected, countWords(s, "en-US"));
        }
    }

}
