package org.zanata.webtrans.shared.model;

import java.io.Serializable;

import com.google.gwt.user.client.rpc.IsSerializable;
import org.zanata.common.LocaleId;

//@Immutable
public class IdForLocale implements Identifier<Long>, IsSerializable, Serializable
{
   private static final long serialVersionUID = 1L;
   private Long id;
   private LocaleId localeId;

   // for GWT
   @SuppressWarnings("unused")
   private IdForLocale()
   {
   }

   public IdForLocale(Long id, LocaleId localeId)
   {
      this.id = id;
      this.localeId = localeId;
   }

   @Override
   public String toString()
   {
      return String.valueOf(id);
   }

   @Override
   public int hashCode()
   {
      return id.intValue();
   }

   @Override
   public Long getValue()
   {
      return id;
   }

   public Long getId()
   {
      return id;
   }

   public LocaleId getLocaleId()
   {
      return localeId;
   }

   @Override
   public boolean equals(Object obj)
   {
      if (obj == this)
      {
         return true;
      }
      if (obj == null)
      {
         return false;
      }
      return obj instanceof IdForLocale && ((IdForLocale) obj).id.equals(id);
   }
}
