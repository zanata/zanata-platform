package org.fedorahosted.flies.adapter.po;

import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.xml.bind.JAXBException;

import junit.framework.Assert;

import org.fedorahosted.flies.common.LocaleId;
import org.fedorahosted.flies.rest.dto.Document;
import org.fedorahosted.flies.rest.dto.DocumentResource;
import org.fedorahosted.flies.rest.dto.TextFlow;
import org.junit.Test;
import org.xml.sax.InputSource;

public class PoReaderTest {
	
	LocaleId de = new LocaleId("de");
	LocaleId fr = new LocaleId("fr");

	@Test
	public void extractTemplateThenAdd2Targets() throws IOException, JAXBException {
		String testDir = "src/test/resources/extractTemplateThenAdd2Targets";

		Document doc = new Document("doc1", "doc1.pot", "/", PoReader.PO_CONTENT_TYPE);
		
		InputSource inputSource = new InputSource(
				new File(testDir, "pot/file.pot").toURI().toString()
		);
		inputSource.setEncoding("utf8");
		
		PoReader poReader = new PoReader();

		System.out.println("parsing template");
		poReader.extractTemplate(doc, inputSource);
		String [] locales = new String[]{"de", "fr"};
		for (String locale : locales){
			inputSource = new InputSource(
					new File(testDir, locale+"/file.po").toURI().toString()
			);
			inputSource.setEncoding("utf8");
			System.out.println("extracting target: " + locale);
			poReader.extractTarget(doc, inputSource, new LocaleId(locale));
		}
		
//		System.out.println("marshalling");
//		JAXBContext jaxbContext = JAXBContext.newInstance(Document.class, PotEntryData.class, PoHeader.class, TextFlowTargets.class);
//		Marshaller m = jaxbContext.createMarshaller();
//		m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
//		m.marshal(doc, System.out);
		
		List<DocumentResource> resources = doc.getResources();
		
		TextFlow tf1 = (TextFlow) resources.get(0);
		Assert.assertEquals("a", tf1.getContent());
		Assert.assertEquals("one", tf1.getTarget(de).getContent());
		Assert.assertEquals("1", tf1.getTarget(fr).getContent());
		
		TextFlow tf2 = (TextFlow) resources.get(1);
		Assert.assertEquals("b", tf2.getContent());
		Assert.assertEquals("two", tf2.getTarget(de).getContent());
		Assert.assertNull(tf2.getTarget(fr));

		TextFlow tf3 = (TextFlow) resources.get(2);
		Assert.assertEquals("c", tf3.getContent());
		Assert.assertNull(tf3.getTarget(de));
		Assert.assertNull(tf3.getTarget(fr));
		
		// TODO test PO headers and attributes
	}
}
