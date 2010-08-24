package net.openl10n.flies.common;

import java.io.Serializable;

import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonValue;

import com.ibm.icu.util.ULocale;

public final class LocaleId implements Serializable
{

   private static final long serialVersionUID = -7977805381672178179L;

   private String id;
   // TODO split up to language code, country code, qualifier etc..

   public static final LocaleId EN = new LocaleId("en");
   public static final LocaleId EN_US = new LocaleId("en-US");
   public static final LocaleId DE = new LocaleId("de");
   public static final LocaleId FR = new LocaleId("fr");
   public static final LocaleId ES = new LocaleId("es");

   // JaxB needs a no-arg constructor :(
   @SuppressWarnings("unused")
   private LocaleId()
   {
      id = null;
   }

   @JsonCreator
   public LocaleId(String localeId)
   {
      if (localeId == null)
         throw new IllegalArgumentException("localeId");
      if (localeId.indexOf('_') != -1)
         throw new IllegalArgumentException("expected lang[-country[-modifier]], got " + localeId);
      this.id = localeId.intern();
   }

   public LocaleId(ULocale locale)
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

      this.id = builder.toString();
   }

   @Override
   public boolean equals(Object obj)
   {
      if (obj == this)
         return true;
      if (!(obj instanceof LocaleId))
         return false;
      return this.id.equals(((LocaleId) obj).id);
   }

   @Override
   public int hashCode()
   {
      return id.hashCode();
   }

   @Override
   @JsonValue
   public String toString()
   {
      return id;
   }

   public static LocaleId fromJavaName(String localeName)
   {
      return new LocaleId(localeName.replace('_', '-'));
   }

   public String toJavaName()
   {
      return id.replace('-', '_');
   }

   public String getId()
   {
      return id;
   }

}
