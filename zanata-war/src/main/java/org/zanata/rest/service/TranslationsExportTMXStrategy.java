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
import java.util.Set;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.xml.XMLConstants;

import lombok.extern.slf4j.Slf4j;
import nu.xom.Attribute;
import nu.xom.Element;

import org.zanata.common.LocaleId;
import org.zanata.model.SourceContents;
import org.zanata.model.TargetContents;
import org.zanata.util.TMXUtils;
import org.zanata.util.VersionUtility;

import com.google.common.base.Optional;
import com.google.common.collect.Sets;

/**
 * Writes translations for Zanata Projects/TextFlows as TMX.
 * @author Sean Flanigan <a href="mailto:sflaniga@redhat.com">sflaniga@redhat.com</a>
 *
 */
@ParametersAreNonnullByDefault
@Slf4j
public class TranslationsExportTMXStrategy implements ExportTMXStrategy<SourceContents>
{
   private static final String creationTool = "Zanata " + TranslationsExportTMXStrategy.class.getSimpleName();
   private static final String creationToolVersion =
         VersionUtility.getVersionInfo(TranslationsExportTMXStrategy.class).getVersionNo();

   private final @Nullable LocaleId localeId;

   /**
    * Exports one or all locales.  If localeId is null, export all locales.
    * @param localeId
    */
   public TranslationsExportTMXStrategy(@Nullable LocaleId localeId)
   {
      super();
      this.localeId = localeId;
   }

   @Override
   public Element buildHeader() throws IOException
   {
      String segType = "block"; // TODO other segmentation types
      String dataType = "unknown"; // TODO track data type metadata throughout the system

      Element header = new Element("header");
      header.addAttribute(new Attribute("creationtool", creationTool));
      header.addAttribute(new Attribute("creationtoolversion", creationToolVersion));
      header.addAttribute(new Attribute("segtype", segType));
      header.addAttribute(new Attribute("o-tmf", "unknown"));
      header.addAttribute(new Attribute("adminlang", "en"));
      header.addAttribute(new Attribute("srclang", TMXUtils.ALL_LOCALE));
      header.addAttribute(new Attribute("datatype", dataType));
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
   public Element buildTU(SourceContents tu) throws IOException
   {
      Element textUnit = new Element("tu");

      LocaleId sourceLocaleId = tu.getLocale();
      String tuid = tu.getQualifiedId();
      // Perhaps we could encode plurals using TMX attributes?
      String srcContent = tu.getContents().get(0);
      if (srcContent.contains("\0"))
      {
         log.warn("illegal null character; discarding SourceContents with id="+tuid);
         return null;
      }
      String srcLang = sourceLocaleId.getId();
      textUnit.addAttribute(new Attribute(TMXUtils.SRCLANG, srcLang));

      textUnit.addAttribute(new Attribute("tuid", tuid));

      Set<Element> tuvs = buildTUVs(tu);
      for (Element tuv : tuvs)
      {
         textUnit.appendChild(tuv);
      }
      return textUnit;
   }

   private Set<Element> buildTUVs(SourceContents tf)
   {
      Set<Element> result = Sets.newLinkedHashSet();
      result.add(buildSourceTUV(tf));
      if (localeId != null)
      {
         TargetContents tfTarget = tf.getTargetContents(localeId);
         if (tfTarget != null)
         {
            Optional<Element> tuv = buildTargetTUV(tfTarget);
            result.addAll(tuv.asSet());
         }
      }
      else
      {
         Iterable<TargetContents> allTargetContents = tf.getAllTargetContents();
         for (TargetContents tfTarget : allTargetContents)
         {
            Optional<Element> tuv = buildTargetTUV(tfTarget);
            result.addAll(tuv.asSet());
         }
      }
      return result;
   }

   private Element buildSourceTUV(SourceContents tf)
   {
      Element sourceTuv = new Element("tuv");
      sourceTuv.addAttribute(new Attribute("xml:lang", XMLConstants.XML_NS_URI, tf.getLocale().getId()));
      Element seg = new Element("seg");
      seg.appendChild(tf.getContents().get(0));
      sourceTuv.appendChild(seg);
      return sourceTuv;
   }

   private Optional<Element> buildTargetTUV(TargetContents tfTarget)
   {
      if (tfTarget.getState().isTranslated())
      {
         LocaleId locId = tfTarget.getLocaleId();
         String trgContent = tfTarget.getContents().get(0);
         if (trgContent.contains("\0"))
         {
            log.warn("illegal null character; discarding TargetContents with locale="+
                  locId+", contents="+trgContent);
            return Optional.absent();
         }
         Element tuv = new Element("tuv");
         tuv.addAttribute(new Attribute("xml:lang", XMLConstants.XML_NS_URI, locId.getId()));
         Element seg = new Element("seg");
         seg.appendChild(trgContent);
         tuv.appendChild(seg);
         return Optional.of(tuv);
      }
      return Optional.absent();
   }

}
