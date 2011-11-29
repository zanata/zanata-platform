package org.zanata.webtrans.client.history;

import java.util.HashMap;
import java.util.Map;

import org.zanata.webtrans.client.presenter.AppPresenter;
import org.zanata.webtrans.shared.model.DocumentId;

/**
 * Encapsulates a string token of key-value pairs for GWT history operations.
 * 
 * @author David Mason, damason@redhat.com
 * 
 */
public class HistoryToken
{
   private static final String KEY_VALUE_SEPARATOR = ":";
   private static final String PAIR_SEPARATOR = ",";

   public static final String KEY_DOCUMENT = "doc";
   public static final String KEY_VIEW = "view";

   public static final String VALUE_DOCLIST_VIEW = "doclist";
   public static final String VALUE_EDITOR_VIEW = "editor";

   private Map<String, String> members;

   public HistoryToken()
   {
      members = new HashMap<String, String>();
   }


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
            pair = pairString.split(KEY_VALUE_SEPARATOR);
            historyToken.members.put(pair[0], pair[1]);
         }
      }
      catch (IllegalArgumentException e)
      {
         throw new IllegalArgumentException("token must be a list of key-value pairs in the form key1:value1,key2:value2,...", e);
      }

      return historyToken;
   }

   public DocumentId getDocumentId()
   {
      try
      {
         return new DocumentId(Long.parseLong(members.get(HistoryToken.KEY_DOCUMENT)));
      }
      catch (NullPointerException e)
      {
         return null;
      }
      catch (NumberFormatException e)
      {
         return null;
      }
   }

   public void setDocumentId(DocumentId docId)
   {
      members.put(HistoryToken.KEY_DOCUMENT, docId.toString());
   }

   public AppPresenter.Display.MainView getView()
   {
      try
      {
         String view = members.get(KEY_VIEW);
         if (view.equals(VALUE_EDITOR_VIEW))
         {
            return AppPresenter.Display.MainView.Editor;
         }
         else if (view.equals(VALUE_DOCLIST_VIEW))
         {
            return AppPresenter.Display.MainView.Documents;
         }
         else
         { // invalid view
            return null;
         }
      }
      catch (ClassCastException e)
      {
         return null;
      }
      catch (NullPointerException e)
      {
         return null;
      }
   }

   public void setView(AppPresenter.Display.MainView view)
   {
      if (view == AppPresenter.Display.MainView.Editor)
      {
         members.put(HistoryToken.KEY_VIEW, VALUE_EDITOR_VIEW);
      }
      else if (view == AppPresenter.Display.MainView.Documents)
      {
         members.put(HistoryToken.KEY_VIEW, VALUE_DOCLIST_VIEW);
      }
      // TODO log a warning
   }

   /**
    * @return a token string for use with
    *         {@link com.google.gwt.user.client.History}
    */
   public String toTokenString()
   {
      String token = "";
      boolean first = true;
      for (Map.Entry<String, String> pair : members.entrySet())
      {
         if (pair.getKey() != null && pair.getKey() != "" && pair.getValue() != null && pair.getValue() != "")
         {
            if (first)
               first = false;
            else
               token += PAIR_SEPARATOR;
            token += pair.getKey() + KEY_VALUE_SEPARATOR + pair.getValue();
         }
      }
      return token;
   }

}
