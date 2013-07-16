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

import nu.xom.Element;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.zanata.common.util.ElementBuilder;
import org.zanata.dao.TransMemoryDAO;
import org.zanata.model.tm.TransMemory;

import fj.Effect;

/**
 * Parses TMX input.
 *
 * @author Carlos Munoz <a href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 */
@Name("tmxParser")
public class TMXParser
{
   @In
   private TransMemoryDAO transMemoryDAO;

   public TMXParser()
   {
   }

   public TMXParser(TransMemoryDAO transMemoryDAO)
   {
      this.transMemoryDAO = transMemoryDAO;
   }

   public void parseTMX(InputStream input) throws XMLStreamException
   {
      final TransMemory tm = new TransMemory();
      tm.setSlug("test-slug");
      // TODO Save TM
//      transMemoryDAO.makePersistent(tm);

      parseTMX(input, new TransUnitPersister(tm));
   }

   public void parseTMX(InputStream input, Effect<Element> sideEffect) throws XMLStreamException
   {
      XMLInputFactory factory = XMLInputFactory.newInstance();
      factory.setProperty(XMLInputFactory.IS_VALIDATING, false);
      factory.setProperty(XMLInputFactory.SUPPORT_DTD, false);
      XMLEventReader reader = factory.createXMLEventReader(input);

      QName tu = new QName("tu");

      while (reader.hasNext())
      {
         XMLEvent event = reader.nextEvent();
         if (event.isStartElement())
         {
            StartElement startElem = event.asStartElement();
            if (startElem.getName().equals(tu))
            {
               Element tuElem = ElementBuilder.buildElement(startElem, reader);
               sideEffect.e(tuElem);
            }
         }
      }
      reader.close();
   }

}
