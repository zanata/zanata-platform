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

import javax.annotation.Nonnull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.okapi.common.LocaleId;
import net.sf.okapi.steps.tokenization.Tokenizer;
import net.sf.okapi.steps.tokenization.tokens.Tokens;

public class OkapiUtil
{
   private static final Logger log = LoggerFactory.getLogger(OkapiUtil.class);

   private OkapiUtil()
   {
   }

   @SuppressWarnings("null")
   public static @Nonnull LocaleId toOkapiLocale(@Nonnull org.zanata.common.LocaleId zanataLocale)
   {
      return LocaleId.fromBCP47(zanataLocale.getId());
   }

   /**
    * Count words using Okapi's WordCounter, which tries to implement the LISA
    * standard GMX-V:
    * http://web.archive.org/web/20090403134742/http://www.lisa.org/Global
    * -information-m.105.0.html
    * 
    * @param s
    * @param bcp47Locale
    * @return
    */
   public static long countWords(String s, String bcp47Locale)
   {
      if (s == null)
      {
         log.debug("null string");
         return 0;
      }
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

         Tokens tokens = StringTokenizer.tokenizeString(s, locale, "WORD");
         return tokens.size();
      }
      catch (Exception e)
      {
         Object[] args = new Object[] {s, bcp47Locale, e};
         log.error("unable to count words in string '{}' for locale '{}'", args);
         return 0;
      }
   }

   /**
    * Extracts plain text from a TMX entry. This ignores the TMX elements that mark up native code sequences:
    * <bpt></bpt>
    * <ept></ept>
    * <it></it>
    * <ph></ph>
    *
    * @param markedUpContent The tmx marked up content.
    * @return A string with all tmx mark-up content stripped out. Essentially a plain text version of the string.
    */
   public static String extractPlainTextTmxContent(String markedUpContent)
   {
      return removeXmlTags(markedUpContent, "bpt", "ept", "it", "ph");
   }

   /**
    * Removes all ocurrences of an xml tag (and it's contents) from the given String.
    * Example:
    *
    * <code>removeXmlTags("This <a>is</a> a string", "a")</code> will yield the string "This is  a string"
    *
    * @param content The content to be stripped of the xml tags.
    * @param xmlTagNames The tag names to be removed from the content (Don't use the angle braces)
    * @return A string with all the given xml tags (and their sub-content) removed.
    */
   private static String removeXmlTags( final String content, String ... xmlTagNames )
   {
      String processedContent = content;
      for( String xmlTagName : xmlTagNames )
      {
         String openTag = "<" + xmlTagName;
         String closeTag = "</" + xmlTagName + ">";
         while( processedContent.contains(openTag) )
         {
            int openIdx = processedContent.indexOf(openTag);
            int closeIdx = processedContent.indexOf(closeTag);
            processedContent = processedContent.substring(0,openIdx) + processedContent.substring(closeIdx + closeTag.length());
         }
      }
      return processedContent;
   }

   private static class StringTokenizer extends Tokenizer
   {
      public static Tokens tokenizeString(String text, LocaleId language, String... tokenNames)
      {
         synchronized (Tokenizer.class)
         {
            return Tokenizer.tokenizeString(text, language, tokenNames);
         }
      }
   }

}
