package org.zanata.client.ant.po;

import org.zanata.client.ant.po.DownloadPoTask;
import org.zanata.client.ant.po.UploadPoTask;

import junit.framework.Test;
import junit.framework.TestSuite;

@SuppressWarnings("nls")
public class LocalTest extends AbstractBuildTest
{
   /**
    * This helps Infinitest, since it doesn't know about the taskdefs inside
    * build.xml
    */
   @SuppressWarnings("unchecked")
   static Class<?>[] testedClasses = { UploadPoTask.class, DownloadPoTask.class };

   public LocalTest(String name)
   {
      super(name);
   }

   @Override
   protected String getBuildFile()
   {
      return "src/test/resources/org/zanata/client/ant/po/build.xml";
   }

   public static Test suite()
   {
      TestSuite suite = new TestSuite(LocalTest.class.getName());
      suite.addTest(new LocalTest("dummy"));
      // FIXME get the other tests working
      // suite.addTest(new LocalTest("uploadpo"));
      // suite.addTest(new LocalTest("downloadpo"));
      // suite.addTest(new LocalTest("roundtriplocal"));
      return suite;
   }

   // @Override
   // protected void tearDown() throws Exception {
   // String outDir = getProject().getProperty("out.dir");
   // if (outDir != null) {
   // TestUtil.delete(new File(outDir));
   // }
   // super.tearDown();
   // }

}
