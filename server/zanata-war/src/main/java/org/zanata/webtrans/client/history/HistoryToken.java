package org.zanata.webtrans.client.history;

import org.zanata.webtrans.client.presenter.AppPresenter;

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
   // public static final String VALUE_DOCLIST_VIEW = "list";
   public static final String VALUE_EDITOR_VIEW = "doc";

   public static final String KEY_DOC_FILTER_TEXT = "filter";

   public static final String KEY_DOC_FILTER_OPTION = "filtertype";
   public static final String VALUE_DOC_FILTER_EXACT = "exact";
   public static final String VALUE_DOC_FILTER_INEXACT = "substr";

   // defaults
   private static final String DEFAULT_DOCUMENT_PATH = "";
   private static final String DEFAULT_DOC_FILTER_TEXT = "";
   private static final boolean DEFAULT_DOC_FILTER_EXACT = false;
   private static final AppPresenter.Display.MainView DEFAULT_VIEW = AppPresenter.Display.MainView.Documents;

   private AppPresenter.Display.MainView view;
   private String fullDocPath;
   private boolean docFilterExact;
   private String docFilterText;

   public HistoryToken()
   {
      view = DEFAULT_VIEW;
      fullDocPath = DEFAULT_DOCUMENT_PATH;
      docFilterText = DEFAULT_DOC_FILTER_TEXT;
      docFilterExact = DEFAULT_DOC_FILTER_EXACT;
   }

   /**
    * Generates a history token from the given token string. Default values will
    * be used for any keys that are not present or do not have a valid value
    * associated with them.
    * 
    * @param token A GWT history token in the form key1:value1,key2:value2,...
    */
   public static HistoryToken fromTokenString(String token)
   {
      HistoryToken historyToken = new HistoryToken();

      String[] pair;

      try
      {
         for (String pairString : token.split(PAIR_SEPARATOR))
         {
            pair = pairString.split(DELIMITER_K_V);
            String key = pair[0];
            String value = pair[1];

            if (key == HistoryToken.KEY_DOCUMENT)
            {
               if (value != null && value.length() > 0)
                  historyToken.setDocumentPath((value));
            }
            else if (key == HistoryToken.KEY_VIEW)
            {
               if (value.equals(VALUE_EDITOR_VIEW))
                  historyToken.setView(AppPresenter.Display.MainView.Editor);
               // else assume document list
            }
            else if (key == HistoryToken.KEY_DOC_FILTER_TEXT)
            {
               if (value != null && value.length() > 0)
                  historyToken.setDocFilterText(value);
            }
            else if (key == HistoryToken.KEY_DOC_FILTER_OPTION)
            {
               if (value == VALUE_DOC_FILTER_EXACT)
                  historyToken.setDocFilterExact(true);
            }
            else
               Log.info("unrecognised history key: " + key);

         }
      }
      catch (IllegalArgumentException e)
      {
         throw new IllegalArgumentException("token must be a list of key-value pairs in the form key1:value1,key2:value2,...", e);
      }

      return historyToken;
   }

   /**
    * 
    * @return the document path, may be an empty string, will not be null
    */
   public String getDocumentPath()
   {
      if (fullDocPath == null)
         return DEFAULT_DOCUMENT_PATH;
      return fullDocPath;
   }

   public void setDocumentPath(String fullDocPath)
   {
      this.fullDocPath = fullDocPath;
   }

   /**
    * 
    * @return the current view, will never return null
    */
   public AppPresenter.Display.MainView getView()
   {
      if (view == null)
         return DEFAULT_VIEW;
      return view;
   }

   public void setView(AppPresenter.Display.MainView view)
   {
      this.view = view;
   }

   /**
    * 
    * @return true if document filter should accept only an exact match
    */
   public boolean getDocFilterExact()
   {
      return docFilterExact;
   }

   public void setDocFilterExact(boolean exactMatch)
   {
      docFilterExact = exactMatch;
   }

   /**
    * 
    * @return the string against which to filter the document list. May be an
    *         empty string, will never be null.
    */
   public String getDocFilterText()
   {
      if (docFilterText == null)
         return DEFAULT_DOC_FILTER_TEXT;
      return docFilterText;
   }

   public void setDocFilterText(String value)
   {
      this.docFilterText = value;
   }


   /**
    * Generates a token string to represent this {@link HistoryToken}. Fields
    * that have their default value are not included in the string.
    * 
    * @return a token string for use with
    *         {@link com.google.gwt.user.client.History}
    */
   public String toTokenString()
   {
      String token = "";
      boolean first = true;

      if (getView() != DEFAULT_VIEW)
      {
         if (first)
            first = false;
         else
            token += PAIR_SEPARATOR;
         token += KEY_VIEW + DELIMITER_K_V;
         // this conditional is unnecessary
         if (view == AppPresenter.Display.MainView.Editor)
         {
            token += VALUE_EDITOR_VIEW;
         }
      }

      if (!getDocumentPath().equals(DEFAULT_DOCUMENT_PATH))
      {
         if (first)
            first = false;
         else
            token += PAIR_SEPARATOR;
         token += KEY_DOCUMENT + DELIMITER_K_V + fullDocPath;
      }

      if (getDocFilterExact() != DEFAULT_DOC_FILTER_EXACT)
      {
         if (first)
            first = false;
         else
            token += PAIR_SEPARATOR;
         token += KEY_DOC_FILTER_OPTION + DELIMITER_K_V;
         // this is redundant if defaults don't change
         token += docFilterExact ? VALUE_DOC_FILTER_EXACT : VALUE_DOC_FILTER_INEXACT;
      }

      if (!getDocFilterText().equals(DEFAULT_DOC_FILTER_TEXT))
      {
         if (first)
            first = false;
         else
            token += PAIR_SEPARATOR;
         token += KEY_DOC_FILTER_TEXT + DELIMITER_K_V + docFilterText;
      }

      return token;
   }

   public Object clone()
   {
      HistoryToken newToken = new HistoryToken();
      newToken.view = this.view;
      newToken.fullDocPath = this.fullDocPath;
      newToken.docFilterText = this.docFilterText;
      newToken.docFilterExact = this.docFilterExact;

      return newToken;
   }
}
