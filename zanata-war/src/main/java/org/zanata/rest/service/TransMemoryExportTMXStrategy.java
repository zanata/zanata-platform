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

package org.zanata.rest.service;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.xml.XMLConstants;

import lombok.extern.slf4j.Slf4j;

import nu.xom.Attribute;
import nu.xom.Element;

import org.zanata.common.LocaleId;
import org.zanata.model.tm.TMXMetadataHelper;
import org.zanata.model.tm.TransMemory;
import org.zanata.model.tm.TransMemoryUnitVariant;
import org.zanata.model.tm.TransMemoryUnit;
import org.zanata.util.TMXUtils;
import org.zanata.util.VersionUtility;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;

/**
 * Writes one or more variations for a single TransMemoryUnit as a TMX translation unit.
 * @author Sean Flanigan <a href="mailto:sflaniga@redhat.com">sflaniga@redhat.com</a>
 *
 */
@ParametersAreNonnullByDefault
@Slf4j
public class TransMemoryExportTMXStrategy implements ExportTMXStrategy<TransMemoryUnit>
{
   private static final String creationTool = "Zanata " + TransMemoryExportTMXStrategy.class.getSimpleName();
   private static final String creationToolVersion =
         VersionUtility.getVersionInfo(TransMemoryExportTMXStrategy.class).getVersionNo();
   private TransMemory tm;

   public TransMemoryExportTMXStrategy(TransMemory tm)
   {
      super();
      this.tm = tm;
   }

   @Override
   public Element buildHeader() throws IOException
   {
      Element header = new Element("header");
      addAttributesAndChildren(header, tm);
      header.addAttribute(new Attribute("creationtool", creationTool));
      header.addAttribute(new Attribute("creationtoolversion", creationToolVersion));
      return header;
   }

   /**
    * Writes the specified SourceContents (TextFlow) and one or all of its translations to the TMXWriter.
    * @param tu the SourceContents (TextFlow) whose contents and translations are to be exported
    * @param tuidPrefix String to be prepended to all resIds when generating tuids
    * @return 
    * @throws IOException 
    * @throws Exception 
    */
   @Override
   public Element buildTU(TransMemoryUnit tu) throws IOException
   {
      Element textUnit = new Element("tu");

      Optional<LocaleId> sourceLocaleId = getSourceLocale(tu);
      String srcLang = sourceLocaleId.isPresent() ?
            sourceLocaleId.get().getId() : TMXUtils.ALL_LOCALE;
      textUnit.addAttribute(new Attribute(TMXUtils.SRCLANG, srcLang));

      String tuid = tu.getTransUnitId();
      if (tuid != null)
      {
         textUnit.addAttribute(new Attribute("tuid", tuid));
      }

      addAttributesAndChildren(textUnit, tu);

      for (TransMemoryUnitVariant tuv: tu.getTransUnitVariants().values())
      {
         textUnit.appendChild(buildTUV(tuv));
      }
      return textUnit;
   }

   private Optional<LocaleId> getSourceLocale(TransMemoryUnit tu)
   {
      String tuSourceLanguage = tu.getSourceLanguage();
      if (tuSourceLanguage != null)
      {
         return Optional.of(new LocaleId(tuSourceLanguage));
      }
      return Optional.absent();
   }

   private void addAttributesAndChildren(Element header, TransMemory transMemory)
   {
      ImmutableMap<String,String> attributes = TMXMetadataHelper.getAttributes(transMemory);
      for (Map.Entry<String, String> attr : attributes.entrySet())
      {
         header.addAttribute(toAttribute(attr));
      }
      for (Element child : TMXMetadataHelper.getChildren(transMemory))
      {
         header.appendChild(child);
      }
   }

   private void addAttributesAndChildren(Element tu, TransMemoryUnit transUnit)
   {
      ImmutableMap<String,String> attributes = TMXMetadataHelper.getAttributes(transUnit);
      for (Map.Entry<String, String> attr : attributes.entrySet())
      {
         tu.addAttribute(toAttribute(attr));
      }
      for (Element child : TMXMetadataHelper.getChildren(transUnit))
      {
         tu.appendChild(child);
      }
   }
   
   private Attribute toAttribute(Map.Entry<String, String> attr)
   {
      String name = attr.getKey();
      if (name.equals("xml:lang"))
      {
         return new Attribute(name, XMLConstants.XML_NS_URI, attr.getValue());
      }
      else
      {
         return new Attribute(name, attr.getValue());
      }
   }

   private Element buildTUV(TransMemoryUnitVariant tuv)
   {
      Element result = new Element("tuv");
      @Nonnull String trgContent = tuv.getPlainTextSegment();
      ImmutableMap<String,String> attributes = TMXMetadataHelper.getAttributes(tuv);
      for (Map.Entry<String, String> attr : attributes.entrySet())
      {
         result.addAttribute(toAttribute(attr));
      }
      for (Element child : TMXMetadataHelper.getChildren(tuv))
      {
         result.appendChild(child);
      }
      Element seg = new Element("seg");
      seg.appendChild(trgContent);
      result.appendChild(seg);
      return result;
   }

}
