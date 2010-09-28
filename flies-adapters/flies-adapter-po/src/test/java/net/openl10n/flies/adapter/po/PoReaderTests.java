package net.openl10n.flies.adapter.po;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import net.openl10n.flies.common.LocaleId;
import net.openl10n.flies.rest.dto.deprecated.Document;
import net.openl10n.flies.rest.dto.deprecated.TextFlowTargets;
import net.openl10n.flies.rest.dto.po.PoHeader;
import net.openl10n.flies.rest.dto.po.PotEntryData;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.xml.sax.InputSource;

public class PoReaderTests
{
   private static final Logger log = LoggerFactory.getLogger(PoReaderTests.class);

   File file;

   @BeforeMethod
   public void setup() throws IOException
   {
      file = File.createTempFile("poReaderTests", ".xml");
      System.out.println("creating file: " + file);
      if (file.exists())
         file.delete();
   }

   @Test
   public void extractTemplateThenAddATarget() throws IOException, JAXBException
   {

      Document doc = new Document("doc1", "mydoc.doc", "/", PoReader.PO_CONTENT_TYPE);

      InputSource inputSource = new InputSource(
      // new
      // File("/home/asgeirf/projects/gitsvn/Deployment_Guide/pt-BR/SELinux_Background.po").toURI().toString()
            "http://svn.fedorahosted.org/svn/Deployment_Guide/community/fc10/de-DE/Apache.po");
      inputSource.setEncoding("utf8");

      PoReader poReader = new PoReader();

      System.out.println("parsing template");
      poReader.extractTemplate(doc, inputSource);
      String[] locales = new String[] { "de-DE", "pt-BR", "fr-FR" };
      for (String locale : locales)
      {
         inputSource = new InputSource(
         // new File("/home/asgeirf/projects/gitsvn/Deployment_Guide/" + locale
         // + "/SELinux_Background.po").toURI().toString()
               "http://svn.fedorahosted.org/svn/Deployment_Guide/community/fc10/fr-FR/Apache.po");
         inputSource.setEncoding("utf8");
         System.out.println("extracting target: " + locale);
         poReader.extractTarget(doc, inputSource, new LocaleId(locale));
      }

      System.out.println("marshalling");
      JAXBContext jaxbContext = JAXBContext.newInstance(Document.class, PotEntryData.class, PoHeader.class, TextFlowTargets.class);
      Marshaller m = jaxbContext.createMarshaller();
      m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
      {
         StringWriter writer = new StringWriter();
         m.marshal(doc, writer);
         log.debug("{}", writer);
      }

      // FIXME add assertions!
   }
}
