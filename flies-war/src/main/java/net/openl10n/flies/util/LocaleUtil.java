package net.openl10n.flies.util;

import com.ibm.icu.util.ULocale;

import net.openl10n.flies.common.LocaleId;

public class LocaleUtil
{
   public static LocaleId toLocaleId(ULocale locale)
   {
      return new LocaleId(locale.toLanguageTag());
   }
   
}
