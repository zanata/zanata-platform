package org.zanata.webtrans.shared.rpc;

import net.customware.gwt.dispatch.shared.Action;

import org.zanata.common.LocaleId;


public class GetGlossary implements Action<GetGlossaryResult>, HasSearchType
{
   private static final long serialVersionUID = 1L;
   private LocaleId localeId;
   private String query;
   private SearchType searchType;
   private LocaleId srcLocaleId;

   @SuppressWarnings("unused")
   private GetGlossary()
   {
   }

   public GetGlossary(String query, LocaleId localeId, LocaleId srcLocaleId, SearchType searchType)
   {
      this.query = query;
      this.localeId = localeId;
      this.searchType = searchType;
      this.srcLocaleId = srcLocaleId;
   }

   @Override
   public SearchType getSearchType()
   {
      return searchType;
   }

   public void setLocaleId(LocaleId localeId)
   {
      this.localeId = localeId;
   }

   public LocaleId getLocaleId()
   {
      return localeId;
   }

   public void setQuery(String query)
   {
      this.query = query;
   }

   public String getQuery()
   {
      return query;
   }

   public LocaleId getSrcLocaleId()
   {
      return srcLocaleId;
   }

   @Override
   public int hashCode()
   {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((localeId == null) ? 0 : localeId.hashCode());
      result = prime * result + ((query == null) ? 0 : query.hashCode());
      result = prime * result + ((searchType == null) ? 0 : searchType.hashCode());
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
      if (!(obj instanceof GetGlossary))
      {
         return false;
      }
      GetGlossary other = (GetGlossary) obj;
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
      return searchType == other.searchType;
   }

}
