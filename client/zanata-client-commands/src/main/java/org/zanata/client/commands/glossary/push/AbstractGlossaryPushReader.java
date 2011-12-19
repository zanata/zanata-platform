/*
 * Copyright 2011, Red Hat, Inc. and individual contributors
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
package org.zanata.client.commands.glossary.push;

import java.io.File;
import java.io.IOException;

import org.zanata.client.config.LocaleMapping;
import org.zanata.common.LocaleId;
import org.zanata.rest.dto.Glossary;

/**
 * 
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 * 
 **/
public abstract class AbstractGlossaryPushReader
{
   private GlossaryPushOptions opts;

   private String fileExtension;

   public abstract Glossary extractGlossary(File glossaryFile) throws IOException;

   protected LocaleId getLocaleFromMap(String localLocale)
   {
      if (!getOpts().getLocales().isEmpty())
      {
         for (LocaleMapping loc : getOpts().getLocales())
         {
            if (loc.getLocalLocale().equals(localLocale))
            {
               return new LocaleId(loc.getLocale());
            }
         }
      }
      return new LocaleId(localLocale);
   }

   public GlossaryPushOptions getOpts()
   {
      return opts;
   }

   public void setOpts(GlossaryPushOptions opts)
   {
      this.opts = opts;
   }

   public String getFileExtension()
   {
      return fileExtension;
   }

}
