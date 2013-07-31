package org.zanata.xml;

import java.io.IOException;
import java.io.OutputStream;

import nu.xom.Comment;
import nu.xom.DocType;
import nu.xom.Element;
import nu.xom.ProcessingInstruction;
import nu.xom.Serializer;
import nu.xom.Text;

/**
 * This class can build an XML stream from a series of XOM Elements.  Requires XOM 1.2 or later.
 * <p>
 * Sample usage:
 * <pre>
 * Element rootElement = new Element("root");
 * StreamSerializer serializer = new StreamSerializer(out);
 * serializer.setIndent(4);
 * serializer.writeXMLDeclaration();
 * serializer.writeStartTag(rootElement);
 * while(hasNextElement()) {
 *     serializer.write(nextElement());
 * }
 * serializer.writeEndTag(rootElement);
 * serializer.flush();
 * </pre>
 * From http://stackoverflow.com/a/1479881/14379
 * See also {@link nux.xom.io.StreamingSerializer}
 * @author Sean Flanigan <a href="mailto:sflaniga@redhat.com">sflaniga@redhat.com</a>
 *
 */
public class StreamSerializer extends Serializer
{

   public StreamSerializer(OutputStream out)
   {
      super(out);
   }

   public void newLine() throws IOException
   {
      super.breakLine();
   }

   @Override
   public void write(Element element) throws IOException
   {
      super.write(element);
   }

   @Override
   public void write(Comment c) throws IOException
   {
      super.write(c);
   }

   @Override
   public void write(DocType dt) throws IOException
   {
      super.write(dt);
   }

   @Override
   public void write(ProcessingInstruction pi) throws IOException
   {
      super.write(pi);
   }

   @Override
   public void write(Text t) throws IOException
   {
      super.write(t);
   }

   @Override
   public void writeXMLDeclaration() throws IOException
   {
      super.writeXMLDeclaration();
   }

   @Override
   public void writeEndTag(Element element) throws IOException
   {
      super.writeEndTag(element);
   }

   @Override
   public void writeStartTag(Element element) throws IOException
   {
      super.writeStartTag(element);
   }

}
