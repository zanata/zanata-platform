package org.zanata.webtrans.client.history;

import java.util.List;

import org.zanata.webtrans.client.presenter.MainView;
import com.beust.jcommander.internal.Lists;
import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.Iterables;

/**
 * Encapsulates a string token of key-value pairs for GWT history operations.
 * 
 * @author David Mason, damason@redhat.com
 * 
 */
public class HistoryToken
{
   private static final String PAIR_SEPARATOR = ";";
   private static final Splitter SPLITTER = Splitter.on(PAIR_SEPARATOR).omitEmptyStrings().trimResults();
   private static final Joiner JOINER = Joiner.on(PAIR_SEPARATOR).skipNulls();

   // defaults
   protected static final MainView DEFAULT_VIEW = MainView.Documents;
   protected static final String DEFAULT_DOCUMENT_PATH = "";
   protected static final String DEFAULT_DOC_FILTER_TEXT = "";
   protected static final boolean DEFAULT_DOC_FILTER_EXACT = false;
   protected static final boolean DEFAULT_DOC_FILTER_CASE_SENSITIVE = false;
   protected static final String DEFAULT_SEARCH_TEXT = "";
   protected static final String DEFAULT_PROJECT_SEARCH_TEXT = "";
   protected static final String DEFAULT_PROJECT_SEARCH_REPLACE = "";
   protected static final boolean DEFAULT_PROJECT_SEARCH_CASE_SENSITIVE = false;
   protected static final boolean DEFAULT_PROJECT_SEARCH_IN_SOURCE = false;
   protected static final boolean DEFAULT_PROJECT_SEARCH_IN_TARGET = true;
   private static final Long DEFAULT_TEXT_FLOW_ID = null;

   private MainView view;
   private String fullDocPath;
   private boolean docFilterExact;
   private boolean docFilterCaseSensitive;
   private String docFilterText;
   private String searchText;
   private String projectSearchText;
   private String projectSearchReplace;
   private boolean projectSearchCaseSensitive;
   private boolean projectSearchInSource;
   private boolean projectSearchInTarget;
   private Long textFlowId;

   public HistoryToken()
   {
      view = DEFAULT_VIEW;
      fullDocPath = DEFAULT_DOCUMENT_PATH;
      docFilterText = DEFAULT_DOC_FILTER_TEXT;
      docFilterExact = DEFAULT_DOC_FILTER_EXACT;
      docFilterCaseSensitive = DEFAULT_DOC_FILTER_CASE_SENSITIVE;
      searchText = DEFAULT_SEARCH_TEXT;
      projectSearchText = DEFAULT_PROJECT_SEARCH_TEXT;
      projectSearchReplace = DEFAULT_PROJECT_SEARCH_REPLACE;
      projectSearchCaseSensitive = DEFAULT_PROJECT_SEARCH_CASE_SENSITIVE;
      projectSearchInSource = DEFAULT_PROJECT_SEARCH_IN_SOURCE;
      projectSearchInTarget = DEFAULT_PROJECT_SEARCH_IN_TARGET;
      textFlowId = DEFAULT_TEXT_FLOW_ID;
   }

   /**
    * Generate a history token from the given token string
    *
    * @param token A GWT history token in the form key1:value1;key2:value2;...
    * @see #toTokenString()
    */
   public static HistoryToken fromTokenString(String token)
   {
      HistoryToken historyToken = new HistoryToken();

      if (Strings.isNullOrEmpty(token))
      {
         return historyToken;
      }

      // decode characters that may still be url-encoded
      token = token.replace("%3A", ":").replace("%3B", ";").replace("%2F", "/");

      Iterable<Token> tokens = Iterables.transform(SPLITTER.split(token), TokenParserFunction.FUNCTION);

      for (Token entry : tokens)
      {
         if (entry != Token.NULL_TOKEN)
         {
            DocumentListTokens.INSTANCE.populateHistoryToken(historyToken, entry);
            MainViewTokens.INSTANCE.populateHistoryToken(historyToken, entry);
            ProjectSearchTokens.INSTANCE.populateHistoryToken(historyToken, entry);
            EditorTokens.INSTANCE.populateHistoryToken(historyToken, entry);
         }
      }
      return historyToken;
   }


   public void setProjectSearchInSource(boolean searchInSource)
   {
      projectSearchInSource = searchInSource;
   }

   public void setProjectSearchInTarget(boolean searchInTarget)
   {
      projectSearchInTarget = searchInTarget;
   }

   public String getSearchText()
   {
      return this.searchText;
   }

   public void setSearchText(String value)
   {
      this.searchText = Strings.isNullOrEmpty(value) ? DEFAULT_SEARCH_TEXT : value;
   }

   public String getProjectSearchText()
   {
      return this.projectSearchText;
   }

   public void setProjectSearchText(String value)
   {
      this.projectSearchText = Strings.isNullOrEmpty(value) ? DEFAULT_PROJECT_SEARCH_TEXT : value;
   }

   public String getProjectSearchReplacement()
   {
      return projectSearchReplace;
   }

   public void setProjectSearchReplacement(String value)
   {
      projectSearchReplace = Strings.isNullOrEmpty(value) ? DEFAULT_PROJECT_SEARCH_REPLACE : value;
   }

   public boolean getProjectSearchCaseSensitive()
   {
      return this.projectSearchCaseSensitive;
   }

   public void setProjectSearchCaseSensitive(boolean caseSensitive)
   {
      this.projectSearchCaseSensitive = caseSensitive;
   }

   public String getDocumentPath()
   {
      return fullDocPath;
   }

   public void setDocumentPath(String fullDocPath)
   {
      this.fullDocPath = Strings.isNullOrEmpty(fullDocPath) ? DEFAULT_DOCUMENT_PATH : fullDocPath;
   }

   public MainView getView()
   {
      return view;
   }

   public void setView(MainView view)
   {
      this.view = view == null ? DEFAULT_VIEW : view;
   }

   public boolean getDocFilterExact()
   {
      return docFilterExact;
   }

   public void setDocFilterExact(boolean exactMatch)
   {
      docFilterExact = exactMatch;
   }

   public String getDocFilterText()
   {
      return docFilterText;
   }

   public void setDocFilterText(String value)
   {
      this.docFilterText = Strings.isNullOrEmpty(value) ? DEFAULT_DOC_FILTER_TEXT : value;
   }

   public void setDocFilterCaseSensitive(boolean caseSensitive)
   {
      docFilterCaseSensitive = caseSensitive;
   }

   public boolean isDocFilterCaseSensitive()
   {
      return docFilterCaseSensitive;
   }

   /**
    * @return a token string for use with
    *         {@link com.google.gwt.user.client.History}
    * @see HistoryToken#fromTokenString(String)
    */
   public String toTokenString()
   {
      List<Token> tokens = Lists.newArrayList();

      MainViewTokens.INSTANCE.toTokenString(this, tokens);
      DocumentListTokens.INSTANCE.toTokenString(this, tokens);
      ProjectSearchTokens.INSTANCE.toTokenString(this, tokens);
      EditorTokens.INSTANCE.toTokenString(this, tokens);

      return JOINER.join(tokens);
   }

   public boolean isProjectSearchInSource()
   {
      return projectSearchInSource;
   }

   public boolean isProjectSearchInTarget()
   {
      return projectSearchInTarget;
   }

   public void setTextFlowId(String textFlowId)
   {
      String textFlow = Strings.nullToEmpty(textFlowId);
      if (textFlow.matches("^\\d+$"))
      {
         this.textFlowId = Long.valueOf(textFlow);
      }
      else
      {
         this.textFlowId = null;
      }
   }

   public Long getTextFlowId()
   {
      return textFlowId;
   }

   private static enum TokenParserFunction implements Function<String, Token>
   {
      FUNCTION;
      
      @Override
      public Token apply(String input)
      {
         return Token.fromString(input);
      }
   }
}
