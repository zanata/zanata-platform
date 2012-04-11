package org.zanata.webtrans.shared.rpc;

import net.customware.gwt.dispatch.shared.Action;

import org.zanata.common.LocaleId;
import org.zanata.webtrans.shared.model.TransMemoryQuery;


public class GetTranslationMemory implements Action<GetTranslationMemoryResult>, HasSearchType
{
   private static final long serialVersionUID = 1L;
   private LocaleId localeId;
   private TransMemoryQuery query;

   @SuppressWarnings("unused")
   private GetTranslationMemory()
   {
   }

   public GetTranslationMemory(TransMemoryQuery query, LocaleId localeId)
   {
      this.query = query;
      this.localeId = localeId;
   }

   @Override
   public SearchType getSearchType()
   {
      return query.getSearchType();
   }

   public void setLocaleId(LocaleId localeId)
   {
      this.localeId = localeId;
   }

   public LocaleId getLocaleId()
   {
      return localeId;
   }

   public TransMemoryQuery getQuery()
   {
      return query;
   }

   @Override
   public int hashCode()
   {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((localeId == null) ? 0 : localeId.hashCode());
      result = prime * result + ((query == null) ? 0 : query.hashCode());
      return result;
   }

   @Override
   public boolean equals(Object obj)
   {
      if (this == obj)
      {
         return true;
      }
      if (obj == null)
      {
         return false;
      }
      if (!(obj instanceof GetTranslationMemory))
      {
         return false;
      }
      GetTranslationMemory other = (GetTranslationMemory) obj;
      if (localeId == null)
      {
         if (other.localeId != null)
         {
            return false;
         }
      }
      else if (!localeId.equals(other.localeId))
      {
         return false;
      }
      if (query == null)
      {
         if (other.query != null)
         {
            return false;
         }
      }
      else if (!query.equals(other.query))
      {
         return false;
      }
      return true;
   }

}
