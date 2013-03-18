package org.zanata.webtrans.client.history;

import java.util.List;

/**
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
enum  DocumentListTokens implements TokensConverter
{
   INSTANCE;

   static final String KEY_DOC_FILTER_TEXT = "filter";
   static final String KEY_DOC_FILTER_OPTION = "filtertype";
   static final String KEY_DOC_FILTER_CASE = "filtercase";
   static final String VALUE_DOC_FILTER_EXACT = "exact";
   static final String VALUE_DOC_FILTER_CASE_SENSITIVE = "sensitive";

   @Override
   public void populateHistoryToken(HistoryToken historyToken, Token token)
   {

      String key = token.getKey();
      String value = token.getValue();
      if (key.equals(DocumentListTokens.KEY_DOC_FILTER_OPTION) && value.equals(DocumentListTokens.VALUE_DOC_FILTER_EXACT))
      {
         historyToken.setDocFilterExact(true);
         // else default used
      }
      else if (key.equals(DocumentListTokens.KEY_DOC_FILTER_TEXT))
      {
         historyToken.setDocFilterText(value);
      }
      else if (key.equals(DocumentListTokens.KEY_DOC_FILTER_CASE))
      {
         if (value.equals(DocumentListTokens.VALUE_DOC_FILTER_CASE_SENSITIVE))
         {
            historyToken.setDocFilterCaseSensitive(true);
         }
         // else default used
      }
   }

   @Override
   public void toTokenString(HistoryToken historyToken, List<Token> tokens)
   {
      if (historyToken.getDocFilterExact() != HistoryToken.DEFAULT_DOC_FILTER_EXACT)
      {
         // exact is the only non-default filter value
         tokens.add(new Token(KEY_DOC_FILTER_OPTION, VALUE_DOC_FILTER_EXACT));
      }
      if (!historyToken.getDocFilterText().equals(HistoryToken.DEFAULT_DOC_FILTER_TEXT))
      {
         tokens.add(new Token(KEY_DOC_FILTER_TEXT, historyToken.getDocFilterText()));
      }

      if (historyToken.getProjectSearchCaseSensitive() != HistoryToken.DEFAULT_DOC_FILTER_CASE_SENSITIVE)
      {
         tokens.add(new Token(KEY_DOC_FILTER_CASE, VALUE_DOC_FILTER_CASE_SENSITIVE));
      }
   }
}
