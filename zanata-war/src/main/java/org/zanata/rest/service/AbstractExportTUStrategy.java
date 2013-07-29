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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

import lombok.extern.slf4j.Slf4j;

import net.sf.okapi.common.filterwriter.TMXWriter;
import net.sf.okapi.common.resource.ITextUnit;
import net.sf.okapi.common.resource.Property;
import net.sf.okapi.common.resource.TextFragment;
import net.sf.okapi.common.resource.TextUnit;

/**
 * @author Sean Flanigan <a href="mailto:sflaniga@redhat.com">sflaniga@redhat.com</a>
 *
 * @param <T> a translation unit (either SourceContents/HTextFlow or TransMemoryUnit)
 */
@ParametersAreNonnullByDefault
@Slf4j
public abstract class AbstractExportTUStrategy<T>
{

   public AbstractExportTUStrategy()
   {
   }

   /**
    * Writes the specified SourceContents (TextFlow) and one or all of its translations to the TMXWriter.
    * @param tmxWriter
    * @param tuidPrefix String to be prepended to all resIds when generating tuids
    * @param tu the SourceContents (TextFlow) whose contents and translations are to be exported
    */
   public void exportTranslationUnit(TMXWriter tmxWriter, T tu)
   {
      assert tmxWriter.isWriteAllPropertiesAsAttributes();
      net.sf.okapi.common.LocaleId sourceLocaleId = getSourceLocale(tu);
      String tuid = getTUID(tu);
      // Perhaps we could encode plurals using TMX attributes?
      String srcContent = getSrcContent(tu);
      if (srcContent.contains("\0"))
      {
         log.warn("illegal null character; discarding SourceContents with id="+tuid);
         return;
      }

      ITextUnit textUnit = new TextUnit(tuid, srcContent);
      setSrcLang(textUnit, sourceLocaleId);
      textUnit.setName(tuid);
      addTextUnitVariants(textUnit, tu);
      // If there aren't any translations for this TU, we shouldn't include it.
      // From the TMX spec: "Logically, a complete translation-memory
      // database will contain at least two <tuv> elements in each translation
      // unit."
      if (!textUnit.getTargetLocales().isEmpty())
      {
         tmxWriter.writeTUFull(textUnit, sourceLocaleId);
      }
   }

   protected void setSrcLang(ITextUnit textUnit, net.sf.okapi.common.LocaleId sourceLocaleId)
   {
      textUnit.setProperty(new Property("srclang", sourceLocaleId.toBCP47()));
   }

   protected void addTargetToTextUnit(ITextUnit textUnit, net.sf.okapi.common.LocaleId locId, String trgContent)
   {
      if (trgContent.contains("\0"))
      {
         log.warn("illegal null character; discarding TargetContents with sourceId="+textUnit.getId()+", locale="+locId);
      }
      else
      {
      TextFragment target = new TextFragment(trgContent);
      textUnit.setTargetContent(locId, target);
      }
   }

   protected abstract @Nonnull net.sf.okapi.common.LocaleId getSourceLocale(T tu);
   protected abstract @Nullable String getTUID(T tu);
   protected abstract void addTextUnitVariants(ITextUnit textUnit, T tu);
   protected abstract String getSrcContent(T tu);

}
