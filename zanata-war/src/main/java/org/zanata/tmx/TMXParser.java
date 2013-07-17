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
import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.zanata.common.util.ElementBuilder;
import org.zanata.model.tm.TransMemory;

import fj.Effect;
import nu.xom.Element;

/**
 * Parses TMX input.
 *
 * @author Carlos Munoz <a href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 */
@Name("tmxParser")
@AutoCreate
public class TMXParser
{
   @In
   private TransMemoryAdapter transMemoryAdapter;

   public void parseAndSaveTMX(InputStream input, final TransMemory tm) throws XMLStreamException
   {
      transMemoryAdapter.setTm(tm);
      parseTMX(input, transMemoryAdapter.getHeaderPersister(), transMemoryAdapter.getTransUnitPersister());
   }

   public void parseTMX(InputStream input, Effect<Element> headerHandler, Effect<Element> tuHandler) throws XMLStreamException
   {
      XMLInputFactory factory = XMLInputFactory.newInstance();
      factory.setProperty(XMLInputFactory.IS_VALIDATING, false);
      factory.setProperty(XMLInputFactory.SUPPORT_DTD, false);
      XMLEventReader reader = factory.createXMLEventReader(input);

      QName tu = new QName("tu");
      QName header = new QName("header");

      while (reader.hasNext())
      {
         XMLEvent event = reader.nextEvent();
         if (event.isStartElement())
         {
            StartElement startElem = event.asStartElement();
            if(startElem.getName().equals(header))
            {
               Element headerElem = ElementBuilder.buildElement(startElem, reader);
               headerHandler.e(headerElem);
            }
            else if (startElem.getName().equals(tu))
            {
               Element tuElem = ElementBuilder.buildElement(startElem, reader);
               tuHandler.e(tuElem);
            }
         }
      }
      reader.close();
   }

}
