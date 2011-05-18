package org.zanata.util;

import static org.zanata.util.OkapiUtil.*;

import org.testng.Assert;
import org.testng.annotations.Test;

@Test(groups = { "unit-tests" })
public class OkapiUtilTest 
{
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
         "secondSession.saveOrUpdate(mate);  // save the new instance (mate has a null id)"
   };
   long[] count = 
   {
         8,
         2,
         9,
//         4+8+7+4+3+5+11+12 = 54 ?
         49
   };
  @Test
  public void testCountWords() 
   {
     countWords(null, "en-US");
     int i=0;
     for (String s : strings) 
     {
        long expected = count[i++];
        long n = countWords(s, "en-US");
        Assert.assertEquals(n, expected);
//        if (n == 0)
//           Assert.fail(s + ":" + n);
//        else
//           System.out.println(n);
     }
  }
}
