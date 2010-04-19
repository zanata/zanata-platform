package org.fedorahosted.flies.adapter.po;

import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import junit.framework.Assert;

import org.fedorahosted.flies.common.LocaleId;
import org.fedorahosted.flies.rest.dto.Document;
import org.fedorahosted.flies.rest.dto.TextFlow;
import org.fedorahosted.flies.rest.dto.TextFlowTargets;
import org.fedorahosted.flies.rest.dto.po.PoHeader;
import org.fedorahosted.flies.rest.dto.po.PotEntryData;
import org.junit.Test;
import org.xml.sax.InputSource;

public class PoReaderTest {
	
	LocaleId ja = new LocaleId("ja-JP");

	@Test
	public void extractTemplateThenAdd2Targets() throws IOException, JAXBException {
		String testDir = "src/test/resources/";

		Document doc = new Document("doc1", "doc1.pot", "/", PoReader.PO_CONTENT_TYPE);
		
		InputSource inputSource = new InputSource(
				new File(testDir, "pot/RPM.pot").toURI().toString()
		);
		inputSource.setEncoding("utf8");
		
		PoReader poReader = new PoReader();

		System.out.println("parsing template");
		poReader.extractTemplate(doc, inputSource);
		String [] locales = new String[]{"ja-JP"};
		for (String locale : locales){
			inputSource = new InputSource(
					new File(testDir, locale+"/RPM.po").toURI().toString()
			);
			inputSource.setEncoding("utf8");
			System.out.println("extracting target: " + locale);
			poReader.extractTarget(doc, inputSource, new LocaleId(locale));
		}
		
		System.out.println("marshalling");
		JAXBContext jaxbContext = JAXBContext.newInstance(Document.class, PotEntryData.class, PoHeader.class, TextFlowTargets.class);
		Marshaller m = jaxbContext.createMarshaller();
		m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
		m.marshal(doc, System.out);
		
		List<TextFlow> resources = doc.getResources();
		
		TextFlow tf1 = (TextFlow) resources.get(3);
		Assert.assertEquals("Important", tf1.getContent());
		Assert.assertEquals("キーのインポート", tf1.getTarget(ja).getContent());
		
		// TODO test PO headers and attributes
	}
}
