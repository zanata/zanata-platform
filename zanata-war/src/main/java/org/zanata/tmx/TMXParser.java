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

import static org.zanata.tmx.TMXAttribute.*;

import java.io.InputStream;
import java.util.Date;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import lombok.Setter;
import nu.xom.Attribute;
import nu.xom.Element;

import org.codehaus.jackson.map.ObjectMapper;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.zanata.common.util.ElementBuilder;
import org.zanata.model.tm.TMMetadataHelper;
import org.zanata.model.tm.TMTransUnitVariant;
import org.zanata.model.tm.TMTranslationUnit;
import org.zanata.model.tm.TransMemory;
import org.zanata.model.tm.TMMetadataType;

import com.google.common.collect.Maps;

import fj.Effect;
import nu.xom.Elements;

/**
 * Parses TMX input.
 *
 * @author Carlos Munoz <a href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 */
@Name("tmxParser")
@Scope(ScopeType.STATELESS)
@AutoCreate
public class TMXParser
{
   private static final DateTimeFormatter ISO8601Z = DateTimeFormat.forPattern("yyyyMMdd'T'HHmmss'Z").withZoneUTC();

   @In
   @Setter
   private EntityManager entityManager;

   public void parseAndSaveTMX(InputStream input, final TransMemory tm) throws XMLStreamException
   {
      // TODO Save TM
      parseTMX(input, new Effect<Element>()
      {
         @Override
         public void e(Element tuElem)
         {
            parseAndSaveTransUnit(tuElem, tm);
         }
      });
   }

   private Date parseDate(String dateString)
   {
      return ISO8601Z.parseDateTime(dateString).toDate();
   }

   private void parseAndSaveTransUnit(Element tuElem, TransMemory tm)
   {
      TMTranslationUnit tu = new TMTranslationUnit();
      String creationDate = creationdate.getAttribute(tuElem);
      if (creationDate != null)
      {
         tu.setCreationDate(parseDate(creationDate));
      }
      String changeDate = changedate.getAttribute(tuElem);
      if (changeDate != null)
      {
         tu.setLastChanged(parseDate(changeDate));
      }
      tu.setSourceLanguage(srclang.getAttribute(tuElem));
      tu.setTranslationMemory(tm);
      tu.setTransUnitId(tuid.getAttribute(tuElem));

      Map<String, String> metadata = Maps.newHashMap();
      for (int i=0; i < tuElem.getAttributeCount(); i++)
      {
         Attribute attr = tuElem.getAttribute(i);
         String name = attr.getQualifiedName();
         if (!TMXAttribute.contains(name))
         {
            String value = attr.getValue();
            metadata.put(name, value);
         }
      }
      TMMetadataHelper.setTMXMetadata(tu, metadata);

      tu.setVersionNum(0);

      // Parse the tuv's
      Elements tuvElems = tuElem.getChildElements("tuv");
      for( int i=0; i<tuvElems.size(); i++ )
      {
         Element tuvElem = tuvElems.get(i);
         parseAndSaveTransUnitVariant(tuvElem, tu);
      }

      entityManager.persist(tu);
   }

   private void parseAndSaveTransUnitVariant(Element tuvElem, TMTranslationUnit tu)
   {
      String language = tuvElem.getAttributeValue("lang", XMLConstants.XML_NS_URI);
      String content = tuvElem.getFirstChildElement("seg").getChild(0).toXML();

      TMTransUnitVariant tuv = new TMTransUnitVariant(language, content);
      //TODO save metadata
      tu.getTransUnitVariants().put(language, tuv);
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
