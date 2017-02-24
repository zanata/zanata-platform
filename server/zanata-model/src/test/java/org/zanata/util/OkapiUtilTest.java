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
    private long[] count = { 2, 2, 9,
            // Okapi says the fourth string has 49 words, but it looks like 54
            // ?!
            // TODO work out why
            49, 3, 2, 3, };

    @Test
    public void testCountWords() {
        countWords(null, "en-US");
        int i = 0;
        for (String s : strings) {
            long expected = count[i++];
            long n = countWords(s, "en-US");
            Assert.assertEquals(expected, n);
            // if (n == 0)
            // Assert.fail(s + ":" + n);
            // else
            // System.out.println(n);
        }
    }

}
