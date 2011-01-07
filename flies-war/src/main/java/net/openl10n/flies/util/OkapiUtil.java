package net.openl10n.flies.util;

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

   public static LocaleId toOkapiLocale(net.openl10n.flies.common.LocaleId fliesLocale)
   {
      return LocaleId.fromBCP47(fliesLocale.getId());
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
