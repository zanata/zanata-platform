package net.openl10n.flies.hibernate.search;

import net.openl10n.flies.common.LocaleId;

import org.hibernate.search.bridge.TwoWayStringBridge;

public class LocaleIdBridge implements TwoWayStringBridge
{

   @Override
   public String objectToString(Object value)
   {
      if (value instanceof LocaleId)
      {
         LocaleId locale = (LocaleId) value;
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
      return new LocaleId(localeId);
   }

}
