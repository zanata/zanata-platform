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

package org.zanata.client.commands.push;

import java.io.File;

import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.lang.StringUtils;
import org.zanata.client.config.LocaleList;
import org.zanata.client.config.LocaleMapping;


public class TargetFileFilter implements IOFileFilter
{

   private final LocaleList locales;
   private final String extension;

   public TargetFileFilter(LocaleList locales, String extension)
   {
      this.locales = locales;
      this.extension = extension;
   }

   @Override
   public boolean accept(File file)
   {
      return accept(file.getName());
   }

   @Override
   public boolean accept(File dir, String name)
   {
      return accept(name);
   }

   private boolean accept(String name)
   {
      if (!StringUtils.endsWithIgnoreCase(name, extension))
         return false;
      if (name.contains("_"))
      {
         for (LocaleMapping locMap : locales)
         {
            String loc = locMap.getJavaLocale().toLowerCase();
            if (StringUtils.endsWithIgnoreCase(name, "_" + loc + extension))
            {
               return false;
            }
         }
      }
      return true;
   }

}
