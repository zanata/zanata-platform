package org.fedorahosted.flies.client.config;

import java.io.File;
import java.net.URL;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import junit.framework.TestCase;

import org.apache.commons.configuration.CompositeConfiguration;
import org.apache.commons.configuration.FileConfiguration;
import org.apache.commons.configuration.HierarchicalINIConfiguration;
import org.apache.commons.configuration.SystemConfiguration;
import org.fedorahosted.flies.rest.GenerateSchema;

// FIXME make this a real test (ie add assertions)
public class TestFliesConfig extends TestCase
{
   JAXBContext jc = JAXBContext.newInstance(FliesConfig.class);
   Unmarshaller unmarshaller = jc.createUnmarshaller();
   Marshaller marshaller = jc.createMarshaller();
   File fliesProjectXml = new File(System.getProperty("user.dir"), "target/flies.xml");
   File fliesUserFile = new File(System.getProperty("user.dir"), "target/.config/flies.ini");

   public TestFliesConfig() throws Exception
   {
      marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
   }

   public void testGenerateSchema() throws Exception
   {
      GenerateSchema.generateSchemaToStdout(jc);
   }

   public void testWriteReadProject() throws Exception
   {
      writeProject();
      readProject();
   }

   void writeProject() throws Exception
   {
      FliesConfig config = new FliesConfig();
      config.getDocSets().add(new DocSet());
      config.getDocSets().add(new DocSet());
      marshaller.marshal(config, fliesProjectXml);
   }

   void readProject() throws JAXBException
   {
      FliesConfig config = (FliesConfig) unmarshaller.unmarshal(fliesProjectXml);
      // System.out.println(config);
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
      String user = config.getString("flies.user");
      System.out.println(user);
      boolean debug = config.getBoolean("flies.debug");
      System.out.println(debug);
   }

}
