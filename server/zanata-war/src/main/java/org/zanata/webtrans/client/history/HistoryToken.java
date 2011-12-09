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
   public static final String VALUE_DOCLIST_VIEW = "list";
   public static final String VALUE_EDITOR_VIEW = "doc";

   public static final String KEY_DOC_FILTER_TEXT = "filter";

   public static final String KEY_DOC_FILTER_OPTION = "filtertype";
   public static final String VALUE_DOC_FILTER_EXACT = "exact";
   public static final String VALUE_DOC_FILTER_INEXACT = "substr";

   private AppPresenter.Display.MainView view = null;
   private String fullDocPath = null;
   private Boolean docFilterExact = null;
   private String docFilterText = null;


   /**
    * Generate a history token from the given token string
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
               try
               {
                  historyToken.setDocumentPath((value));
               }
               catch (NullPointerException e)
               {
                  historyToken.setDocumentPath(null);
               }
               catch (NumberFormatException e)
               {
                  historyToken.setDocumentPath(null);
               }
            }
            else if (key == HistoryToken.KEY_VIEW)
            {
               if (value.equals(VALUE_EDITOR_VIEW))
               {
                  historyToken.setView(AppPresenter.Display.MainView.Editor);
               }
               else if (value.equals(VALUE_DOCLIST_VIEW))
               {
                  historyToken.setView(AppPresenter.Display.MainView.Documents);
               }
               else
               { // invalid view
                  historyToken.setView(null);
               }
            }
            else if (key == HistoryToken.KEY_DOC_FILTER_OPTION)
            {
               if (value == VALUE_DOC_FILTER_EXACT)
                  historyToken.setDocFilterExact(true);
               else if (value == VALUE_DOC_FILTER_INEXACT)
                  historyToken.setDocFilterExact(false);
            }
            else if (key == HistoryToken.KEY_DOC_FILTER_TEXT)
            {
               historyToken.setDocFilterText(value);
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

   public boolean hasDocumentPath()
   {
      return fullDocPath != null && fullDocPath.length() > 0;
   }

   public String getDocumentPath()
   {
      return fullDocPath;
   }

   public void setDocumentPath(String fullDocPath)
   {
      this.fullDocPath = fullDocPath;
   }

   public boolean hasView()
   {
      return view != null;
   }

   public AppPresenter.Display.MainView getView()
   {
      return view;
   }

   public void setView(AppPresenter.Display.MainView view)
   {
      this.view = view;
   }

   public boolean hasDocFilterExact()
   {
      return docFilterExact != null;
   }

   public Boolean getDocFilterExact()
   {
      return docFilterExact;
   }

   public void setDocFilterExact(Boolean exactMatch)
   {
      docFilterExact = exactMatch;
   }

   public boolean hasDocFilterText()
   {
      return docFilterText != null;
   }

   public String getDocFilterText()
   {
      return docFilterText;
   }

   public void setDocFilterText(String value)
   {
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

      if (hasView())
      {
         if (first)
            first = false;
         else
            token += PAIR_SEPARATOR;
         token += KEY_VIEW + DELIMITER_K_V;
         if (view == AppPresenter.Display.MainView.Editor)
         {
            token += VALUE_EDITOR_VIEW;
         }
         else if (view == AppPresenter.Display.MainView.Documents)
         {
            token += VALUE_DOCLIST_VIEW;
         }
      }

      if (hasDocumentPath())
      {
         if (first)
            first = false;
         else
            token += PAIR_SEPARATOR;
         token += KEY_DOCUMENT + DELIMITER_K_V + fullDocPath.toString();
      }

      if (hasDocFilterExact())
      {
         if (first)
            first = false;
         else
            token += PAIR_SEPARATOR;
         token += KEY_DOC_FILTER_OPTION + DELIMITER_K_V;
         token += docFilterExact ? VALUE_DOC_FILTER_EXACT : VALUE_DOC_FILTER_INEXACT;
      }

      if (hasDocFilterText())
      {
         if (first)
            first = false;
         else
            token += PAIR_SEPARATOR;
         token += KEY_DOC_FILTER_TEXT + DELIMITER_K_V + docFilterText;
      }

      return token;
   }
}
