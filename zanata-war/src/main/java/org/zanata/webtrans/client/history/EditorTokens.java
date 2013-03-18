package org.zanata.webtrans.client.history;

import java.util.List;

enum EditorTokens implements TokensConverter
{
   INSTANCE;

   static final String KEY_TEXT_FLOW_ID = "textflow";
   static final String KEY_SEARCH_DOC_TEXT = "search";
   static final String KEY_DOCUMENT = "doc";

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
   }
}
