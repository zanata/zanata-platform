package org.zanata.webtrans.client.history;

import java.util.List;

import org.zanata.webtrans.client.presenter.MainView;

/**
 * @author Patrick Huang <a
 *         href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
public enum MainViewTokens implements TokensConverter
{
   INSTANCE;

   static final String KEY_VIEW = "view";
   static final String VALUE_SEARCH_RESULTS_VIEW = "search";
   static final String VALUE_EDITOR_VIEW = "doc";

   @Override
   public void populateHistoryToken(HistoryToken historyToken, Token token)
   {
      String key = token.getKey();
      String value = token.getValue();
      if (key.equals(KEY_VIEW))
      {
         if (value.equals(VALUE_EDITOR_VIEW))
         {
            historyToken.setView(MainView.Editor);
         }
         else if (value.equals(VALUE_SEARCH_RESULTS_VIEW))
         {
            historyToken.setView(MainView.Search);
         }
         // else default (document list) will be used
      }
   }

   @Override
   public void toTokenString(HistoryToken historyToken, List<Token> tokens)
   {

      if (historyToken.getView() == MainView.Search)
      {
         tokens.add(new Token(KEY_VIEW, VALUE_SEARCH_RESULTS_VIEW));
      }
      else if (historyToken.getView() == MainView.Editor)
      {
         tokens.add(new Token(KEY_VIEW, VALUE_EDITOR_VIEW));
      }
   }
}
