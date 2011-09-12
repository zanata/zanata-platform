package org.zanata.webtrans.shared.rpc;

import net.customware.gwt.dispatch.shared.Action;

import org.zanata.common.LocaleId;


public class GetGlossary implements Action<GetGlossaryResult>
{

   public static enum SearchType
   {
      EXACT, FUZZY
   }

   private static final long serialVersionUID = 1L;
   private LocaleId localeId;
   private String query;
   private SearchType searchType;

   @SuppressWarnings("unused")
   private GetGlossary()
   {
   }

   public GetGlossary(String query, LocaleId localeId, SearchType searchType)
   {
      this.query = query;
      this.localeId = localeId;
      this.searchType = searchType;
   }

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

}
