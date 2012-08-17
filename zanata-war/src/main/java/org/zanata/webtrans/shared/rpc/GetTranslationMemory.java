package org.zanata.webtrans.shared.rpc;

import org.zanata.common.LocaleId;
import org.zanata.webtrans.shared.model.TransMemoryQuery;


public class GetTranslationMemory implements DispatchAction<GetTranslationMemoryResult>, HasSearchType
{
   private static final long serialVersionUID = 1L;
   private LocaleId localeId;
   private LocaleId sourceLocaleId;
   private TransMemoryQuery query;

   @SuppressWarnings("unused")
   private GetTranslationMemory()
   {
   }

   public GetTranslationMemory(TransMemoryQuery query, LocaleId localeId, LocaleId sourceLocaleId)
   {
      this.query = query;
      this.localeId = localeId;
      this.sourceLocaleId = sourceLocaleId;
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

   public LocaleId getSourceLocaleId()
   {
      return sourceLocaleId;
   }

   public void setSourceLocaleId(LocaleId sourceLocaleId)
   {
      this.sourceLocaleId = sourceLocaleId;
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
      result = prime * result + ((sourceLocaleId == null) ? 0 : sourceLocaleId.hashCode());
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
      if (sourceLocaleId == null)
      {
         if (other.sourceLocaleId != null)
         {
            return false;
         }
      }
      else if (!sourceLocaleId.equals(other.sourceLocaleId))
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
