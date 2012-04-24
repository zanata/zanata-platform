package org.zanata.webtrans.client.history;

import org.zanata.webtrans.client.presenter.MainView;

import com.allen_sauer.gwt.log.client.Log;

/**
 * Encapsulates a string token of key-value pairs for GWT history operations.
 * 
 * @author David Mason, damason@redhat.com
 * 
 */
public class HistoryToken
{
   private static final String DELIMITER_K_V = ":";
   private static final String PAIR_SEPARATOR = ";";

   public static final String KEY_DOCUMENT = "doc";

   public static final String KEY_VIEW = "view";
   public static final String VALUE_SEARCH_RESULTS_VIEW = "search";
   public static final String VALUE_EDITOR_VIEW = "doc";

   public static final String KEY_SEARCH_DOC_TEXT = "search";
   public static final String KEY_SEARCH_PROJECT_TEXT = "projectsearch";
   public static final String KEY_SEARCH_PROJECT_REPLACEMENT = "projectsearchreplace";

   public static final String KEY_SEARCH_PROJECT_CASE = "projectsearchcase";
   public static final String VALUE_SEARCH_PROJECT_CASE_SENSITIVE = "sensitive";


   public static final String KEY_DOC_FILTER_TEXT = "filter";

   public static final String KEY_DOC_FILTER_OPTION = "filtertype";
   public static final String VALUE_DOC_FILTER_EXACT = "exact";

   private MainView view;
   private String fullDocPath;
   private boolean docFilterExact;
   private String docFilterText;
   private String searchText;
   private String projectSearchText;
   private String projectSearchReplace;
   private boolean projectSearchCaseSensitive;

   // defaults
   private static final MainView DEFAULT_VIEW = MainView.Documents;
   private static final String DEFAULT_DOCUMENT_PATH = "";
   private static final String DEFAULT_DOC_FILTER_TEXT = "";
   private static final boolean DEFAULT_DOC_FILTER_EXACT = false;
   private static final String DEFAULT_SEARCH_TEXT = "";
   private static final String DEFAULT_PROJECT_SEARCH_TEXT = "";
   private static final String DEFAULT_PROJECT_SEARCH_REPLACE = "";
   private static final boolean DEFAULT_PROJECT_SEARCH_CASE_SENSITIVE = false;

   public HistoryToken()
   {
      view = DEFAULT_VIEW;
      fullDocPath = DEFAULT_DOCUMENT_PATH;
      docFilterText = DEFAULT_DOC_FILTER_TEXT;
      docFilterExact = DEFAULT_DOC_FILTER_EXACT;
      searchText = DEFAULT_SEARCH_TEXT;
      projectSearchText = DEFAULT_PROJECT_SEARCH_TEXT;
      projectSearchReplace = DEFAULT_PROJECT_SEARCH_REPLACE;
      projectSearchCaseSensitive = DEFAULT_PROJECT_SEARCH_CASE_SENSITIVE;
   }

   /**
    * Generate a history token from the given token string
    * 
    * @param token A GWT history token in the form key1:value1,key2:value2,...
    */
   public static HistoryToken fromTokenString(String token)
   {
      HistoryToken historyToken = new HistoryToken();

      if (token == null || token.length() == 0)
      {
         return historyToken;
      }

      // decode characters that may still be url-encoded
      //TODO need to encode/decode separators in filter and search strings in to/fromTokenString
      token = token.replaceAll("%3A", ":").replaceAll("%3B", ";").replaceAll("%2F", "/");

      for (String pairString : token.split(PAIR_SEPARATOR))
      {
         String[] pair = pairString.split(DELIMITER_K_V);
         String key;
         String value;
         try
         {
            key = pair[0];
            value = pair[1];
         }
         catch (ArrayIndexOutOfBoundsException e)
         {
            continue;
         }

         if (key.equals(HistoryToken.KEY_DOCUMENT))
         {
            historyToken.setDocumentPath(value);
         }
         else if (key.equals(HistoryToken.KEY_VIEW))
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
         else if (key.equals(KEY_DOC_FILTER_OPTION))
         {
            if (value.equals(VALUE_DOC_FILTER_EXACT))
            {
               historyToken.setDocFilterExact(true);
            }
            // else default used
         }
         else if (key.equals(KEY_DOC_FILTER_TEXT))
         {
            historyToken.setDocFilterText(value);
         }
         else if (key.equals(KEY_SEARCH_DOC_TEXT))
         {
            historyToken.setSearchText(value);
         }
         else if (key.equals(KEY_SEARCH_PROJECT_TEXT))
         {
            historyToken.setProjectSearchText(value);
         }
         else if (key.equals(KEY_SEARCH_PROJECT_REPLACEMENT))
         {
            historyToken.setProjectSearchReplacement(value);
         }
         else if (key.equals(KEY_SEARCH_PROJECT_CASE))
         {
            Log.info("found project search case key");
            if (value.equals(VALUE_SEARCH_PROJECT_CASE_SENSITIVE))
            {
               historyToken.setProjectSearchCaseSensitive(true);
               Log.info("found project search case sensitive value");
            }
            //else default used
         }
         else
         {
            Log.info("unrecognised history key: " + key);
         }

      }

      return historyToken;
   }

   public String getSearchText()
   {
      return this.searchText;
   }

   public void setSearchText(String value)
   {
      if (value == null || value.length() == 0)
         this.searchText = DEFAULT_SEARCH_TEXT;
      else
         this.searchText = value;
   }

   public String getProjectSearchText()
   {
      return this.projectSearchText;
   }

   public void setProjectSearchText(String value)
   {
      if (value == null || value.length() == 0)
         this.projectSearchText = DEFAULT_PROJECT_SEARCH_TEXT;
      else
         this.projectSearchText = value;
   }

   public String getProjectSearchReplacement()
   {
      return projectSearchReplace;
   }

   public void setProjectSearchReplacement(String value)
   {
      if (value == null || value.length() == 0)
      {
         projectSearchReplace = DEFAULT_PROJECT_SEARCH_REPLACE;
      }
      else
      {
         projectSearchReplace = value;
      }
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
      if (fullDocPath == null)
         this.fullDocPath = DEFAULT_DOCUMENT_PATH;
      else
         this.fullDocPath = fullDocPath;
   }

   public MainView getView()
   {
      return view;
   }

   public void setView(MainView view)
   {
      if (view == null)
         this.view = DEFAULT_VIEW;
      else
         this.view = view;
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
      if (value == null || value.length() == 0)
         this.docFilterText = DEFAULT_DOC_FILTER_TEXT;
      else
         this.docFilterText = value;
   }

   /**
    * @return a token string for use with
    *         {@link com.google.gwt.user.client.History}
    */
   public String toTokenString()
   {
      String token = "";
      boolean first = true;

      if (view != DEFAULT_VIEW)
      {
         if (first)
            first = false;
         else
            token += PAIR_SEPARATOR;
         token += KEY_VIEW + DELIMITER_K_V;
         if (view == MainView.Search)
         {
            token += VALUE_SEARCH_RESULTS_VIEW;
         }
         else
         {
            // must be editor
            token += VALUE_EDITOR_VIEW;
         }
      }

      if (!fullDocPath.equals(DEFAULT_DOCUMENT_PATH))
      {
         if (first)
            first = false;
         else
            token += PAIR_SEPARATOR;
         token += KEY_DOCUMENT + DELIMITER_K_V + fullDocPath;
      }

      if (docFilterExact != DEFAULT_DOC_FILTER_EXACT)
      {
         if (first)
            first = false;
         else
            token += PAIR_SEPARATOR;
         // exact is the only non-default filter value
         token += KEY_DOC_FILTER_OPTION + DELIMITER_K_V + VALUE_DOC_FILTER_EXACT;
      }

      if (!docFilterText.equals(DEFAULT_DOC_FILTER_TEXT))
      {
         if (first)
            first = false;
         else
            token += PAIR_SEPARATOR;
         token += KEY_DOC_FILTER_TEXT + DELIMITER_K_V + docFilterText;
      }

      if (!projectSearchText.equals(DEFAULT_PROJECT_SEARCH_TEXT))
      {
         if (first)
            first = false;
         else
            token += PAIR_SEPARATOR;
         token += KEY_SEARCH_PROJECT_TEXT + DELIMITER_K_V + projectSearchText;
      }

      if(!projectSearchReplace.equals(DEFAULT_PROJECT_SEARCH_REPLACE))
      {
         if (first)
            first = false;
         else
            token += PAIR_SEPARATOR;
         token += KEY_SEARCH_PROJECT_REPLACEMENT + DELIMITER_K_V + projectSearchReplace;
      }

      if (projectSearchCaseSensitive != DEFAULT_PROJECT_SEARCH_CASE_SENSITIVE)
      {
         if (first)
            first = false;
         else
            token += PAIR_SEPARATOR;
         // sensitive is the only non-default filter value
         token += KEY_SEARCH_PROJECT_CASE + DELIMITER_K_V + VALUE_SEARCH_PROJECT_CASE_SENSITIVE;
      }

      if (!searchText.equals(DEFAULT_SEARCH_TEXT))
      {
         if (first)
            first = false;
         else
            token += PAIR_SEPARATOR;
         token += KEY_SEARCH_DOC_TEXT + DELIMITER_K_V + searchText;
      }

      return token;
   }
}
