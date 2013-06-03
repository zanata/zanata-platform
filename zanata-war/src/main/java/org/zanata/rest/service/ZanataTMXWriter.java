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

import java.io.Closeable;

import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.XMLWriter;
import net.sf.okapi.common.filterwriter.TMXWriter;
import net.sf.okapi.common.resource.ITextUnit;
import net.sf.okapi.common.resource.TextContainer;
import net.sf.okapi.common.resource.TextFragment;

/**
 * This class extends TMXWriter to add {@link #writeTUFull(ITextUnit
 * textUnit, LocaleId sourceLocaleId)} using a ThreadLocal trick.  It
 * is a temporary workaround for
 * https://code.google.com/p/okapi/issues/detail?id=342
 * @author Sean Flanigan <a href="mailto:sflaniga@redhat.com">sflaniga@redhat.com</a>
 * @todo remove this class when https://code.google.com/p/okapi/issues/detail?id=342 is released
 */
public class ZanataTMXWriter extends TMXWriter implements Closeable
{
   private ThreadLocal<LocaleId> threadLocal = new ThreadLocal<LocaleId>();
   private LocaleId allLocales = new LocaleId("*all*", false);

   public ZanataTMXWriter(XMLWriter xmlWriter)
   {
      super(xmlWriter);
   }

   public void writeTUFull(ITextUnit textUnit, LocaleId sourceLocaleId)
   {
      threadLocal.set(sourceLocaleId);
      super.writeTUFull(textUnit);
   }

   @Override
   protected void writeTUV(TextFragment frag, LocaleId locale, TextContainer contForProp)
   {
      LocaleId actualLocale = locale.equals(allLocales) ? threadLocal.get() : locale;
      super.writeTUV(frag, actualLocale, contForProp);
   }

   @Override
   public void close()
   {
      threadLocal.remove();
   }
}
