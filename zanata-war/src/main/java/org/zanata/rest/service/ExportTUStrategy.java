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

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

import lombok.extern.slf4j.Slf4j;
import net.sf.okapi.common.resource.ITextUnit;
import net.sf.okapi.common.resource.TextFragment;
import net.sf.okapi.common.resource.TextUnit;

import org.zanata.common.LocaleId;
import org.zanata.model.SourceContents;
import org.zanata.model.TargetContents;
import org.zanata.util.OkapiUtil;

/**
 * Writes one or more translations for a single TextFlow as a TMX translation unit.
 * @author Sean Flanigan <a href="mailto:sflaniga@redhat.com">sflaniga@redhat.com</a>
 *
 */
@Slf4j
@ParametersAreNonnullByDefault
public class ExportTUStrategy<TU extends SourceContents> //implements ExportTUStrategy<SourceContents>
{
   private final LocaleId localeId;

   /**
    * Exports one or all locales.  If localeId is null, export all locales.
    * @param localeId
    */
   public ExportTUStrategy(@Nullable LocaleId localeId)
   {
      this.localeId = localeId;
   }

   /**
    * Writes the specified SourceContents (TextFlow) and one or all of its translations to the TMXWriter.
    * @param tmxWriter
    * @param tuidPrefix String to be prepended to all resIds when generating tuids
    * @param tf the SourceContents (TextFlow) whose contents and translations are to be exported
    */
   public void exportTranslationUnit(ZanataTMXWriter tmxWriter, SourceContents tf)
   {
      net.sf.okapi.common.LocaleId sourceLocaleId = OkapiUtil.toOkapiLocaleOrEmpty(tf.getLocale());
      String tuid = tf.getQualifiedId();
      // Perhaps we could encode plurals using TMX attributes?
      String srcContent = tf.getContents().get(0);
      if (srcContent.contains("\0"))
      {
         log.warn("illegal null character; discarding SourceContents with id="+tuid);
         return;
      }

      ITextUnit textUnit = new TextUnit(tuid, srcContent);
      textUnit.setName(tuid);
      if (localeId != null)
      {
         TargetContents tfTarget = tf.getTargetContents(localeId);
         addTargetToTextUnit(textUnit, tfTarget);
      }
      else
      {
         Iterable<TargetContents> allTargetContents = tf.getAllTargetContents();
         for (TargetContents tfTarget : allTargetContents)
         {
            addTargetToTextUnit(textUnit, tfTarget);
         }
      }
      // If there aren't any translations for this TU, we shouldn't include it.
      // From the TMX spec: "Logically, a complete translation-memory
      // database will contain at least two <tuv> elements in each translation
      // unit."
      if (!textUnit.getTargetLocales().isEmpty())
      {
         tmxWriter.writeTUFull(textUnit, sourceLocaleId);
      }
   }

   private void addTargetToTextUnit(ITextUnit textUnit, TargetContents tfTarget)
   {
      if (tfTarget != null && tfTarget.getState().isTranslated())
      {
         String trgContent = tfTarget.getContents().get(0);
         if (trgContent.contains("\0"))
         {
            log.warn("illegal null character; discarding TargetContents with sourceId="+textUnit.getId()+", locale="+tfTarget.getLocaleId());
            return;
         }
         net.sf.okapi.common.LocaleId locId = OkapiUtil.toOkapiLocale(tfTarget.getLocaleId());
         TextFragment target = new TextFragment(trgContent);
         textUnit.setTargetContent(locId, target);
      }
   }

}
