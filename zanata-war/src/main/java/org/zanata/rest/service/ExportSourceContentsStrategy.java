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

import net.sf.okapi.common.resource.ITextUnit;

import org.zanata.common.LocaleId;
import org.zanata.model.SourceContents;
import org.zanata.model.TargetContents;
import org.zanata.util.OkapiUtil;

/**
 * Writes one or more translations for a single TextFlow as a TMX translation unit.
 * @author Sean Flanigan <a href="mailto:sflaniga@redhat.com">sflaniga@redhat.com</a>
 *
 */
@ParametersAreNonnullByDefault
public class ExportSourceContentsStrategy extends AbstractExportTUStrategy<SourceContents>
{

   private final @Nullable LocaleId localeId;

   /**
    * Exports one or all locales.  If localeId is null, export all locales.
    * @param localeId
    */
   public ExportSourceContentsStrategy(@Nullable LocaleId localeId)
   {
      super();
      this.localeId = localeId;
   }

   @Override
   protected String getSrcContent(SourceContents tf)
   {
      return tf.getContents().get(0);
   }
   
   @Override
   protected net.sf.okapi.common.LocaleId getSourceLocale(SourceContents tf)
   {
      return OkapiUtil.toOkapiLocaleOrEmpty(tf.getLocale());
   }

   @Override
   protected String getTUID(SourceContents tf)
   {
      return tf.getQualifiedId();
   }

   @Override
   protected void addTextUnitVariants(ITextUnit textUnit, SourceContents tf)
   {
      if (localeId != null)
      {
         TargetContents tfTarget = tf.getTargetContents(localeId);
         if (tfTarget != null)
         {
            addTargetToTextUnit(textUnit, tfTarget);
         }
      }
      else
      {
         Iterable<TargetContents> allTargetContents = tf.getAllTargetContents();
         for (TargetContents tfTarget : allTargetContents)
         {
            addTargetToTextUnit(textUnit, tfTarget);
         }
      }
   }

   private void addTargetToTextUnit(ITextUnit textUnit, TargetContents tfTarget)
   {
      if (tfTarget.getState().isTranslated())
      {
         String trgContent = tfTarget.getContents().get(0);
         net.sf.okapi.common.LocaleId locId = OkapiUtil.toOkapiLocale(tfTarget.getLocaleId());
         addTargetToTextUnit(textUnit, locId, trgContent);
      }
   }

}
