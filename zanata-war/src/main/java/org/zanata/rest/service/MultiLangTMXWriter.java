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

import org.zanata.util.TMXUtils;

import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.XMLWriter;
import net.sf.okapi.common.filterwriter.TMXWriter;
import net.sf.okapi.common.resource.ITextUnit;
import net.sf.okapi.common.resource.TextContainer;
import net.sf.okapi.common.resource.TextFragment;

/**
 * @author Sean Flanigan <a href="mailto:sflaniga@redhat.com">sflaniga@redhat.com</a>
 *
 */
public class MultiLangTMXWriter extends TMXWriter
{

   public MultiLangTMXWriter(XMLWriter writer)
   {
      super(writer);
   }

   /**
    * Writes out a TextUnit with srclang *all*
    * @param textUnit an ITextUnit with the property srclang = *all*
    */
   public void writeTUFullMultiLang(ITextUnit textUnit)
   {
      super.writeTUFull(textUnit, TMXUtils.ALL_LOCALE);
   }

   @Override
   protected void writeTUV (TextFragment frag,
         LocaleId locale,
         TextContainer contForProp)
   {
      if (!locale.equals(TMXUtils.ALL_LOCALE))
      {
         super.writeTUV(frag, locale, contForProp);
      }
   }

}
