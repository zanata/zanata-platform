package org.zanata.client.commands.gettext;

import java.io.File;


import org.fest.assertions.Assertions;
import org.testng.annotations.Test;
import org.zanata.client.commands.gettext.PublicanUtil;

@Test(groups = "unit-tests")
public class PublicanUtilTest
{

   public void test1() throws Exception
   {
      File dir = new File(".");
      File file = new File(dir, "sub/myfile.pot");
      Assertions.assertThat("sub/myfile.pot").isEqualTo(PublicanUtil.getSubPath(dir, file));
   }

   public void test2() throws Exception
   {
      File dir = new File("pot");
      File file = new File("pot/sub/myfile.pot");
      Assertions.assertThat("sub/myfile.pot").isEqualTo(PublicanUtil.getSubPath(dir, file));
   }

   public void test3() throws Exception
   {
      File dir = new File("/tmp/pot");
      File file = new File("/tmp/pot/sub/myfile.pot");
      Assertions.assertThat("sub/myfile.pot").isEqualTo(PublicanUtil.getSubPath(dir, file));
   }

}
