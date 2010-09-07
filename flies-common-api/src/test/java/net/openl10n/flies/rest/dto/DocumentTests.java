package net.openl10n.flies.rest.dto;

import java.net.URI;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import net.openl10n.flies.common.ContentType;
import net.openl10n.flies.common.LocaleId;
import net.openl10n.flies.rest.dto.Link;
import net.openl10n.flies.rest.dto.deprecated.Document;
import net.openl10n.flies.rest.dto.deprecated.TextFlow;
import net.openl10n.flies.rest.dto.deprecated.TextFlowTarget;
import net.openl10n.flies.rest.dto.deprecated.TextFlowTargets;

import org.junit.Test;

public class DocumentTests
{

   @Test
   public void createAFullDocument() throws JAXBException
   {
      Document doc = new Document("/my/path/document.txt", ContentType.TextPlain);
      doc.getLinks().add(new Link(URI.create("http://example.com")));
      TextFlow tf = new TextFlow("id");
      tf.setContent("hello world!");
      doc.getTextFlows().add(tf);

      TextFlowTarget tft = new TextFlowTarget(tf);
      tft.setLang(LocaleId.EN_US);
      tf.addTarget(tft);

      doc.getExtensions(true).addAll(tf.getExtensions());

      JAXBContext jaxbContext = JAXBContext.newInstance(Document.class, TextFlowTargets.class);
      Marshaller m = jaxbContext.createMarshaller();
      m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
      m.marshal(doc, System.out);
   }
}
