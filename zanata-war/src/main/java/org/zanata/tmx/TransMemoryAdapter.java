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
    * Persists a translation unit when a tu
    * element is encountered while parsing.
    */
   public void persistTransUnit(TransMemory tm, Element tuElem)
   {
      TransMemoryUnit tu = new TransMemoryUnit();
      tu.setTranslationMemory(tm);

      TMXMetadataHelper.setMetadata(tu, tuElem, tm.getSourceLanguage());
      tu.setVersionNum(0);

      addTUVs(tu, tuElem.getChildElements("tuv"));

      tu.setUniqueId(determineUniqueId(tu));

      removeExistingTUIfAny(tm.getSlug(), tu.getUniqueId());
      entityManager.persist(tu);
      entityManager.flush();
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
         String srcLang = tu.getSourceLanguage();
         if (srcLang != null)
         {
            TransMemoryUnitVariant sourceVariant = tu.getTransUnitVariants().get(srcLang);
            if (sourceVariant == null)
            {
               throw new RuntimeException("Source variant cannot be determined for Translation unit with no tuid.");
            }
            return sourceVariant.getPlainTextSegmentHash();
         }
         else
         {
            throw new RuntimeException("Source language cannot be determined for Translation unit with no tuid. " +
                  "It must be defined either in the <tu> or the <header> element.");
         }
      }
   }

   private void addTUVs(TransMemoryUnit tu, Elements tuvElems)
   {
      for (int i = 0; i < tuvElems.size(); i++)
      {
         Element tuvElem = tuvElems.get(i);
         addVariant(tu, tuvElem);
      }
   }

   private void addVariant(TransMemoryUnit tu, Element tuvElem)
   {
      String taggedSegment = tuvElem.getFirstChildElement("seg").toXML();

      TransMemoryUnitVariant tuv = new TransMemoryUnitVariant();
      tuv.setTaggedSegment(taggedSegment);
      TMXMetadataHelper.setMetadata(tuv, tuvElem);
      String locale = validLocale(tuv.getLanguage());
      tu.getTransUnitVariants().put(locale, tuv);
   }

   private String validLocale(String language) throws IllegalArgumentException
   {
      // Throws IllegalArgumentException for illegal locale code
      return new LocaleId(language).getId();
   }

   private void removeExistingTUIfAny(String tmSlug, String uniqueId)
   {
      TransMemoryUnit existingTu = transMemoryDAO.findTranslationUnit(tmSlug, uniqueId);
      if( existingTu != null )
      {
         entityManager.remove(existingTu);
      }
   }

}
