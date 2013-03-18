package org.zanata.webtrans.client.history;

import java.util.List;

enum EditorTokens implements TokensConverter
{
   INSTANCE;

   static final String KEY_TEXT_FLOW_ID = "textflow";
   static final String KEY_SEARCH_DOC_TEXT = "search";
   static final String KEY_DOCUMENT = "doc";
   static final String KEY_MESSAGE_FILTER_UNTRANSLATED = "untranslated";
   static final String KEY_MESSAGE_FILTER_TRANSLATED = "translated";
   static final String KEY_MESSAGE_FILTER_FUZZY = "fuzzy";
   static final String VALUE_MESSAGE_FILTER = "show";

   @Override
   public void populateHistoryToken(HistoryToken historyToken, Token token)
   {
      String key = token.getKey();
      String value = token.getValue();
      if (key.equals(EditorTokens.KEY_DOCUMENT))
      {
         historyToken.setDocumentPath(value);
      }
      else if (key.equals(EditorTokens.KEY_SEARCH_DOC_TEXT))
      {
         historyToken.setSearchText(value);
      }
      else if (key.equals(EditorTokens.KEY_TEXT_FLOW_ID))
      {
         historyToken.setTextFlowId(value);
      }
      if (key.equals(KEY_MESSAGE_FILTER_UNTRANSLATED))
      {
         historyToken.setFilterUntranslated(true);
      }
      if (key.equals(KEY_MESSAGE_FILTER_FUZZY))
      {
         historyToken.setFilterFuzzy(true);
      }
      if (key.equals(KEY_MESSAGE_FILTER_TRANSLATED))
      {
         historyToken.setFilterTranslated(true);
      }
   }

   @Override
   public void toTokenString(HistoryToken historyToken, List<Token> tokens)
   {
      if (!historyToken.getDocumentPath().equals(HistoryToken.DEFAULT_DOCUMENT_PATH))
      {
         tokens.add(new Token(KEY_DOCUMENT, historyToken.getDocumentPath()));
      }

      if (!historyToken.getSearchText().equals(HistoryToken.DEFAULT_SEARCH_TEXT))
      {
         tokens.add(new Token(KEY_SEARCH_DOC_TEXT, historyToken.getSearchText()));
      }

      if (historyToken.getTextFlowId() != null)
      {
         tokens.add(new Token(KEY_TEXT_FLOW_ID, historyToken.getTextFlowId().toString()));
      }

      if (historyToken.isFilterUntranslated() != historyToken.isFilterFuzzy() || historyToken.isFilterUntranslated() != historyToken.isFilterTranslated())
      {
         // if filter options is set (not showing everything)
         if (historyToken.isFilterUntranslated())
         {
            tokens.add(new Token(KEY_MESSAGE_FILTER_UNTRANSLATED, VALUE_MESSAGE_FILTER));
         }
         if (historyToken.isFilterFuzzy())
         {
            tokens.add(new Token(KEY_MESSAGE_FILTER_FUZZY, VALUE_MESSAGE_FILTER));
         }
         if (historyToken.isFilterTranslated())
         {
            tokens.add(new Token(KEY_MESSAGE_FILTER_TRANSLATED, VALUE_MESSAGE_FILTER));
         }
      }
   }
}
