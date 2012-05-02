package org.zanata.webtrans.shared.rpc;


public interface HasSearchType
{
   public static enum SearchType
   {
      /**
       * Search for exact terms, adjacent to each other and in correct order.
       * (Lucene PhraseQuery)
       */
      EXACT,

      /**
       * Search for similar terms. (Lucene fuzzy query)
       */
      FUZZY,

      /**
       * Uses search string as a raw Lucene query without adding any escapes.
       */
      RAW,

      /**
       * Fuzzy search for TextFlows which have similar strings for corresponding plural forms
       */
      FUZZY_PLURAL
   }

   SearchType getSearchType();
}
