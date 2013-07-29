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

import javax.persistence.EntityManager;
import javax.xml.XMLConstants;

import lombok.NoArgsConstructor;
import nu.xom.Element;
import nu.xom.Elements;

import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.zanata.common.LocaleId;
import org.zanata.dao.TransMemoryDAO;
import org.zanata.model.tm.TransMemoryUnitVariant;
import org.zanata.model.tm.TransMemoryUnit;
import org.zanata.model.tm.TMXMetadataHelper;
import org.zanata.model.tm.TransMemory;

/**
 * Translation Memory Adapter for the TMX parser. Provides callback effects
 * (functions) to be used when the parser encounters certain specific events.
 * 
 * @author Sean Flanigan <a
 *         href="mailto:sflaniga@redhat.com">sflaniga@redhat.com</a>
 * 
 */
@Name("transMemoryAdapter")
@AutoCreate
@NoArgsConstructor
public class TransMemoryAdapter
{
   @In
   private EntityManager entityManager;

   @In
   private TransMemoryDAO transMemoryDAO;

   /**
    * Persists the header elements when
    * encountered while parsing. This modifies the translation memory fields and
    * metadata.
    */
   public void persistHeader(TransMemory tm, Element headerElem)
   {
      TMXMetadataHelper.setMetadata(tm, headerElem);
      entityManager.merge(tm);
   }

   /**
    * Persists a translation unit when a <tu>
    * element is encountered while parsing.
    */
   public void persistTransUnit(TransMemory tm, Element tuElem)
   {
      TransMemoryUnit tu = new TransMemoryUnit();
      tu.setTranslationMemory(tm);

      TMXMetadataHelper.setMetadata(tu, tuElem);
      tu.setVersionNum(0);

      // Parse the tuvs
      Elements tuvElems = tuElem.getChildElements("tuv");
      for (int i = 0; i < tuvElems.size(); i++)
      {
         Element tuvElem = tuvElems.get(i);
         addVariant(tu, tuvElem);
      }

      tu.setUniqueId(determineUniqueId(tu));

      // Find if there is already a tu and replace it
      TransMemoryUnit existingTu = transMemoryDAO.findTranslationUnit(tm.getSlug(), tu.getUniqueId());
      if( existingTu != null )
      {
         entityManager.remove(existingTu);
      }
      entityManager.persist(tu);
      entityManager.flush();
   }

   private void addVariant(TransMemoryUnit tu, Element tuvElem)
   {
      String language = tuvElem.getAttributeValue("lang", XMLConstants.XML_NS_URI);
      String content = tuvElem.getFirstChildElement("seg").toXML();

      TransMemoryUnitVariant tuv = new TransMemoryUnitVariant(language, content);
      TMXMetadataHelper.setMetadata(tuv, tuvElem);

      String locale = new LocaleId(language).getId(); // This will fail if the locale is not accepted
      tu.getTransUnitVariants().put(locale, tuv);
   }

   private String determineUniqueId(TransMemoryUnit tu)
   {
      if (tu.getTransUnitId() != null)
      {
         // tuid is the natural id by default
         return tu.getTransUnitId();
      }
      else
      {
         // Go looking for a source content hash
         String srcLang = tu.getSourceLanguage() != null ? tu.getSourceLanguage() : tu.getTranslationMemory().getSourceLanguage();
         if (srcLang != null)
         {
            if (srcLang.equalsIgnoreCase("*all*"))
            {
               srcLang = tu.getSourceLanguage();
               if (srcLang == null || srcLang.equalsIgnoreCase("*all*"))
               {
                  throw new RuntimeException("Source language cannot be determined for Translation unit. "
                        + "It must be defined either in the <tu> or the <header> element.");
               }
            }

            TransMemoryUnitVariant sourceVariant = tu.getTransUnitVariants().get(srcLang);
            if (sourceVariant == null)
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
