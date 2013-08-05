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
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.xml.XMLConstants;

import nu.xom.Attribute;
import nu.xom.Element;

import org.zanata.common.LocaleId;
import org.zanata.model.tm.HasTMMetadata;
import org.zanata.model.tm.TMXMetadataHelper;
import org.zanata.model.tm.TransMemory;
import org.zanata.model.tm.TransMemoryUnitVariant;
import org.zanata.model.tm.TransMemoryUnit;
import org.zanata.util.TMXConstants;
import org.zanata.util.VersionUtility;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;

/**
 * Writes one or more variations for a single TransMemoryUnit as a TMX translation unit.
 * @author Sean Flanigan <a href="mailto:sflaniga@redhat.com">sflaniga@redhat.com</a>
 *
 */
@ParametersAreNonnullByDefault
public class TransMemoryTMXExportStrategy implements TMXExportStrategy<TransMemoryUnit>
{
   private static final String creationTool = "Zanata " + TransMemoryTMXExportStrategy.class.getSimpleName();
   private static final String creationToolVersion =
         VersionUtility.getVersionInfo(TransMemoryTMXExportStrategy.class).getVersionNo();
   private TransMemory tm;

   public TransMemoryTMXExportStrategy(TransMemory tm)
   {
      super();
      this.tm = tm;
   }

   @Override
   public Element buildHeader() throws IOException
   {
      Element header = new Element("header");
      addAttributes(header, TMXMetadataHelper.getAttributes(tm));
      addChildren(header, tm);
      header.addAttribute(new Attribute("creationtool", creationTool));
      header.addAttribute(new Attribute("creationtoolversion", creationToolVersion));
      return header;
   }

   @Override
   public Optional<Element> buildTU(TransMemoryUnit transUnit) throws IOException
   {
      Element tu = new Element("tu");

      Optional<LocaleId> sourceLocaleId = getSourceLocale(transUnit);
      String srcLang = sourceLocaleId.isPresent() ?
            sourceLocaleId.get().getId() : TMXConstants.ALL_LOCALE;
      tu.addAttribute(new Attribute(TMXConstants.SRCLANG, srcLang));

      String tuid = transUnit.getTransUnitId();
      if (tuid != null)
      {
         tu.addAttribute(new Attribute("tuid", tuid));
      }
      addAttributes(tu, TMXMetadataHelper.getAttributes(transUnit));
      addChildren(tu, transUnit);

      for (TransMemoryUnitVariant tuv: transUnit.getTransUnitVariants().values())
      {
         tu.appendChild(buildTUV(tuv));
      }
      return Optional.of(tu);
   }

   private static Optional<LocaleId> getSourceLocale(TransMemoryUnit tu)
   {
      String tuSourceLanguage = tu.getSourceLanguage();
      if (tuSourceLanguage != null)
      {
         return Optional.of(new LocaleId(tuSourceLanguage));
      }
      return Optional.absent();
   }

   private static void addAttributes(Element toElem, ImmutableMap<String,String> attributes)
   {
      for (Map.Entry<String, String> attr : attributes.entrySet())
      {
         toElem.addAttribute(toAttribute(attr));
      }
   }

   private static void addChildren(Element toHeader, HasTMMetadata fromTransMemory)
   {
      for (Element child : TMXMetadataHelper.getChildren(fromTransMemory))
      {
         toHeader.appendChild(child);
      }
   }

   private static Attribute toAttribute(Map.Entry<String, String> attr)
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

   private static Element buildTUV(TransMemoryUnitVariant fromVariant)
   {
      Element tuv = new Element("tuv");
      addAttributes(tuv, TMXMetadataHelper.getAttributes(fromVariant));
      addChildren(tuv, fromVariant);
      Element seg = new Element("seg");
      @Nonnull String trgContent = fromVariant.getPlainTextSegment();
      seg.appendChild(trgContent);
      tuv.appendChild(seg);
      return tuv;
   }

}
