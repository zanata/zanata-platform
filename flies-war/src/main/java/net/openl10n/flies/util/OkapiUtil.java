package net.openl10n.flies.util;

import net.sf.okapi.common.LocaleId;
import net.sf.okapi.steps.wordcount.WordCounter;

public class OkapiUtil
{
   private OkapiUtil()
   {
   }

   public static LocaleId toOkapiLocale(net.openl10n.flies.common.LocaleId fliesLocale)
   {
      return LocaleId.fromBCP47(fliesLocale.getId());
   }

   public static long countWords(String s, String bcp47Locale)
   {
      long count = WordCounter.count(s, LocaleId.fromBCP47(bcp47Locale));
      return count;
   }

}
