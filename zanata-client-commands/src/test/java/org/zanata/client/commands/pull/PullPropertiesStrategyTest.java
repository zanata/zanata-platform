package org.zanata.client.commands.pull;

import static org.testng.Assert.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.easymock.EasyMock;
import org.easymock.IMocksControl;
import org.fedorahosted.openprops.Properties;
import org.testng.annotations.Test;
import org.zanata.rest.dto.resource.Resource;
import org.zanata.rest.dto.resource.TextFlow;

@Test
public class PullPropertiesStrategyTest
{
   IMocksControl control = EasyMock.createControl();

   File outDir = new File("target/test-output/writeprops/");
   Properties props = new Properties();
   PullOptions opts;
   Resource doc;

   public PullPropertiesStrategyTest()
   {
      outDir.mkdirs();
      opts = control.createMock(PullOptions.class);
      EasyMock.expect(opts.getSrcDir()).andReturn(outDir).anyTimes();
      control.replay();
      doc = new Resource(null);
      doc.getTextFlows().add(newTextFlow("key", "value"));
      doc.getTextFlows().add(newTextFlow("unicode", "レス"));
   }

   @Test
   public void utf8() throws Exception
   {
      PullStrategy strat = new UTF8PropertiesStrategy();
      strat.setPullOptions(opts);

      doc.setName("utf8");
      strat.writeSrcFile(doc);

      File f = new File(outDir, "utf8.properties");
      InputStreamReader r = new InputStreamReader(new FileInputStream(f), "UTF-8");
      props.load(r);
      checkResults(props);
   }


   @Test
   public void latin1() throws Exception
   {
      PullStrategy strat = new PropertiesStrategy();
      strat.setPullOptions(opts);

      doc.setName("latin1");
      strat.writeSrcFile(doc);

      File f = new File(outDir, "latin1.properties");
      InputStream inStream = new FileInputStream(f);
      props.load(inStream);
      checkResults(props);
   }

   private TextFlow newTextFlow(String key, String value)
   {
      TextFlow tf = new TextFlow();
      tf.setId(key);
      tf.setContents(value);
      return tf;
   }

   private void checkResults(Properties props)
   {
      assertEquals(props.getProperty("key"), "value");
      assertEquals(props.getProperty("unicode"), "レス");
   }

}
