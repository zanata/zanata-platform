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

import java.util.Collections;
import java.util.Map;

import net.sf.okapi.common.filterwriter.TMXWriter;
import net.sf.okapi.common.resource.TextFragment;

import org.zanata.common.ContentState;
import org.zanata.common.LocaleId;
import org.zanata.model.NamedDocument;
import org.zanata.model.SourceContents;
import org.zanata.model.TargetContents;

/**
 * @author Sean Flanigan <a href="mailto:sflaniga@redhat.com">sflaniga@redhat.com</a>
 *
 */
public class ExportSingleLocaleStrategy implements ExportTUStrategy
{

   private LocaleId targetLocaleId;

   public ExportSingleLocaleStrategy(LocaleId targetLocaleId)
   {
      this.targetLocaleId = targetLocaleId;
   }

   @Override
   public void exportTranslationUnit(TMXWriter tmxWriter, NamedDocument doc, SourceContents tf)
   {
      // FIXME handle multiple locales: use writeTUFull
      TargetContents tfTarget = tf.getTargetContents(targetLocaleId);
      // TODO handle ContentState.Reviewed
      if (tfTarget != null && tfTarget.getState() == ContentState.Approved)
      {
         // Perhaps we could encode plurals using TMX attributes?
         String srcContent = tf.getContents().get(0);
         TextFragment source = new TextFragment(srcContent);
         String trgContent = tfTarget.getContents().get(0);
         TextFragment target = new TextFragment(trgContent);
         String tuid = doc.getName() + ":" + tf.getResId();
         Map<String, String> attributes = Collections.emptyMap();
         tmxWriter.writeTU(source, target, tuid, attributes);
      }
   }

}
