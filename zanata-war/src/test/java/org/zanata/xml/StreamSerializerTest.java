package org.zanata.xml;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.startsWith;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import javax.xml.XMLConstants;

import nu.xom.Attribute;
import nu.xom.Element;

import org.testng.annotations.Test;
import org.zanata.xml.StreamSerializer;

public class StreamSerializerTest {

    @Test
    public void sanityTest() throws IOException {
        OutputStream out = new ByteArrayOutputStream();
        StreamSerializer serializer = new StreamSerializer(out);

        serializer.writeXMLDeclaration();
        Element rootElement = new Element("root");
        rootElement.addAttribute(new Attribute("rootAttr", "rootAttrVal"));
        serializer.writeStartTag(rootElement);

        Element sub1 = new Element("sub");
        sub1.addAttribute(new Attribute("subAttr", "subAttrVal"));
        serializer.write(sub1);

        Element sub2 = new Element("sub");
        sub2.addAttribute(new Attribute("xml:lang", XMLConstants.XML_NS_URI,
                "en"));
        serializer.write(sub2);

        serializer.writeEndTag(rootElement);
        serializer.flush();
        String output = out.toString();

        assertThat(output,
                startsWith("<?xml version=\"1.0\" encoding=\"UTF-8\"?>"));
        String expected =
                "<root rootAttr=\"rootAttrVal\">"
                        + "<sub subAttr=\"subAttrVal\"/>"
                        + "<sub xml:lang=\"en\"/>" + "</root>";
        assertThat(output, containsString(expected));
    }
}
