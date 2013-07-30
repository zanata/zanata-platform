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

import static net.sf.okapi.common.LocaleId.fromBCP47;

import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.filterwriter.TMXWriter;
import net.sf.okapi.common.resource.ITextUnit;
import net.sf.okapi.common.resource.Property;
import nu.xom.Element;

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
public class TransMemoryExportTMXStrategy extends ExportTMXStrategy<TransMemoryUnit>
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
   public void exportHeader(TMXWriter tmxWriter)
   {
      ImmutableMap<String, String> metadata = TMXMetadataHelper.getAttributes(tm);
      // TODO Okapi can't export header's child nodes (prop and note)
//      TMXMetadataHelper.getChildren(tm);
      net.sf.okapi.common.LocaleId sourceLocale;
      String srclang = tm.getSourceLanguage();
      if (srclang == null)
      {
         sourceLocale = TMXUtils.ALL_LOCALE;
      }
      else
      {
         sourceLocale = new net.sf.okapi.common.LocaleId(srclang);
      }
      // TMXWriter demands a non-null target locale, but if you write
      // your TUs with writeTUFull(), it is never used.
      LocaleId targetLocale = net.sf.okapi.common.LocaleId.EMPTY;
      String segType = (String) metadata.get(TMXUtils.SEG_TYPE);
      String originalTMFormat = (String) metadata.get(TMXUtils.O_TMF);
      String dataType = (String) metadata.get(TMXUtils.DATA_TYPE);

      tmxWriter.writeStartDocument(
            sourceLocale,
            targetLocale,
            creationTool, creationToolVersion,
            segType, originalTMFormat, dataType);
   }

   @Override
   protected void exportMetadata(ITextUnit textUnit, TransMemoryUnit tu)
   {
      ImmutableMap<String,String> attributes = TMXMetadataHelper.getAttributes(tu);
      for (Map.Entry<String, String> attr : attributes.entrySet())
      {
         textUnit.setProperty(toProp(attr));
      }
      for (Property prop : toProps(TMXMetadataHelper.getChildren(tu)))
      {
         textUnit.setProperty(prop);
      }
   }

   private Property toProp(Map.Entry<String, String> attr)
   {
      return new Property(attr.getKey(), attr.getValue());
   }

   private List<Property> toProps(List<Element> children)
   {
      List<Property> props = Lists.newArrayList();
      for (Element elem : children)
      {
         if (elem.getLocalName().equals("prop"))
         {
            // TODO Okapi can only write string values for properties
            Property property = new Property(elem.getAttributeValue("type"), elem.getValue());
            // TODO Okapi doesn't allow multiple props with same type
            props.add(property);
         }
         // TODO Okapi can't write note elements
      }
      return props;
   }

   @Override
   protected Optional<LocaleId> getSourceLocale(TransMemoryUnit tu)
   {
      String tuSourceLanguage = tu.getSourceLanguage();
      if (tuSourceLanguage != null)
      {
         return Optional.of(LocaleId.fromBCP47(tuSourceLanguage));
      }
      return Optional.absent();
   }

   @Override
   protected @Nonnull net.sf.okapi.common.LocaleId resolveSourceLocale(TransMemoryUnit tu)
   {
      return fromBCP47(resolveSourceLanguage(tu));
   }

   private @Nonnull String resolveSourceLanguage(TransMemoryUnit tu)
   {
      String tuSourceLanguage = tu.getSourceLanguage();
      if (tuSourceLanguage != null)
      {
         return tuSourceLanguage;
      }
      else
      {
         String tmSourceLanguage = tu.getTranslationMemory().getSourceLanguage();
         assert tmSourceLanguage != null;
         return tmSourceLanguage;
      }

   }

   @Override
   protected @Nonnull String getSrcContent(TransMemoryUnit tu)
   {
      TransMemoryUnitVariant tuv = tu.getTransUnitVariants().get(resolveSourceLanguage(tu));
      assert tuv != null;
      return tuv.getPlainTextSegment();
   }

   @Override
   protected @Nullable String getTUID(TransMemoryUnit tu)
   {
      return tu.getTransUnitId();
   }

   @Override
   protected void addTextUnitVariants(ITextUnit textUnit, TransMemoryUnit tu)
   {
      for (TransMemoryUnitVariant tuv: tu.getTransUnitVariants().values())
      {
         addTUV(textUnit, tuv);
      }
   }

   private void addTUV(ITextUnit textUnit, TransMemoryUnitVariant tuv)
   {
      @Nonnull String trgContent = tuv.getPlainTextSegment();
      @Nonnull net.sf.okapi.common.LocaleId locId = fromBCP47(tuv.getLanguage());
      ImmutableMap<String,String> attributes = TMXMetadataHelper.getAttributes(tuv);
      List<Property> props = Lists.newArrayList();
      for (Map.Entry<String, String> attr : attributes.entrySet())
      {
         props.add(toProp(attr));
      }
      props.addAll(toProps(TMXMetadataHelper.getChildren(tuv)));
      addTargetToTextUnit(textUnit, locId, trgContent, props);
   }

}
