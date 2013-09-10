package org.zanata.client.commands.push;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

import org.fedorahosted.openprops.Properties;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.zanata.rest.dto.resource.Resource;

public class PropertiesPushStrategyTest
{
   private File outDir = new File("target/test-output/readprops/");
   private Properties props;

   @Mock
   private PushOptions opts;

   @Before
   public void prepare()
   {
      outDir.mkdirs();
      props = new Properties();
      props.setProperty("key", "value");
      props.setProperty("unicode", "レス");
   }

   @Before
   public void beforeMethod()
   {
      MockitoAnnotations.initMocks(this);
      when(opts.getSourceLang()).thenReturn("en");
   }

   @Test
   public void utf8() throws Exception
   {
      File f = new File(outDir, "utf8.properties");
      FileOutputStream fos = new FileOutputStream(f);
      try
      {
         Writer w = new OutputStreamWriter(fos, "UTF-8");
         props.store(w, null);
      }
      finally
      {
         fos.close();
      }
      PropertiesStrategy strat = new PropertiesStrategy("UTF-8");
      strat.setPushOptions(opts);
      strat.init();

      Resource doc = strat.loadSrcDoc(outDir, "utf8");
      checkResults(doc);
   }

   @Test
   public void latin1() throws Exception
   {
      File f = new File(outDir, "latin1.properties");
      FileOutputStream fos = new FileOutputStream(f);

      try
      {
         props.store(fos, null);
      }
      finally
      {
         fos.close();
      }

      PropertiesStrategy strat = new PropertiesStrategy();
      strat.setPushOptions(opts);
      strat.init();

      Resource doc = strat.loadSrcDoc(outDir, "latin1");
      checkResults(doc);
   }

   // TODO test translated properties files

   private void checkResults(Resource doc)
   {
      assertEquals(doc.getTextFlows().get(0).getId(), "key");
      assertEquals(doc.getTextFlows().get(0).getContents(), asList("value"));
      assertEquals(doc.getTextFlows().get(1).getId(), "unicode");
      assertEquals(doc.getTextFlows().get(1).getContents(), asList("レス"));
   }

}
