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

import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import org.zanata.model.tm.TMTransUnitVariant;
import org.zanata.model.tm.TMTranslationUnit;
import org.zanata.model.tm.TransMemory;

import static org.zanata.tmx.TMXEvent.BodyEnd;
import static org.zanata.tmx.TMXEvent.BodyStart;
import static org.zanata.tmx.TMXEvent.HeaderEnd;
import static org.zanata.tmx.TMXEvent.HeaderStart;
import static org.zanata.tmx.TMXEvent.TMXEnd;
import static org.zanata.tmx.TMXEvent.TMXStart;
import static org.zanata.tmx.TMXEvent.TUEnd;
import static org.zanata.tmx.TMXEvent.TUStart;
import static org.zanata.tmx.TMXEvent.TUVStart;

/**
 * Parses TMX input.
 *
 * @author Carlos Munoz <a href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 */
public class TMXParser
{

   private XMLEventReader reader;

   public void parseTMX(InputStream input) throws XMLStreamException
   {
      XMLInputFactory factory = XMLInputFactory.newFactory().newInstance();
      factory.setProperty(XMLInputFactory.IS_VALIDATING, false);
      factory.setProperty(XMLInputFactory.SUPPORT_DTD, false);
      reader = factory.createXMLEventReader(input);

      advanceUntil(TMXStart);
      advanceUntil(HeaderStart);

      handleHeader();
      advanceUntil(BodyStart);
      handleBody();

      advanceUntil(TMXEnd);
      expectNext(TMXEnd);
      reader.close();
   }

   private void expectNext(TMXEvent ... expected) throws XMLStreamException
   {
      XMLEvent nextEvent = reader.peek();

      for( TMXEvent expectedEvent : expected )
      {
         if( expectedEvent.matches( nextEvent ) )
         {
            return;
         }
      }

      throw new RuntimeException("Did not find any of the expected elements: " + Arrays.toString(expected)
            + ". Found " + eventToString(nextEvent) + " instead.");
   }

   private static String eventToString(XMLEvent event)
   {
      if( event.isStartElement() )
      {
         return "Start Elem: " + event.asStartElement().getName().getLocalPart();
      }
      else if( event.isEndElement() )
      {
         return "End Elem: " + event.asStartElement().getName().getLocalPart();
      }
      else if( event.isAttribute() )
      {
         return "Attribute: " + event;
      }
      else if( event.isCharacters() )
      {
         return "Chars: " + event.asCharacters().getData();
      }
      else if( event.isEntityReference() )
      {
         return "Entity ref: " + event;
      }
      else if( event.isNamespace() )
      {
         return "Namespace: " + event;
      }
      else if( event.isProcessingInstruction() )
      {
         return "Processing Instr: " + event;
      }
      else if( event.isStartDocument() )
      {
         return "Start Doc: " + event;
      }
      else if( event.isEndDocument() )
      {
         return "End Doc: " + event;
      }
      else
      {
         return event.toString();
      }
   }

   private void advanceUntil(TMXEvent ... events) throws XMLStreamException
   {
      while(reader.hasNext())
      {
         XMLEvent nextEvent = reader.peek();

         for( TMXEvent e : events )
         {
            if( e.matches(nextEvent) )
            {
               return;
            }
         }

         reader.nextEvent();
      }
   }

   private Map<String, String> collectElementAttributes( StartElement startEvent, String ... attNames ) throws XMLStreamException
   {
      Map<String, String> collected = new HashMap<String, String>();
      Iterator it = startEvent.getAttributes();
      while( it.hasNext() )
      {
         for( String providedAttName : attNames )
         {
            Attribute nextAtt = (Attribute) it.next();
            String currAttName = nextAtt.getName().getLocalPart();
            if(currAttName.equals( providedAttName ))
            {
               collected.put(currAttName, nextAtt.getValue());
            }
         }
      }

      return collected;
   }

   private TMTransUnitVariant handleTransUnitVariant(XMLEvent event) throws XMLStreamException
   {
      expectNext(TUVStart);
      XMLEvent tuvEvent = reader.nextEvent();
      Map<String, String> tuvAttributes = collectElementAttributes(tuvEvent.asStartElement(), "name");


      String lang =  tuvAttributes.get("name");
      advanceUntil(TMXEvent.SEGStart);
      reader.nextEvent(); // Consume the start event
      String content = reader.nextEvent().asCharacters().getData();
      advanceUntil(TMXEvent.SEGEnd);
      advanceUntil(TMXEvent.TUVEnd);
      reader.nextEvent(); // Consume the end Event

      return new TMTransUnitVariant(lang, content);
   }

   private void handleTransUnit(XMLEvent event) throws XMLStreamException
   {
      expectNext(TUStart);
      XMLEvent tuEvent = reader.nextEvent();
      TMTranslationUnit tu = new TMTranslationUnit();
      advanceUntil(TUVStart, TUEnd);

      while( reader.hasNext() )
      {
         expectNext(TUVStart, TUEnd);
         XMLEvent nextEvent = reader.peek();

         // Found a TUV
         if( TUVStart.matches(nextEvent) )
         {
            TMTransUnitVariant tuv = handleTransUnitVariant(nextEvent);
            tu.getTransUnitVariants().put(tuv.getLanguage(), tuv);
         }
         // Reached the End of the TU
         else if( TUEnd.matches(nextEvent) )
         {
            break;
         }

         advanceUntil(TUVStart, TUEnd);
      }

      expectNext(TUEnd);
      reader.nextEvent(); // Consume the end Event

      // TODO Save the TU
   }

   private void handleBody() throws XMLStreamException
   {
      expectNext(BodyStart);
      reader.nextEvent(); // At the start of the body element
      advanceUntil(TUStart, BodyEnd);

      while(reader.hasNext())
      {
         expectNext(TUStart, BodyEnd);
         XMLEvent nextEvent = reader.peek();

         if( TUStart.matches(nextEvent) )
         {
            // TODO pass the trans unit name maybe
            handleTransUnit(nextEvent);
         }
         else if( BodyEnd.matches(nextEvent) )
         {
            break;
         }

         advanceUntil(TUStart, BodyEnd);
      }

      expectNext(BodyEnd);
      reader.nextEvent(); // Consume the end Event
   }

   private void handleHeader() throws XMLStreamException
   {
      expectNext(HeaderStart);

      TransMemory tm = new TransMemory();
      tm.setName("TEST");
      tm.setSlug("test-slug");
      // TODO Save TM

      advanceUntil(HeaderEnd);
      reader.nextEvent(); // Consume the header end event
   }
}
