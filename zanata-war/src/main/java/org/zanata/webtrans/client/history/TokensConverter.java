package org.zanata.webtrans.client.history;

import java.util.List;

interface TokensConverter
{
   void populateHistoryToken(HistoryToken historyToken, Token token);

   void toTokenString(HistoryToken historyToken, List<Token> tokens);
}
