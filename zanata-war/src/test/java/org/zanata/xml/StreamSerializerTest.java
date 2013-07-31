package org.zanata.xml;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

import nu.xom.Element;

import org.hamcrest.text.StringContainsInOrder;
import org.testng.annotations.Test;
import org.zanata.xml.StreamSerializer;

public class StreamSerializerTest
{

   @Test
   public void streamSerializer() throws IOException
   {
      Element rootElement = new Element("root");
      OutputStream out = new ByteArrayOutputStream();
      StreamSerializer serializer = new StreamSerializer(out);
      serializer.writeXMLDeclaration();
      serializer.writeStartTag(rootElement);

      serializer.write(new Element("sub"));
      serializer.write(new Element("sub"));
      serializer.write(new Element("sub"));

      serializer.writeEndTag(rootElement);
      serializer.flush();
      String output = out.toString();

      assertThat(output, containsString("<root><sub/><sub/><sub/></root>"));
   }
}
