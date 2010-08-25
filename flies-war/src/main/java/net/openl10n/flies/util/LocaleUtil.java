package net.openl10n.flies.util;

import com.ibm.icu.util.ULocale;

import net.openl10n.flies.common.LocaleId;

public class LocaleUtil
{
   public static LocaleId toLocaleId(ULocale locale)
   {
      StringBuilder builder = new StringBuilder();
      builder.append(locale.getLanguage());
      if (!locale.getCountry().isEmpty())
      {
         builder.append('-');
         builder.append(locale.getCountry());
      }
      if (!locale.getScript().isEmpty())
      {
         builder.append('-');
         builder.append(locale.getScript());
      }
      if (!locale.getVariant().isEmpty())
      {
         builder.append('-');
         builder.append(locale.getVariant());
      }

      String id = builder.toString();

      return new LocaleId(id);
   }

}
