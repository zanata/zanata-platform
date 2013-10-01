package org.zanata.webtrans.client.history;

import static org.junit.Assert.*;

import org.hamcrest.Matchers;
import org.junit.Before;
import org.testng.annotations.Test;
import org.zanata.webtrans.client.presenter.MainView;

/**
 * Encapsulates a string token of key-value pairs for GWT history operations.
 *
 * @author David Mason, damason@redhat.com
 *
 */
@Test(groups = { "unit-tests" })
public class HistoryTokenTests {
    private HistoryToken token;

    @Before
    public void resetToken() {
        token = null;
    }

    @Test
    public void constructionSetsDefaults() {
        token = new HistoryToken();

        assertEquals("default view should be document list",
                MainView.Documents, token.getView());
        assertEquals("default document path should be an empty string", "",
                token.getDocumentPath());
        assertEquals("default document filter text should be an empty string",
                "", token.getDocFilterText());
        assertFalse("default document filter exact match flag should be false",
                token.getDocFilterExact());
        assertEquals("default search text should be an empty string", "",
                token.getSearchText());
        assertEquals(
                "default project-wide search text should be an empty string",
                "", token.getProjectSearchText());
        assertEquals(
                "default project-wide search replacement text should be an empty string",
                "", token.getProjectSearchReplacement());
        assertFalse(
                "default project-wide search case sensitive flag should be false",
                token.getProjectSearchCaseSensitive());
        assertThat(token.getTextFlowId(), Matchers.nullValue());
        assertThat(token.isFilterTranslated(), Matchers.equalTo(false));
        assertThat(token.isFilterUntranslated(), Matchers.equalTo(false));
        assertThat(token.isFilterFuzzy(), Matchers.equalTo(false));
        assertThat(token.isFilterApproved(), Matchers.equalTo(false));
        assertThat(token.isFilterRejected(), Matchers.equalTo(false));
        assertThat(token.isFilterHasError(), Matchers.equalTo(false));
    }

    @Test
    public void fromEmptyStringSetsDefaults() {
        token = HistoryToken.fromTokenString("");

        assertEquals("default view should be document list",
                MainView.Documents, token.getView());
        assertEquals("default document path should be an empty string", "",
                token.getDocumentPath());
        assertEquals("default document filter text should be an empty string",
                "", token.getDocFilterText());
        assertFalse("default document filter exact match flag should be false",
                token.getDocFilterExact());
        assertEquals("default search text should be an empty string", "",
                token.getSearchText());
        assertEquals(
                "default project-wide search text should be an empty string",
                "", token.getProjectSearchText());
        assertEquals(
                "default project-wide search replacement text should be an empty string",
                "", token.getProjectSearchReplacement());
        assertFalse(
                "default project-wide search case sensitive flag should be false",
                token.getProjectSearchCaseSensitive());
        assertThat(token.getTextFlowId(), Matchers.nullValue());
        assertThat(token.isFilterTranslated(), Matchers.equalTo(false));
        assertThat(token.isFilterUntranslated(), Matchers.equalTo(false));
        assertThat(token.isFilterFuzzy(), Matchers.equalTo(false));
        assertThat(token.isFilterApproved(), Matchers.equalTo(false));
        assertThat(token.isFilterRejected(), Matchers.equalTo(false));
        assertThat(token.isFilterHasError(), Matchers.equalTo(false));
    }

    @Test
    public void fromNullStringSetsDefaults() {
        token = HistoryToken.fromTokenString(null);

        assertEquals("default view should be document list",
                MainView.Documents, token.getView());
        assertEquals("default document path should be an empty string", "",
                token.getDocumentPath());
        assertEquals("default document filter text should be an empty string",
                "", token.getDocFilterText());
        assertFalse("default document filter exact match flag should be false",
                token.getDocFilterExact());
        assertEquals("default search text should be an empty string", "",
                token.getSearchText());
        assertEquals(
                "default project-wide search text should be an empty string",
                "", token.getProjectSearchText());
        assertEquals(
                "default project-wide search replacement text should be an empty string",
                "", token.getProjectSearchReplacement());
        assertFalse(
                "default project-wide search case sensitive flag should be false",
                token.getProjectSearchCaseSensitive());
        assertThat(token.getTextFlowId(), Matchers.nullValue());
        assertThat(token.isFilterTranslated(), Matchers.equalTo(false));
        assertThat(token.isFilterUntranslated(), Matchers.equalTo(false));
        assertThat(token.isFilterFuzzy(), Matchers.equalTo(false));
        assertThat(token.isFilterApproved(), Matchers.equalTo(false));
        assertThat(token.isFilterRejected(), Matchers.equalTo(false));
        assertThat(token.isFilterHasError(), Matchers.equalTo(false));
    }

    @Test
    public void fromTokenStringSetsValues() {
        String tokenString =
                "doc:some/document;view:doc;filter:myfilter;filtertype:exact";

        token = HistoryToken.fromTokenString(tokenString);

        assertEquals("view should be set from token string", MainView.Editor,
                token.getView());
        assertEquals("document path should be set from token string",
                "some/document", token.getDocumentPath());
        assertEquals("document filter text should be set from token string",
                "myfilter", token.getDocFilterText());
        assertTrue(
                "document filter exact match flag should be set from token string",
                token.getDocFilterExact());
    }

    @Test
    public void fromTokenStringSetsSearchValues() {
        String tokenString =
                "search:searchtext;projectsearch:projectsearchtext;projectsearchreplace:replacementtext;projectsearchcase:sensitive";

        token = HistoryToken.fromTokenString(tokenString);

        assertEquals("search text should be set from token string",
                "searchtext", token.getSearchText());
        assertEquals(
                "project-wide search text should be set from token string",
                "projectsearchtext", token.getProjectSearchText());
        assertEquals(
                "project-wide search replacement text should be set from token string",
                "replacementtext", token.getProjectSearchReplacement());
        assertTrue(
                "project-wide search case sensitivity should be set from token string",
                token.getProjectSearchCaseSensitive());
    }

    @Test
    public void fromTokenStringParameterOrderIrrelevant() {
        String differentOrderTokenString =
                "filter:myfilter;doc:some/document;filtertype:exact;view:doc";

        token = HistoryToken.fromTokenString(differentOrderTokenString);

        assertEquals("view should be set from any position in token string",
                MainView.Editor, token.getView());
        assertEquals(
                "document path should be set from any position in token string",
                "some/document", token.getDocumentPath());
        assertEquals(
                "document filter text should be set from any position in token string",
                "myfilter", token.getDocFilterText());
        assertTrue(
                "document filter exact match flag should be set from any position in token string",
                token.getDocFilterExact());
    }

    @Test
    public void fromTokenStringParameterOrderIrrelevant2() {
        String differentOrderTokenString =
                "projectsearch:projectsearchtext;search:searchtext;projectsearchcase:sensitive;projectsearchreplace:replacementtext";

        token = HistoryToken.fromTokenString(differentOrderTokenString);

        assertEquals(
                "search text should be set from any position in token string",
                "searchtext", token.getSearchText());
        assertEquals(
                "project-wide search text should be set from any position in token string",
                "projectsearchtext", token.getProjectSearchText());
        assertEquals(
                "project-wide search replacement text should be set from any position in token string",
                "replacementtext", token.getProjectSearchReplacement());
        assertTrue(
                "project-wide search case sensitivity should be set from any position in token string",
                token.getProjectSearchCaseSensitive());
    }

    @Test
    public void fromTokenStringHasTextFlowId() {
        String tokenString = "textflow:1";

        token = HistoryToken.fromTokenString(tokenString);

        assertThat(token.getTextFlowId(), Matchers.equalTo(1L));
    }

    @Test
    public void badTextFlowId() {
        String tokenString = "textflow:abc";

        token = HistoryToken.fromTokenString(tokenString);

        assertThat(token.getTextFlowId(), Matchers.nullValue());
    }

    @Test
    public void fromTokenStringUnknownTokenKeysIgnored() {
        String unknownTokensString =
                "foo:thing;bar:stuff;moo:whatever;mar:somethingelse";

        token = HistoryToken.fromTokenString(unknownTokensString);

        // should be using defaults as there are no known keys
        assertEquals("unknown keys should be ignored", MainView.Documents,
                token.getView());
        assertEquals("unknown keys should be ignored", "",
                token.getDocumentPath());
        assertEquals("unknown keys should be ignored", "",
                token.getDocFilterText());
        assertFalse("unknown keys should be ignored", token.getDocFilterExact());
        assertEquals("unknown keys should be ignored", "",
                token.getSearchText());
        assertEquals("unknown keys should be ignored", "",
                token.getProjectSearchText());
        assertEquals("unknown keys should be ignored", "",
                token.getProjectSearchReplacement());
        assertFalse("unknown keys should be ignored",
                token.getProjectSearchCaseSensitive());
    }

    @Test
    public void getSetView() {
        token = new HistoryToken();

        token.setView(MainView.Editor);
        assertEquals(MainView.Editor, token.getView());
        token.setView(MainView.Documents);
        assertEquals(MainView.Documents, token.getView());

        token.setView(MainView.Editor);
        token.setView(null);
        assertEquals("view should reset to default if set to null value",
                MainView.Documents, token.getView());
    }

    @Test
    public void getSetDocPath() {
        token = new HistoryToken();

        token.setDocumentPath("new/document/path");
        assertEquals(token.getDocumentPath(), "new/document/path");

        token.setDocumentPath(null);
        assertEquals(
                "document path should be set to empty string if null is given",
                "", token.getDocumentPath());

        token.setDocumentPath("random/path");
        token.setDocumentPath("");
        assertEquals("document path can be set to empty string", "",
                token.getDocumentPath());
    }

    @Test
    public void getSetFilterText() {
        token = new HistoryToken();
        token.setDocFilterText("filter/text, more/filter/text, foo");
        assertEquals("filter/text, more/filter/text, foo",
                token.getDocFilterText());

        token.setDocFilterText(null);
        assertEquals(
                "filter text should be returned as empty string after setting to null",
                "", token.getDocFilterText());

        token.setDocFilterText("some filter text");
        token.setDocFilterText("");
        assertEquals("filter text can be set to empty string", "",
                token.getDocFilterText());
    }

    @Test
    public void getSetFilterFlag() {
        token = new HistoryToken();
        token.setDocFilterExact(true);
        assertTrue(token.getDocFilterExact());
        token.setDocFilterExact(false);
        assertFalse(token.getDocFilterExact());
    }

    @Test
    public void getSetSearchText() {
        token = new HistoryToken();
        token.setSearchText("some search text");
        assertEquals("some search text", token.getSearchText());
        token.setSearchText(null);
        assertEquals(
                "search text should be returned as empty string after setting to null",
                "", token.getSearchText());

        token.setSearchText("text to be discarded");
        token.setSearchText("");
        assertEquals("search text can be set to empty string", "",
                token.getSearchText());
    }

    @Test
    public void getSetProjectSearchText() {
        token = new HistoryToken();
        token.setProjectSearchText("some project search text");
        assertEquals("some project search text", token.getProjectSearchText());

        token.setProjectSearchText(null);
        assertEquals(
                "project search text should be returned as empty string after setting to null",
                "", token.getProjectSearchText());

        token.setProjectSearchText("text to be discarded");
        token.setProjectSearchText("");
        assertEquals("project search text can be set to empty string", "",
                token.getProjectSearchText());
    }

    @Test
    public void getSetProjectSearchReplacement() {
        token = new HistoryToken();
        token.setProjectSearchReplacement("some project search replacement text");
        assertEquals("some project search replacement text",
                token.getProjectSearchReplacement());

        token.setProjectSearchReplacement(null);
        assertEquals(
                "project search replacement text should be returned as empty string after setting to null",
                "", token.getProjectSearchReplacement());

        token.setProjectSearchReplacement("text to be discarded");
        token.setProjectSearchReplacement("");
        assertEquals(
                "project search replacement text can be set to empty string",
                "", token.getProjectSearchReplacement());
    }

    @Test
    public void getSetProjectSearchCaseSensitive() {
        token = new HistoryToken();
        token.setProjectSearchCaseSensitive(true);
        assertTrue(token.getProjectSearchCaseSensitive());
        token.setProjectSearchCaseSensitive(false);
        assertFalse(token.getProjectSearchCaseSensitive());
    }

    @Test
    public void encodesColon() {
        token = new HistoryToken();
        token.setProjectSearchText("test:test");
        assertEquals(
                "Colons should be replaced with \"!c\" in the token string",
                "projectsearch:test!ctest", token.toTokenString());
    }

    @Test
    public void encodesSemicolon() {
        token = new HistoryToken();
        token.setProjectSearchText("test;test");
        assertEquals(
                "Semicolons should be replaced with \"!s\" in the token string",
                "projectsearch:test!stest", token.toTokenString());
    }

    @Test
    public void toTokenStringHasNoDefaults() {
        token = new HistoryToken();

        String tokenString = token.toTokenString();

        assertEquals("output token string should not contain default values",
                0, tokenString.length());
    }

    @Test
    public void toTokenStringHasCustomValues() {
        token = new HistoryToken();
        token.setView(MainView.Editor);
        token.setDocumentPath("some/document");
        token.setDocFilterText("myfilter");
        token.setDocFilterExact(true);
        token.setSearchText("searchtext");
        token.setProjectSearchText("projectsearchtext");
        token.setProjectSearchReplacement("replacementtext");
        token.setProjectSearchCaseSensitive(true);

        String newTokenString = token.toTokenString();

        assertTrue(newTokenString.contains("filter:myfilter"));
        assertTrue(newTokenString.contains("doc:some/document"));
        assertTrue(newTokenString.contains("view:doc"));
        assertTrue(newTokenString.contains("filtertype:exact"));
        assertTrue(newTokenString.contains("search:searchtext"));
        assertTrue(newTokenString.contains("projectsearch:projectsearchtext"));
        assertTrue(newTokenString
                .contains("projectsearchreplace:replacementtext"));
        assertTrue(newTokenString.contains("projectsearchcase:sensitive"));
    }

    @Test
    public void tokenStringRoundTrip() {
        token = new HistoryToken();
        token.setView(MainView.Editor);
        token.setDocumentPath("some/document");
        token.setDocFilterText("myfilter");
        token.setDocFilterExact(true);
        token.setSearchText("searchtext");
        token.setProjectSearchText("projectsearchtext");
        token.setProjectSearchReplacement("replacementtext");
        token.setProjectSearchCaseSensitive(true);

        String tokenString = token.toTokenString();

        token = null;
        token = HistoryToken.fromTokenString(tokenString);

        assertEquals(
                "view should survive a round-trip to and from token string",
                MainView.Editor, token.getView());
        assertEquals(
                "document path should survive a round-trip to and from token string",
                "some/document", token.getDocumentPath());
        assertEquals(
                "document filter text should survive a round-trip to and from token string",
                "myfilter", token.getDocFilterText());
        assertTrue(
                "document filter exact match flag should survive a round-trip to and from token string",
                token.getDocFilterExact());
        assertEquals(
                "search text should survive a round-trip to and from token string",
                "searchtext", token.getSearchText());
        assertEquals(
                "project-wide search text should survive a round-trip to and from token string",
                "projectsearchtext", token.getProjectSearchText());
        assertEquals(
                "project-wide search replacement text should survive a round-trip to and from token string",
                "replacementtext", token.getProjectSearchReplacement());
        assertTrue(
                "project-wide search case sensitivity should survive a round-trip to and from token string",
                token.getProjectSearchCaseSensitive());
    }

    @Test
    public void tokenStringRoundTripWithEncodedCharacters() {
        token = new HistoryToken();
        token.setDocumentPath("some:document;with!encodedchars");
        token.setDocFilterText("my!fil:ter;");
        token.setSearchText(":search!text;");
        token.setProjectSearchText("project:search;text!");
        token.setProjectSearchReplacement("re!place;ment:text");

        String tokenString = token.toTokenString();

        token = null;
        token = HistoryToken.fromTokenString(tokenString);

        assertEquals(
                "encodable characters in document path should survive a round-trip to and from token string",
                "some:document;with!encodedchars", token.getDocumentPath());
        assertEquals(
                "encodable characters in document filter text should survive a round-trip to and from token string",
                "my!fil:ter;", token.getDocFilterText());
        assertEquals(
                "encodable characters in search text should survive a round-trip to and from token string",
                ":search!text;", token.getSearchText());
        assertEquals(
                "encodable characters in project-wide search text should survive a round-trip to and from token string",
                "project:search;text!", token.getProjectSearchText());
        assertEquals(
                "encodable characters in project-wide search replacement text should survive a round-trip to and from token string",
                "re!place;ment:text", token.getProjectSearchReplacement());
    }

    @Test
    public void tokenStringHasFilterOption() {
        token = new HistoryToken();
        token.setFilterFuzzy(true);
        token.setFilterTranslated(true);

        String tokenString = token.toTokenString();

        assertThat(tokenString, Matchers.containsString("fuzzy:show"));
        assertThat(tokenString, Matchers.containsString("translated:show"));
    }

    @Test
    public void tokenStringHasAllFilterOption() {
        token = new HistoryToken();
        token.setFilterFuzzy(true);
        token.setFilterTranslated(true);
        token.setFilterUntranslated(true);
        token.setFilterApproved(true);
        token.setFilterRejected(true);
        token.setFilterHasError(true);

        String tokenString = token.toTokenString();

        assertThat(tokenString, Matchers.equalTo(""));
    }

}
