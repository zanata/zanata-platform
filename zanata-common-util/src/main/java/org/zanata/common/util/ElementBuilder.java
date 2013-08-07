/*
 * Copyright 2013, Red Hat, Inc. and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.zanata.common.util;

import static javax.xml.stream.XMLStreamConstants.*;

import java.util.Iterator;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.Characters;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.stax.StAXSource;

import nu.xom.Attribute.Type;
import nu.xom.Element;
import nu.xom.converters.DOMConverter;

import org.w3c.dom.Document;

/**
 * ElementBuilder consumes a complete element from a StAX XMLStreamReader
 * or XMLEventReader and returns it as a xom Element.
 * <p>
 * {@link nux.xom.io.StaxParser} implements a similar idea.
 *
 * @author Sean Flanigan <a href="mailto:sflaniga@redhat.com">sflaniga@redhat.com</a>
 */
public class ElementBuilder
{

   /**
    * Converts the element and subelements at the reader's current position into a xom Element tree
    * @param reader must be in START_ELEMENT state
    * @return
    * @throws XMLStreamException
    */
   public static Element buildElement(XMLStreamReader reader) throws XMLStreamException
   {
      int eventType = reader.getEventType();
      assert eventType == START_ELEMENT;
      String localName = reader.getLocalName();
      String qualName = getName(reader.getPrefix(), localName);
      Element xElem = new Element(qualName, reader.getNamespaceURI());
      copyAttributes(reader, xElem);

      while (reader.hasNext() && eventType != END_ELEMENT)
      {
         eventType = reader.next();
         switch (eventType)
         {
         case START_ELEMENT:
            Element startElement = buildElement(reader);
            xElem.appendChild(startElement);
            break;
         case END_ELEMENT:
            break;
         case CHARACTERS:
            xElem.appendChild(reader.getText());
            break;
         case SPACE:
            break;
         default:
            throw new RuntimeException("unhandled event type: "+eventType);
         }
      }
      return xElem;
   }

   /**
    * Converts the element and subsequent subelements at the reader's current position into a xom Element tree
    * @param startElem
    * @param reader must be just past the START_ELEMENT event
    * @return
    * @throws XMLStreamException
    */
   public static Element buildElement(StartElement startElem, XMLEventReader reader) throws XMLStreamException
   {
      QName name = startElem.getName();
      String qualName = getName(name);
      Element xElem = new Element(qualName, name.getNamespaceURI());
      copyAttributes(startElem, xElem);

      while (reader.hasNext() && !reader.peek().isEndElement())
      {
         XMLEvent event = reader.nextEvent();
         if (event.isStartElement())
         {
            Element startElement = buildElement(event.asStartElement(), reader);
            xElem.appendChild(startElement);
         }
         else if (event.isCharacters())
         {
            Characters characters = event.asCharacters();
            if (!characters.isIgnorableWhiteSpace())
            {
               xElem.appendChild(characters.getData());
            }
         }
         else
         {
            throw new RuntimeException("unhandled event type: "+event.getEventType());
         }
      }
      if (reader.hasNext()) reader.nextEvent();
      return xElem;
   }

   public static void copyAttributes(XMLStreamReader reader, Element toElem)
   {
      assert reader.getEventType() == START_ELEMENT;
      for (int i=0; i < reader.getAttributeCount(); i++)
      {
         String prefix = reader.getAttributePrefix(i);
         String localName = reader.getAttributeLocalName(i);
         String name = getName(prefix, localName);
         String uri = reader.getAttributeNamespace(i);
         String value = reader.getAttributeValue(i);
         Type attrType = getAttributeType(reader.getAttributeType(i));
         nu.xom.Attribute xAttr = new nu.xom.Attribute(
               name, uri, value, attrType);
         toElem.addAttribute(xAttr);
      }
   }

   public static void copyAttributes(StartElement fromElem, Element toElem)
   {
      Iterator<Attribute> attributes = fromElem.getAttributes();
      while (attributes.hasNext())
      {
         Attribute attr = attributes.next();
         QName qName = attr.getName();
         String name = getName(qName);
         String uri = qName.getNamespaceURI();
         String value = attr.getValue();
         Type attrType = getAttributeType(attr.getDTDType());
         nu.xom.Attribute xAttr = new nu.xom.Attribute(
               name, uri, value, attrType);
         toElem.addAttribute(xAttr);
      }
   }

   private static String getName(String prefix, String localName)
   {
      String xPrefix = prefix.isEmpty() ? "" : prefix + ":";
      String prefixedName = xPrefix + localName;
      return prefixedName;
   }

   private static String getName(QName name)
   {
      String prefix = name.getPrefix();
      String xPrefix = prefix.isEmpty() ? "" : prefix + ":";
      String prefixedName = xPrefix + name.getLocalPart();
      return prefixedName;
   }

   private static Type getAttributeType(String name)
   {
      try
      {
         if (name.equals("ENUMERATED")) return Type.ENUMERATION;
         return (Type) Type.class.getField(name).get(null);
      }
      catch (NoSuchFieldException e)
      {
         throw new RuntimeException("unknown DTD type: "+name);
      }
      catch (IllegalAccessException e)
      {
         throw new RuntimeException(e);
      }
   }

   /**
    * Converts the element and subelements at the reader's current position into a xom Element tree
    * @param reader
    * @param transformer the provided Transformer must support StAXSource
    * @return
    * @throws TransformerException
    */
   static nu.xom.Element buildElement(XMLStreamReader reader, Transformer transformer) throws TransformerException
   {
      DOMResult result = new DOMResult();
      transformer.transform(new StAXSource(reader), result);
      Document docNode = (Document) result.getNode();
      org.w3c.dom.Element elem = (org.w3c.dom.Element) docNode.getFirstChild();
      nu.xom.Element element = DOMConverter.convert(elem);
      return element;
   }

}
