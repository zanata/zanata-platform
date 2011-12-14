package org.zanata.webtrans.client.history;

import static org.junit.Assert.*;

import org.junit.Before;
import org.testng.annotations.Test;
import org.zanata.webtrans.client.presenter.AppPresenter;

/**
 * Encapsulates a string token of key-value pairs for GWT history operations.
 * 
 * @author David Mason, damason@redhat.com
 * 
 */
public class HistoryTokenTests
{
   private HistoryToken token;

   @Before
   public void resetToken()
   {
      token = null;
   }

   @Test
   public void constructionSetsDefaults()
   {
      token = new HistoryToken();

      assertEquals("default view should be document list", AppPresenter.Display.MainView.Documents, token.getView());
      assertEquals("default document path should be an empty string", "", token.getDocumentPath());
      assertEquals("default document filter text should be an empty string", "", token.getDocFilterText());
      assertFalse("default document filter exact match flag should be false", token.getDocFilterExact());
   }

   @Test
   public void fromEmptyStringSetsDefaults()
   {
      token = HistoryToken.fromTokenString("");

      assertEquals("default view should be document list", AppPresenter.Display.MainView.Documents, token.getView());
      assertEquals("default document path should be an empty string", "", token.getDocumentPath());
      assertEquals("default document filter text should be an empty string", "", token.getDocFilterText());
      assertFalse("default document filter exact match flag should be false", token.getDocFilterExact());
   }

   @Test
   public void fromNullStringSetsDefaults()
   {
      token = HistoryToken.fromTokenString(null);

      assertEquals("default view should be document list", AppPresenter.Display.MainView.Documents, token.getView());
      assertEquals("default document path should be an empty string", "", token.getDocumentPath());
      assertEquals("default document filter text should be an empty string", "", token.getDocFilterText());
      assertFalse("default document filter exact match flag should be false", token.getDocFilterExact());
   }

   @Test
   public void fromTokenStringSetsValues()
   {
      String tokenString = "doc:some/document;view:doc;filter:myfilter;filtertype:exact";

      token = HistoryToken.fromTokenString(tokenString);

      assertEquals("view should be set from token string", AppPresenter.Display.MainView.Editor, token.getView());
      assertEquals("document path should be set from token string", "some/document", token.getDocumentPath());
      assertEquals("document filter text should be set from token string", "myfilter", token.getDocFilterText());
      assertTrue("document filter exact match flag should be set from token string", token.getDocFilterExact());
   }

   @Test
   public void fromTokenStringParameterOrderIrrelevant()
   {
      String differentOrderTokenString = "filter:myfilter;doc:some/document;filtertype:exact;view:doc";

      token = HistoryToken.fromTokenString(differentOrderTokenString);

      assertEquals("view should be set from any position in token string", AppPresenter.Display.MainView.Editor, token.getView());
      assertEquals("document path should be set from any position in token string", "some/document", token.getDocumentPath());
      assertEquals("document filter text should be set from any position in token string", "myfilter", token.getDocFilterText());
      assertTrue("document filter exact match flag should be set from any position in token string", token.getDocFilterExact());
   }

   @Test
   public void fromTokenStringUnknownTokenKeysIgnored()
   {
      String unknownTokensString = "foo:thing;bar:stuff;moo:whatever;mar:somethingelse";

      token = HistoryToken.fromTokenString(unknownTokensString);

      // should be using defaults as there are no known keys
      assertEquals("unknown keys should be ignored", AppPresenter.Display.MainView.Documents, token.getView());
      assertEquals("unknown keys should be ignored", "", token.getDocumentPath());
      assertEquals("unknown keys should be ignored", "", token.getDocFilterText());
      assertFalse("unknown keys should be ignored", token.getDocFilterExact());
   }

   @Test
   public void getSetView()
   {
      token = new HistoryToken();

      token.setView(AppPresenter.Display.MainView.Editor);
      assertEquals(AppPresenter.Display.MainView.Editor, token.getView());
      token.setView(AppPresenter.Display.MainView.Documents);
      assertEquals(AppPresenter.Display.MainView.Documents, token.getView());

      token.setView(AppPresenter.Display.MainView.Editor);
      token.setView(null);
      assertEquals("view should reset to default if set to null value", AppPresenter.Display.MainView.Documents, token.getView());
   }

   @Test
   public void getSetDocPath()
   {
      token = new HistoryToken();

      token.setDocumentPath("new/document/path");
      assertEquals(token.getDocumentPath(), "new/document/path");

      token.setDocumentPath(null);
      assertEquals("document path should be set to empty string if null is given", "", token.getDocumentPath());

      token.setDocumentPath("random/path");
      token.setDocumentPath("");
      assertEquals("document path can be set to empty string", "", token.getDocumentPath());
   }

   @Test
   public void getSetFilterText()
   {
      token = new HistoryToken();
      token.setDocFilterText("filter/text, more/filter/text, foo");
      assertEquals("filter/text, more/filter/text, foo", token.getDocFilterText());

      token.setDocFilterText(null);
      assertEquals("filter text should be returned as empty string after setting to null", "", token.getDocFilterText());

      token.setDocFilterText("some filter text");
      token.setDocFilterText("");
      assertEquals("filter text can be set to empty string", "", token.getDocFilterText());
   }

   @Test
   public void getSetFilterFlag()
   {
      token = new HistoryToken();
      token.setDocFilterExact(true);
      assertTrue(token.getDocFilterExact());
      token.setDocFilterExact(false);
      assertFalse(token.getDocFilterExact());
   }

   @Test
   public void toTokenStringHasNoDefaults()
   {
      token = new HistoryToken();

      String tokenString = token.toTokenString();

      assertEquals("output token string should not contain default values", 0, tokenString.length());
   }

   @Test
   public void toTokenStringHasCustomValues()
   {
      token = new HistoryToken();
      token.setView(AppPresenter.Display.MainView.Editor);
      token.setDocumentPath("some/document");
      token.setDocFilterText("myfilter");
      token.setDocFilterExact(true);

      String newTokenString = token.toTokenString();

      assertTrue(newTokenString.contains("filter:myfilter"));
      assertTrue(newTokenString.contains("doc:some/document"));
      assertTrue(newTokenString.contains("view:doc"));
      assertTrue(newTokenString.contains("filtertype:exact"));
   }

   @Test
   public void tokenStringRoundTrip()
   {
      token = new HistoryToken();
      token.setView(AppPresenter.Display.MainView.Editor);
      token.setDocumentPath("some/document");
      token.setDocFilterText("myfilter");
      token.setDocFilterExact(true);

      String tokenString = token.toTokenString();

      token = null;
      token = HistoryToken.fromTokenString(tokenString);

      assertEquals("view should survive a round-trip to and from token string", AppPresenter.Display.MainView.Editor, token.getView());
      assertEquals("document path should survive a round-trip to and from token string", "some/document", token.getDocumentPath());
      assertEquals("document filter text should survive a round-trip to and from token string", "myfilter", token.getDocFilterText());
      assertTrue("document filter exact match flag should survive a round-trip to and from token string", token.getDocFilterExact());
   }

}
