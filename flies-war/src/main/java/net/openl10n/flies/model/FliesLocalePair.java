package net.openl10n.flies.model;

import java.io.Serializable;

import com.ibm.icu.util.ULocale;

import net.openl10n.flies.common.LocaleId;

public class FliesLocalePair implements Serializable
{
   private static final long serialVersionUID = 1L;
   private LocaleId localeId;
   private boolean active;

   public boolean isActive()
   {
      return active;
   }

   public void setActive(boolean active)
   {
      this.active = active;
   }

   public LocaleId getLocaleId()
   {
      return localeId;
   }

   public void setLocaleId(LocaleId localeId)
   {
      this.localeId = localeId;
   }

   private ULocale uLocale;

   public ULocale getuLocale()
   {
      return uLocale;
   }

   public void setuLocale(ULocale uLocale)
   {
      this.uLocale = uLocale;
   }

   public FliesLocalePair(ULocale locale)
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

      this.localeId = new LocaleId(id);
      this.uLocale = locale;
   }


   public FliesLocalePair(LocaleId localeId)
   {
      this.localeId = localeId;
      this.uLocale = new ULocale(this.localeId.getId());
   }

}
