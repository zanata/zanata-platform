package org.zanata.client.commands.pull;

import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.fedorahosted.openprops.Properties;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import org.zanata.rest.dto.resource.Resource;
import org.zanata.rest.dto.resource.TextFlow;

@Test
public class PropertiesPullStrategyTest
{
   File outDir = new File("target/test-output/writeprops/");
   Properties props = new Properties();

   @Mock
   private PullOptions opts;

   private Resource doc;

   @BeforeTest
   public void prepare()
   {
      outDir.mkdirs();
      doc = new Resource(null);
      doc.getTextFlows().add(newTextFlow("key", "value"));
      doc.getTextFlows().add(newTextFlow("unicode", "レス"));
   }

   @BeforeMethod
   public void beforeMethod()
   {
      MockitoAnnotations.initMocks(this);
      when(opts.getSrcDir()).thenReturn(outDir);
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
