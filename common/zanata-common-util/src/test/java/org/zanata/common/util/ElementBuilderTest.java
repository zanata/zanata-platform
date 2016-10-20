package org.zanata.common.util;

import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.events.XMLEvent;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

public class ElementBuilderTest {

    @Before
    public void before() throws IOException {
        is = getClass().getResourceAsStream("ElementBuilderTest.xml");
    }

    private XMLInputFactory xif = XMLInputFactory.newInstance();
    private InputStream is;

    @Test
    public void buildElementXMLStreamReader() throws Exception {
        XMLStreamReader reader = xif.createXMLStreamReader(is);
        while (reader.hasNext()) {
            int eventType = reader.next();
            if (eventType == START_ELEMENT
                    && reader.getLocalName().equals("tu")) {
                ElementBuilder.buildElement(reader).toXML();
            }
        }
    }

    @Test
    public void buildElementStartElementXMLEventReader() throws Exception {
        XMLEventReader reader = xif.createXMLEventReader(is);
        while (reader.hasNext()) {
            XMLEvent event = reader.nextEvent();
            if (event.isStartElement()
                    && event.asStartElement().getName().getLocalPart()
                            .equals("tu")) {
                ElementBuilder.buildElement(event.asStartElement(), reader)
                        .toXML();
            }
        }
    }

    @Test
    @Ignore
    public void buildElementXMLStreamReaderTransformer() throws Exception {
        // Nasty way to ensure that we get a Transformer which supports
        // StAXSource:
        System.setProperty("javax.xml.transform.TransformerFactory",
                "com.sun.org.apache.xalan.internal.xsltc.trax.TransformerFactoryImpl");
        Transformer t = TransformerFactory.newInstance().newTransformer();

        XMLStreamReader reader = xif.createXMLStreamReader(is);
        while (reader.hasNext()) {
            int eventType = reader.next();
            if (eventType == START_ELEMENT
                    && reader.getLocalName().equals("tu")) {
                ElementBuilder.buildElement(reader, t).toXML();
            }
        }
    }
}
