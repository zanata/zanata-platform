package net.openl10n.flies.hibernate.search;

import net.openl10n.flies.common.LocaleId;

import org.apache.lucene.search.Filter;
import org.hibernate.search.annotations.Factory;
import org.hibernate.search.annotations.Key;
import org.hibernate.search.filter.FilterKey;
import org.hibernate.search.filter.StandardFilterKey;
import org.jboss.seam.Seam;

public class TranslatedFilterFactory
{

   @Factory
   public Filter getFilter()
   {
      TranslatedFilter filter = (TranslatedFilter) Seam.componentForName("translatedFilter").newInstance();
      filter.setLocale(locale);
      return filter;
   }

   private LocaleId locale;

   public LocaleId getLocale()
   {
      return locale;
   }

   public void setLocale(LocaleId locale)
   {
      this.locale = locale;
   }

   @Key
   public FilterKey getKey()
   {
      StandardFilterKey key = new StandardFilterKey();
      key.addParameter(locale);
      return key;
   }

}
