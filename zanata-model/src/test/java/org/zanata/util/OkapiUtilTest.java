package org.zanata.util;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.zanata.util.OkapiUtil.countWords;

import org.testng.Assert;
import org.testng.annotations.Test;

@Test(groups = { "unit-tests" })
public class OkapiUtilTest
{
// @formatter:off
   String[] strings = 
   {
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

         "https://cdn.redhat.com", // NB okapi says 2, but looks like 4. perhaps
                                   // something.ext counts as one word?

         "/etc/rhsm/rhsm.conf", // NB okapi says 3, but looks like 4
   };
      // @formatter:on
   long[] count = { 2, 2, 9,
         // 4+8+7+4+3+5+11+12 = 54 ?
         49, 3, 2, 3, };

   @Test
   public void testCountWords()
   {
      countWords(null, "en-US");
      int i = 0;
      for (String s : strings)
      {
         long expected = count[i++];
         long n = countWords(s, "en-US");
         Assert.assertEquals(n, expected);
         // if (n == 0)
         // Assert.fail(s + ":" + n);
         // else
         // System.out.println(n);
      }
   }

   @Test
   public void extractPlainTextContent() throws Exception
   {
      assertThat(OkapiUtil.extractPlainTextTmxContent("<bpt i=\"1\" x=\"1\">{\\b </bpt>Special<ept i=\"1\">}</ept> text"),
            equalTo("Special text"));
      assertThat(OkapiUtil.extractPlainTextTmxContent("<bpt i=\"1\" x=\"1\">{\\cf7 </bpt>Special<ept i=\"1\">}</ept> text"),
            equalTo("Special text"));
      assertThat(OkapiUtil.extractPlainTextTmxContent("<bpt i=\"1\" x=\"1\">&lt;B></bpt>Special<ept i=\"1\">&lt;/B></ept> text"),
            equalTo("Special text"));

      assertThat(OkapiUtil.extractPlainTextTmxContent("The <bpt i=\"1\" x=\"1\">&lt;i></bpt><bpt i=\"2\" x=\"2\">&lt;b></bpt>" +
            "big<ept i=\"2\">&lt;/b></ept> black<ept i=\"1\">&lt;/i></ept> cat."),
            equalTo("The big black cat."));

      assertThat(OkapiUtil.extractPlainTextTmxContent("The icon <ph x=\"1\">&lt;img src=\"testNode.gif\"/></ph>represents " +
            "a conditional node."),
            equalTo("The icon represents a conditional node."));
   }
}
