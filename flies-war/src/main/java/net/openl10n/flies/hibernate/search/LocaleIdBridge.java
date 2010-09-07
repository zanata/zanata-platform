package net.openl10n.flies.hibernate.search;

import net.openl10n.flies.common.LocaleId;
import net.openl10n.flies.model.HLocale;

import org.hibernate.search.bridge.TwoWayStringBridge;

public class LocaleIdBridge implements TwoWayStringBridge
{

   @Override
   public String objectToString(Object value)
   {
      if (value instanceof HLocale)
      {
         LocaleId locale = ((HLocale) value).getLocaleId();
         return locale.toString();
      }
      else
      {
         throw new IllegalArgumentException("LocaleIdBridge used on a non-LocaleId type: " + value.getClass());
      }
   }

   @Override
   public Object stringToObject(String localeId)
   {
      return new HLocale(new LocaleId(localeId));
   }

}
