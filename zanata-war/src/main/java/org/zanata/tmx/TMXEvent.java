/*
 * Copyright 2010, Red Hat, Inc. and individual contributors as indicated by the
 * @author tags. See the copyright.txt file in the distribution for a full
 * listing of individual contributors.
 * 
 * This is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 * 
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this software; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
 * site: http://www.fsf.org.
 */
package org.zanata.tmx;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import static javax.xml.stream.XMLStreamConstants.ATTRIBUTE;
import static javax.xml.stream.XMLStreamConstants.END_ELEMENT;
import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;

/**
 * Utility class to be able to match specific TMX events from the parser.
 *
 * @author Carlos Munoz <a href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 */
@EqualsAndHashCode(of = {"name", "eventType"})
@ToString(of = {"name", "eventType"})
@AllArgsConstructor(access = AccessLevel.PRIVATE)
final class TMXEvent
{
   public static final TMXEvent TMXStart = new TMXEvent(START_ELEMENT, "tmx");
   public static final TMXEvent TMXEnd = new TMXEvent(END_ELEMENT, "tmx");
   public static final TMXEvent HeaderStart = new TMXEvent(START_ELEMENT, "header");
   public static final TMXEvent HeaderEnd = new TMXEvent(END_ELEMENT, "header");
   public static final TMXEvent BodyStart = new TMXEvent(START_ELEMENT, "body");
   public static final TMXEvent BodyEnd = new TMXEvent(END_ELEMENT, "body");
   public static final TMXEvent TUStart = new TMXEvent(START_ELEMENT, "tu");
   public static final TMXEvent TUEnd = new TMXEvent(END_ELEMENT, "tu");
   public static final TMXEvent TUVStart = new TMXEvent(START_ELEMENT, "tuv");
   public static final TMXEvent TUVEnd = new TMXEvent(END_ELEMENT, "tuv");
   public static final TMXEvent SEGStart = new TMXEvent(START_ELEMENT, "seg");
   public static final TMXEvent SEGEnd = new TMXEvent(END_ELEMENT, "seg");

   @Getter
   private int eventType; // Must match events in XmlStreamConstants

   @Getter
   private String name;

   public boolean matches(XMLEvent xmlEvent)
   {
      int xmlEventType = xmlEvent.getEventType();

      if( eventType != xmlEventType )
      {
         return false;
      }

      if( xmlEvent.isStartElement() )
      {
         StartElement startEvent = xmlEvent.asStartElement();
         return name.equals(startEvent.getName().getLocalPart());
      }
      else if( xmlEvent.isEndElement() )
      {
         EndElement endEvent = xmlEvent.asEndElement();
         return name.equals( endEvent.getName().getLocalPart() );
      }

      return false;
   }
}
