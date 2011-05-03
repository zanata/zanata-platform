/*
 * Copyright 2010, Red Hat, Inc. and individual contributors as indicated by the
 * @author tags. See the copyright.txt file in the distribution for a full
 * listing of individual contributors.
 * 
 * This is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 * 
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this software; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
 * site: http://www.fsf.org.
 */
package org.zanata.util;

import net.sf.okapi.common.LocaleId;
import net.sf.okapi.steps.wordcount.WordCounter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OkapiUtil
{
   private static final Logger log = LoggerFactory.getLogger(OkapiUtil.class);

   private OkapiUtil()
   {
   }

   public static LocaleId toOkapiLocale(org.zanata.common.LocaleId zanataLocale)
   {
      return LocaleId.fromBCP47(zanataLocale.getId());
   }

   public static long countWords(String s, String bcp47Locale)
   {
      try
      {
         LocaleId locale;
         try
         {
            locale = LocaleId.fromBCP47(bcp47Locale);
         }
         catch (Exception e)
         {
            log.error("can't understand '{}' as a BCP-47 locale; defaulting to English", bcp47Locale);
            locale = LocaleId.ENGLISH;
         }
         long count = WordCounter.count(s, locale);
         return count;
      }
      catch (Exception e)
      {
         log.error("unable to count words in string '{}' for locale '{}'", s, bcp47Locale);
         return 0;
      }
   }

}
