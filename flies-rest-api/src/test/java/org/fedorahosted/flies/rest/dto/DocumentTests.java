package org.fedorahosted.flies.rest.dto;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;


import org.fedorahosted.flies.ContentType;
import org.fedorahosted.flies.LocaleId;
import org.fedorahosted.flies.rest.dto.Document;
import org.fedorahosted.flies.rest.dto.TextFlow;
import org.fedorahosted.flies.rest.dto.TextFlowTarget;
import org.fedorahosted.flies.rest.dto.TextFlowTargets;
import org.junit.Test;

public class DocumentTests {
	
	@Test
	public void createAFullDocument() throws JAXBException{
		Document doc = new Document("id","name", "/my/path", ContentType.TextPlain, 1, LocaleId.EN);
		
		TextFlow tf = new TextFlow("id");
		tf.setContent("hello world!");
		doc.getResources().add(tf);
		
		TextFlowTarget tft = new TextFlowTarget(tf);
		tft.setLang(LocaleId.EN_US);
		tf.addTarget(tft);
		
		doc.getExtensions().addAll(tf.getExtensions());
	
		JAXBContext jaxbContext = JAXBContext.newInstance(Document.class, TextFlowTargets.class);
		Marshaller m = jaxbContext.createMarshaller();
		m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
		m.marshal(doc, System.out);
	}
}
