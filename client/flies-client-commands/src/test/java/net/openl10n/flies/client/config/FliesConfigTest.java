package net.openl10n.flies.client.config;

import java.io.File;
import java.net.URL;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import junit.framework.TestCase;

import org.apache.commons.configuration.CompositeConfiguration;
import org.apache.commons.configuration.FileConfiguration;
import org.apache.commons.configuration.HierarchicalINIConfiguration;
import org.apache.commons.configuration.SystemConfiguration;

public class FliesConfigTest extends TestCase
{
   JAXBContext jc = JAXBContext.newInstance(FliesConfig.class);
   Unmarshaller unmarshaller = jc.createUnmarshaller();
   Marshaller marshaller = jc.createMarshaller();
   File fliesProjectXml = new File(System.getProperty("user.dir"), "target/flies.xml");
   File fliesUserFile = new File(System.getProperty("user.dir"), "target/flies.ini");

   public FliesConfigTest() throws Exception
   {
      marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
   }

   // public void testGenerateSchema() throws Exception
   // {
   // GenerateSchema.generateSchemaToStdout(jc);
   // }

   public void testWriteReadProject() throws Exception
   {
      writeProject();
      readProject();
   }

   void writeProject() throws Exception
   {
      FliesConfig config = new FliesConfig();
      config.setUrl(new URL("http://example.com"));
      config.setProject("project");
      config.setProjectVersion("version");
      config.getLocales().add(new LocaleMapping("fr", "fr-FR"));
      config.getLocales().add(new LocaleMapping("  zh-CN  "));
      marshaller.marshal(config, fliesProjectXml);
   }

   void readProject() throws Exception
   {
      FliesConfig config = (FliesConfig) unmarshaller.unmarshal(fliesProjectXml);
      assertEquals(new URL("http://example.com"), config.getUrl());
      assertEquals("project", config.getProject());
      assertEquals("version", config.getProjectVersion());
      LocaleList locales = config.getLocales();
      assertEquals(2, locales.size());
      assertEquals("fr", locales.get(0).getLocale());
      assertEquals("fr-FR", locales.get(0).getMapFrom());
      assertEquals("zh-CN", locales.get(1).getLocale());
      assertNull(locales.get(1).getMapFrom());
   }

   public void testWriteReadUser() throws Exception
   {
      writeUser();
      readUser();
   }

   void writeUser() throws Exception
   {
      FileConfiguration config = new HierarchicalINIConfiguration(fliesUserFile);
      config.setProperty("flies.url", new URL("http://flies.example.com/"));
      config.setProperty("flies.username", "admin");
      config.setProperty("flies.key", "b6d7044e9ee3b2447c28fb7c50d86d98");
      config.setProperty("flies.debug", false);
      config.setProperty("flies.errors", true);

      config.save();
   }

   void readUser() throws Exception
   {
      CompositeConfiguration config = new CompositeConfiguration();
      config.addConfiguration(new SystemConfiguration());
      config.addConfiguration(new HierarchicalINIConfiguration(fliesUserFile));
      String user = config.getString("flies.username");
      assertEquals("admin", user);
      boolean debug = config.getBoolean("flies.debug");
      assertFalse(debug);
      boolean errors = config.getBoolean("flies.errors");
      assertTrue(errors);
   }

}
