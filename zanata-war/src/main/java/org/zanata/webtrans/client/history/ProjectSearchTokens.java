package org.zanata.webtrans.client.history;

import java.util.List;

import static org.zanata.webtrans.client.history.HistoryToken.*;

enum ProjectSearchTokens implements TokensConverter
{
   INSTANCE;

   static final String VALUE_SEARCH_PROJECT_FIELD_BOTH = "both";
   static final String VALUE_SEARCH_PROJECT_FIELD_TARGET = "target";
   static final String VALUE_SEARCH_PROJECT_FIELD_SOURCE = "source";
   static final String KEY_SEARCH_PROJECT_FIELDS = "projectsearchin";
   static final String VALUE_SEARCH_PROJECT_CASE_SENSITIVE = "sensitive";
   static final String KEY_SEARCH_PROJECT_CASE = "projectsearchcase";
   static final String KEY_SEARCH_PROJECT_REPLACEMENT = "projectsearchreplace";
   static final String KEY_SEARCH_PROJECT_TEXT = "projectsearch";

   @Override
   public void populateHistoryToken(HistoryToken historyToken, Token token)
   {
      String key = token.getKey();
      String value = token.getValue();
      if (key.equals(ProjectSearchTokens.KEY_SEARCH_PROJECT_TEXT))
      {
         historyToken.setProjectSearchText(value);
      }
      else if (key.equals(ProjectSearchTokens.KEY_SEARCH_PROJECT_REPLACEMENT))
      {
         historyToken.setProjectSearchReplacement(value);
      }
      else if (key.equals(ProjectSearchTokens.KEY_SEARCH_PROJECT_CASE))
      {
         if (value.equals(ProjectSearchTokens.VALUE_SEARCH_PROJECT_CASE_SENSITIVE))
         {
            historyToken.setProjectSearchCaseSensitive(true);
         }
         // else default used
      }
      else if (key.equals(ProjectSearchTokens.KEY_SEARCH_PROJECT_FIELDS))
      {
         if (value.equals(ProjectSearchTokens.VALUE_SEARCH_PROJECT_FIELD_SOURCE))
         {
            historyToken.setProjectSearchInSource(true);
            historyToken.setProjectSearchInTarget(false);
         }
         else if (value.equals(ProjectSearchTokens.VALUE_SEARCH_PROJECT_FIELD_TARGET))
         {
            historyToken.setProjectSearchInSource(false);
            historyToken.setProjectSearchInTarget(true);
         }
         else if (value.equals(ProjectSearchTokens.VALUE_SEARCH_PROJECT_FIELD_BOTH))
         {
            historyToken.setProjectSearchInSource(true);
            historyToken.setProjectSearchInTarget(true);
         }
         // else default used
      }
   }

   @Override
   public void toTokenString(HistoryToken historyToken, List<Token> tokens)
   {
      if (!historyToken.getProjectSearchText().equals(HistoryToken.DEFAULT_PROJECT_SEARCH_TEXT))
      {
         tokens.add(new Token(KEY_SEARCH_PROJECT_TEXT, historyToken.getProjectSearchText()));
      }

      if (!historyToken.getProjectSearchReplacement().equals(HistoryToken.DEFAULT_PROJECT_SEARCH_REPLACE))
      {
         tokens.add(new Token(KEY_SEARCH_PROJECT_REPLACEMENT, historyToken.getProjectSearchReplacement()));
      }

      if (historyToken.getProjectSearchCaseSensitive() != HistoryToken.DEFAULT_PROJECT_SEARCH_CASE_SENSITIVE)
      {
         // sensitive is the only non-default filter value
         tokens.add(new Token(KEY_SEARCH_PROJECT_CASE, VALUE_SEARCH_PROJECT_CASE_SENSITIVE));
      }

      boolean projectSearchInSource = historyToken.isProjectSearchInSource();
      boolean projectSearchInTarget = historyToken.isProjectSearchInTarget();

      if (projectSearchInSource != DEFAULT_PROJECT_SEARCH_IN_SOURCE || projectSearchInTarget != DEFAULT_PROJECT_SEARCH_IN_TARGET)
      {
         if (projectSearchInSource && projectSearchInTarget)
         {
            tokens.add(new Token(KEY_SEARCH_PROJECT_FIELDS, VALUE_SEARCH_PROJECT_FIELD_BOTH));
         }
         else if (projectSearchInSource)
         {
            tokens.add(new Token(KEY_SEARCH_PROJECT_FIELDS, VALUE_SEARCH_PROJECT_FIELD_SOURCE));
         }
         // ignore if neither
      }
   }
}
