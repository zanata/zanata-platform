package net.openl10n.flies.client.ant.po;

import net.openl10n.flies.client.ant.po.DownloadPoTask;
import net.openl10n.flies.client.ant.po.UploadPoTask;
import junit.framework.Test;
import junit.framework.TestSuite;

@SuppressWarnings("nls")
public class RemoteTest extends AbstractBuildTest
{
   /**
    * This helps Infinitest, since it doesn't know about the taskdefs inside
    * build.xml
    */
   @SuppressWarnings("unchecked")
   static Class<?>[] testedClasses = { UploadPoTask.class, DownloadPoTask.class };

   public RemoteTest(String name)
   {
      super(name);
   }

   @Override
   protected String getBuildFile()
   {
      return "src/test/resources/net/openl10n/flies/client/ant/po/build.xml";
   }

   public static Test suite()
   {
      TestSuite suite = new TestSuite(RemoteTest.class.getName());
      suite.addTest(new RemoteTest("roundtripremote"));
      return suite;
   }

}
