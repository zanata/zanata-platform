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

package org.zanata.tmx;

import java.util.Map;

import javax.persistence.EntityManager;
import javax.xml.XMLConstants;

import lombok.NoArgsConstructor;
import lombok.Setter;
import nu.xom.Attribute;
import nu.xom.Element;

import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.zanata.model.tm.TMMetadataType;
import org.zanata.model.tm.TMTransUnitVariant;
import org.zanata.model.tm.TMXMetadataHelper;
import org.zanata.model.tm.TMTranslationUnit;
import org.zanata.model.tm.TransMemory;

import com.google.common.collect.Maps;

import fj.Effect;
import nu.xom.Elements;
import nu.xom.Node;

/**
 * Translation Memory Adapter for the TMX parser. Provides callback effects (functions) to be used when
 * the parser encounters certain specific events.
 *
 * @author Sean Flanigan <a href="mailto:sflaniga@redhat.com">sflaniga@redhat.com</a>
 *
 */
@Name("transMemoryAdapter")
@AutoCreate
@NoArgsConstructor
class TransMemoryAdapter
{
   @In
   private EntityManager entityManager;

   @Setter
   private TransMemory tm;

   TransMemoryAdapter(TransMemory tm)
   {
      this.tm = tm;
   }

   /**
    * Returns an effect function that persists the header elements when encountered while parsing.
    * This modifies the translation memory fields and metadata.
    */
   public Effect<Element> getHeaderPersister()
   {
      return new Effect<Element>() {
         @Override
         public void e(Element headerElem)
         {
            Map<String, String> metadata = Maps.newHashMap();
            for (int i=0; i < headerElem.getAttributeCount(); i++)
            {
               Attribute attr = headerElem.getAttribute(i);
               String name = attr.getQualifiedName();
               String value = attr.getValue();
               metadata.put(name, value);
            }
            // Header might also have sub nodes (save them as pure xml)
            StringBuilder childrenContent = new StringBuilder();
            for (int i=0; i < headerElem.getChildCount(); i++)
            {
               Node child = headerElem.getChild(i);
               childrenContent.append(child.toXML());
            }
            metadata.put(TMXMetadataHelper.TMX_HEADER_CONTENT, childrenContent.toString());

            TMXMetadataHelper.setMetadata(tm, metadata);
            entityManager.merge(tm);
         }
      };
   }

   /**
    * Returns an effect function that persists a translation unit when a <tu> element
    * is encountered while parsing.
    */
   public Effect<Element> getTransUnitPersister()
   {
      return new Effect<Element>() {
         @Override
         public void e(Element tuElem)
         {
            TMTranslationUnit tu = new TMTranslationUnit();
            tu.setTranslationMemory(tm);

            Map<String, String> metadata = Maps.newHashMap();
            for (int i=0; i < tuElem.getAttributeCount(); i++)
            {
               Attribute attr = tuElem.getAttribute(i);
               String name = attr.getQualifiedName();
               String value = attr.getValue();
               metadata.put(name, value);
            }
            TMXMetadataHelper.setMetadata(tu, metadata);
            tu.setVersionNum(0);

            // Parse the tuv's
            Elements tuvElems = tuElem.getChildElements("tuv");
            for( int i=0; i<tuvElems.size(); i++ )
            {
               Element tuvElem = tuvElems.get(i);
               parseAndSaveTransUnitVariant(tuvElem, tu);
            }

            tu.setUniqueId( determineUniqueId(tu) );
            entityManager.persist(tu);
         }
      };
   }

   private void parseAndSaveTransUnitVariant(Element tuvElem, TMTranslationUnit tu)
   {
      String language = tuvElem.getAttributeValue("lang", XMLConstants.XML_NS_URI);
      String content = tuvElem.getFirstChildElement("seg").getChild(0).toXML();

      TMTransUnitVariant tuv = new TMTransUnitVariant(language, content);
      //TODO save metadata
      tu.getTransUnitVariants().put(language, tuv);
   }

   private String determineUniqueId( TMTranslationUnit tu )
   {
      if( tu.getTransUnitId() != null )
      {
         // tuid is the natural id by default
         return tu.getTransUnitId();
      }
      else
      {
         // Go looking for a source content hash
         String srcLang = tu.getSourceLanguage() != null ? tu.getSourceLanguage() : tu.getTranslationMemory().getSourceLanguage();
         if(srcLang != null)
         {
            // source lang is *all*
            if( srcLang.equalsIgnoreCase("*all*") )
            {
               srcLang = tu.getSourceLanguage();
               if(srcLang == null || srcLang.equalsIgnoreCase("*all*"))
               {
                  throw new RuntimeException("Source language cannot be determined for Translation unit. " +
                        "It must be defined either in the <tu> or the <header> element.");
               }
            }

            TMTransUnitVariant sourceVariant = tu.getTransUnitVariants().get(srcLang);
            if( sourceVariant == null )
            {
               throw new RuntimeException("Source variant cannot be determined for Translation unit with no tuid.");
            }

            return sourceVariant.getPlainTextSegmentHash();
         }
         else
         {
            throw new RuntimeException("Source language cannot be determined for Translation unit with no tuid.");
         }
      }
   }


}
