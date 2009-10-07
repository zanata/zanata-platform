package org.fedorahosted.flies.adapter.properties;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;

import org.fedorahosted.flies.ContentType;
import org.fedorahosted.flies.LocaleId;
import org.fedorahosted.flies.rest.dto.Document;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.xml.sax.InputSource;

public class PropReaderTests {

	@BeforeClass
	public static void setupContainer() {
		// ProjectPackage.registerPartFactory(PoHeaderPart.FACTORY);
		// ProjectPackage.registerPartFactory(PotHeaderPart.FACTORY);
		// ProjectPackage.registerPartFactory(PotEntriesDataPart.FACTORY);
	}

	@Before
	public void setup() throws IOException {
	}

	@Test
	public void extractTemplateThenAddATarget() throws Exception {
		Document doc = new Document("test.properties", ContentType.TextPlain);
		InputStream testStream = getResourceAsStream("test.properties");

		PropReader PropReader = new PropReader();

		PropReader.extractTemplate(doc, new InputSource(testStream));
		String[] locales = new String[] { "fr" };
		for (String locale : locales) {
			InputStream testFRStream = getResourceAsStream("test_fr.properties");
			PropReader.extractTarget(doc, new InputSource(testFRStream),
					new LocaleId(locale));
		}
		JAXBContext jc = JAXBContext.newInstance(Document.class.getPackage()
				.getName());
		// JAXBContext jc = JAXBContext.newInstance(Document.class,
		// TextFlowTargets.class);
		Marshaller m = jc.createMarshaller();
		m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
		m.marshal(doc, System.out);
	}

	private InputStream getResourceAsStream(String relativeResourceName)
			throws FileNotFoundException {
		InputStream stream = PropReaderTests.class
				.getResourceAsStream(relativeResourceName);
		if (stream == null)
			throw new FileNotFoundException(relativeResourceName);
		return stream;
	}
}
