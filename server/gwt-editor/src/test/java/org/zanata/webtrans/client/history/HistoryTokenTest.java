package org.zanata.webtrans.client.history;

import org.junit.Before;
import org.junit.Test;
import org.zanata.webtrans.client.presenter.MainView;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Encapsulates a string token of key-value pairs for GWT history operations.
 *
 * @author David Mason, damason@redhat.com
 *
 */
public class HistoryTokenTest {
    private HistoryToken token;

    @Before
    public void setup() {
        token = null;
    }

    @Test
    public void constructionSetsDefaults() {
        token = new HistoryToken();

        assertThat(MainView.Documents)
                .as("default view should be document list")
                .isEqualTo(token.getView());
        assertThat(token.getDocumentPath())
                .as("default document path should be an empty string")
                .isEqualTo("");
        assertThat(token.getDocFilterText())
                .as("default document filter text should be an empty string")
                .isEqualTo("");
        assertThat(token.getDocFilterExact())
                .as("default document filter exact match flag should be false")
                .isFalse();
        assertThat(token.getEditorTextSearch())
                .as("default search text should be null").isNull();
        assertThat(token.getProjectSearchText())
                .as("default project-wide search text should be an empty string")
                .isEqualTo("");
        assertThat(token.getProjectSearchReplacement())
                .as("default project-wide search replacement text should be an empty string")
                .isEqualTo("");
        assertThat(token.getProjectSearchCaseSensitive())
                .as("default project-wide search case sensitive flag should be false")
                .isFalse();
        assertThat(token.getTextFlowId()).isNull();
        assertThat(token.isFilterTranslated()).isFalse();
        assertThat(token.isFilterUntranslated()).isFalse();
        assertThat(token.isFilterFuzzy()).isFalse();
        assertThat(token.isFilterApproved()).isFalse();
        assertThat(token.isFilterRejected()).isFalse();
        assertThat(token.isFilterHasError()).isFalse();
        assertThat(token.isFilterMT()).isFalse();
    }

    @Test
    public void fromEmptyStringSetsDefaults() {
        token = HistoryToken.fromTokenString("");

        assertThat(token.getView())
                .as("default view should be document list")
                .isEqualTo(MainView.Documents);
        assertThat(token.getDocumentPath())
                .as("default document path should be an empty string")
                .isEqualTo("");
        assertThat(token.getDocFilterText())
                .as("default document filter text should be an empty string")
                .isEqualTo("");
        assertThat(token.getDocFilterExact())
                .as("default document filter exact match flag should be false")
                .isFalse();
        assertThat(token.getEditorTextSearch())
                .as("default search text should be null").isNull();
        assertThat(token.getProjectSearchText())
                .as("default project-wide search text should be an empty string")
                .isEqualTo("");
        assertThat(token.getProjectSearchReplacement())
                .as("default project-wide search replacement text should be an empty string")
                .isEqualTo("");
        assertThat(token.getProjectSearchCaseSensitive())
                .as("default project-wide search case sensitive flag should be false")
                .isFalse();
        assertThat(token.getTextFlowId()).isNull();
        assertThat(token.isFilterTranslated()).isFalse();
        assertThat(token.isFilterUntranslated()).isFalse();
        assertThat(token.isFilterFuzzy()).isFalse();
        assertThat(token.isFilterApproved()).isFalse();
        assertThat(token.isFilterRejected()).isFalse();
        assertThat(token.isFilterHasError()).isFalse();
        assertThat(token.isFilterMT()).isFalse();
    }

    @Test
    public void fromNullStringSetsDefaults() {
        token = HistoryToken.fromTokenString(null);

        assertThat(token.getView())
                .as("default view should be document list")
                .isEqualTo(MainView.Documents);
        assertThat(token.getDocumentPath())
                .as("default document path should be an empty string")
                .isEqualTo("");
        assertThat(token.getDocFilterText())
                .as("default document filter text should be an empty string")
                .isEqualTo("");
        assertThat(token.getDocFilterExact())
                .as("default document filter exact match flag should be false")
                .isFalse();
        assertThat(token.getEditorTextSearch())
                .as("default search text should be null").isNull();
        assertThat(token.getProjectSearchText())
                .as("default project-wide search text should be an empty string")
                .isEqualTo("");
        assertThat(token.getProjectSearchReplacement())
                .as("default project-wide search replacement text should be an empty string")
                .isEqualTo("");
        assertThat(token.getProjectSearchCaseSensitive())
                .as("default project-wide search case sensitive flag should be false")
                .isFalse();
        assertThat(token.getTextFlowId()).isNull();
        assertThat(token.isFilterTranslated()).isFalse();
        assertThat(token.isFilterUntranslated()).isFalse();
        assertThat(token.isFilterFuzzy()).isFalse();
        assertThat(token.isFilterApproved()).isFalse();
        assertThat(token.isFilterRejected()).isFalse();
        assertThat(token.isFilterHasError()).isFalse();
        assertThat(token.isFilterMT()).isFalse();
    }

    @Test
    public void fromTokenStringSetsValues() {
        String tokenString =
                "doc:some/document;view:doc;filter:myfilter;filtertype:exact";

        token = HistoryToken.fromTokenString(tokenString);

        assertThat(token.getView())
                .as("view should be set from token string")
                .isEqualTo(MainView.Editor);
        assertThat(token.getDocumentPath())
                .as("document path should be set from token string")
                .isEqualTo("some/document");
        assertThat(token.getDocFilterText())
                .as("document filter text should be set from token string")
                .isEqualTo("myfilter");
        assertThat(token.getDocFilterExact())
                .as("document filter exact match flag should be set from token string")
                .isTrue();
    }

    @Test
    public void fromTokenStringSetsSearchValues() {
        String tokenString =
                "search:searchtext;projectsearch:projectsearchtext;projectsearchreplace:replacementtext;projectsearchcase:sensitive";

        token = HistoryToken.fromTokenString(tokenString);

        assertThat(token.getEditorTextSearch())
                .as("search text should be set from token string")
                .isEqualTo("searchtext");
        assertThat(token.getProjectSearchText())
                .as("project-wide search text should be set from token string")
                .isEqualTo("projectsearchtext");
        assertThat(token.getProjectSearchReplacement())
                .as("project-wide search replacement text should be set from token string")
                .isEqualTo("replacementtext");
        assertThat(token.getProjectSearchCaseSensitive())
                .as("project-wide search case sensitivity should be set from token string")
                .isTrue();
    }

    @Test
    public void fromTokenStringParameterOrderIrrelevant() {
        String differentOrderTokenString =
                "filter:myfilter;doc:some/document;filtertype:exact;view:doc";

        token = HistoryToken.fromTokenString(differentOrderTokenString);

        assertThat(token.getView())
                .as("view should be set from any position in token string")
                .isEqualTo(MainView.Editor);
        assertThat(token.getDocumentPath())
                .as("document path should be set from any position in token string")
                .isEqualTo("some/document");
        assertThat(token.getDocFilterText())
                .as("document filter text should be set from any position in token string")
                .isEqualTo("myfilter");
        assertThat(token.getDocFilterExact())
                .as("document filter exact match flag should be set from any position in token string")
                .isTrue();
    }

    @Test
    public void fromTokenStringParameterOrderIrrelevant2() {
        String differentOrderTokenString =
                "projectsearch:projectsearchtext;search:searchtext;projectsearchcase:sensitive;projectsearchreplace:replacementtext";

        token = HistoryToken.fromTokenString(differentOrderTokenString);

        assertThat(token.getEditorTextSearch())
                .as("search text should be set from any position in token string")
                .isEqualTo("searchtext");
        assertThat(token.getProjectSearchText())
                .as("project-wide search text should be set from any position in token string")
                .isEqualTo("projectsearchtext");
        assertThat(token.getProjectSearchReplacement())
                .as("project-wide search replacement text should be set from any position in token string")
                .isEqualTo("replacementtext");
        assertThat(token.getProjectSearchCaseSensitive())
                .as("project-wide search case sensitivity should be set from any position in token string")
                .isTrue();
    }

    @Test
    public void fromTokenStringHasTextFlowId() {
        String tokenString = "textflow:1";

        token = HistoryToken.fromTokenString(tokenString);

        assertThat(token.getTextFlowId()).isEqualTo(1L);
    }

    @Test
    public void badTextFlowId() {
        String tokenString = "textflow:abc";

        token = HistoryToken.fromTokenString(tokenString);

        assertThat(token.getTextFlowId()).isNull();
    }

    @Test
    public void fromTokenStringUnknownTokenKeysIgnored() {
        String unknownTokensString =
                "foo:thing;bar:stuff;moo:whatever;mar:somethingelse";

        token = HistoryToken.fromTokenString(unknownTokensString);

        // should be using defaults as there are no known keys
        assertThat(token.getView())
                .as("unknown keys should be ignored")
                .isEqualTo(MainView.Documents);
        assertThat(token.getDocumentPath())
                .as("unknown keys should be ignored")
                .isEqualTo("");
        assertThat(token.getDocFilterText())
                .as("unknown keys should be ignored")
                .isEqualTo("");
        assertThat(token.getDocFilterExact())
                .as("unknown keys should be ignored")
                .isFalse();
        assertThat(token.getEditorTextSearch())
                .as("unknown keys should be ignored").isNull();
        assertThat(token.getProjectSearchText())
                .as("unknown keys should be ignored")
                .isEqualTo("");
        assertThat(token.getProjectSearchReplacement())
                .as("unknown keys should be ignored")
                .isEqualTo("");
        assertThat(token.getProjectSearchCaseSensitive())
                .as("unknown keys should be ignored")
                .isFalse();
    }

    @Test
    public void getSetView() {
        token = new HistoryToken();

        token.setView(MainView.Editor);
        assertThat(token.getView()).isEqualTo(MainView.Editor);
        token.setView(MainView.Documents);
        assertThat(token.getView()).isEqualTo(MainView.Documents);

        token.setView(MainView.Editor);
        token.setView(null);
        assertThat(token.getView())
                .as("view should reset to default if set to null value")
                .isEqualTo(MainView.Documents);
    }

    @Test
    public void getSetDocPath() {
        token = new HistoryToken();

        token.setDocumentPath("new/document/path");
        assertThat(token.getDocumentPath()).isEqualTo("new/document/path");

        token.setDocumentPath(null);
        assertThat(token.getDocumentPath())
                .as("document path should be set to empty string if null is given")
                .isEqualTo("");

        token.setDocumentPath("random/path");
        token.setDocumentPath("");
        assertThat(token.getDocumentPath())
                .as("document path can be set to empty string")
                .isEqualTo("");
    }

    @Test
    public void getSetFilterText() {
        token = new HistoryToken();
        token.setDocFilterText("filter/text, more/filter/text, foo");
        assertThat(token.getDocFilterText())
                .isEqualTo("filter/text, more/filter/text, foo");

        token.setDocFilterText(null);
        assertThat(token.getDocFilterText())
                .as("filter text should be returned as empty string after setting to null")
                .isEqualTo("");

        token.setDocFilterText("some filter text");
        token.setDocFilterText("");
        assertThat(token.getDocFilterText())
                .as("filter text can be set to empty string")
                .isEqualTo("");
    }

    @Test
    public void getSetFilterFlag() {
        token = new HistoryToken();
        token.setDocFilterExact(true);
        assertThat(token.getDocFilterExact()).isTrue();
        token.setDocFilterExact(false);
        assertThat(token.getDocFilterExact()).isFalse();
    }

    @Test
    public void getSetSearchText() {
        token = new HistoryToken();
        token.setEditorTextSearch("some search text");
        assertThat(token.getEditorTextSearch()).isEqualTo("some search text");
        token.setEditorTextSearch(null);
        assertThat(token.getEditorTextSearch())
                .as("search text should be returned null after setting to null")
                .isNull();

        token.setEditorTextSearch("text to be discarded");
        token.setEditorTextSearch("");
        assertThat(token.getEditorTextSearch())
                .as("empty search text is treated as null").isNull();
    }

    @Test
    public void getSetProjectSearchText() {
        token = new HistoryToken();
        token.setProjectSearchText("some project search text");
        assertThat(token.getProjectSearchText())
                .isEqualTo("some project search text");

        token.setProjectSearchText(null);
        assertThat(token.getProjectSearchText())
                .as("project search text should be returned as empty string after setting to null")
                .isEqualTo("");

        token.setProjectSearchText("text to be discarded");
        token.setProjectSearchText("");
        assertThat(token.getProjectSearchText())
                .as("project search text can be set to empty string")
                .isEqualTo("");
    }

    @Test
    public void getSetProjectSearchReplacement() {
        token = new HistoryToken();
        token.setProjectSearchReplacement("some project search replacement text");
        assertThat(token.getProjectSearchReplacement()).isEqualTo("some project search replacement text");

        token.setProjectSearchReplacement(null);
        assertThat(token.getProjectSearchReplacement())
                .as("project search replacement text should be returned as empty string after setting to null")
                .isEqualTo("");

        token.setProjectSearchReplacement("text to be discarded");
        token.setProjectSearchReplacement("");
        assertThat(token.getProjectSearchReplacement())
                .as("project search replacement text can be set to empty string")
                .isEqualTo("");
    }

    @Test
    public void getSetProjectSearchCaseSensitive() {
        token = new HistoryToken();
        token.setProjectSearchCaseSensitive(true);
        assertThat(token.getProjectSearchCaseSensitive()).isTrue();
        token.setProjectSearchCaseSensitive(false);
        assertThat(token.getProjectSearchCaseSensitive()).isFalse();
    }

    @Test
    public void encodesColon() {
        token = new HistoryToken();
        token.setProjectSearchText("test:test");
        assertThat(token.toTokenString())
                .as("Colons should be replaced with \"!c\" in the token string")
                .isEqualTo("projectsearch:test!ctest");
    }

    @Test
    public void encodesSemicolon() {
        token = new HistoryToken();
        token.setProjectSearchText("test;test");
        assertThat(token.toTokenString())
                .as("Semicolons should be replaced with \"!s\" in the token string")
                .isEqualTo("projectsearch:test!stest");
    }

    @Test
    public void toTokenStringHasNoDefaults() {
        token = new HistoryToken();

        String tokenString = token.toTokenString();

        assertThat(tokenString.length())
                .as("output token string should not contain default values")
                .isEqualTo(0);
    }

    @Test
    public void toTokenStringHasCustomValues() {
        token = new HistoryToken();
        token.setView(MainView.Editor);
        token.setDocumentPath("some/document");
        token.setDocFilterText("myfilter");
        token.setDocFilterExact(true);
        token.setEditorTextSearch("searchtext");
        token.setProjectSearchText("projectsearchtext");
        token.setProjectSearchReplacement("replacementtext");
        token.setProjectSearchCaseSensitive(true);

        String newTokenString = token.toTokenString();

        assertThat(newTokenString.contains("filter:myfilter")).isTrue();
        assertThat(newTokenString.contains("doc:some/document")).isTrue();
        assertThat(newTokenString.contains("view:doc")).isTrue();
        assertThat(newTokenString.contains("filtertype:exact")).isTrue();
        assertThat(newTokenString.contains("search:searchtext")).isTrue();
        assertThat(newTokenString.contains("projectsearch:projectsearchtext"))
                .isTrue();
        assertThat(newTokenString
                .contains("projectsearchreplace:replacementtext")).isTrue();
        assertThat(newTokenString.contains("projectsearchcase:sensitive"))
                .isTrue();
    }

    @Test
    public void tokenStringRoundTrip() {
        token = new HistoryToken();
        token.setView(MainView.Editor);
        token.setDocumentPath("some/document");
        token.setDocFilterText("myfilter");
        token.setDocFilterExact(true);
        token.setEditorTextSearch("searchtext");
        token.setProjectSearchText("projectsearchtext");
        token.setProjectSearchReplacement("replacementtext");
        token.setProjectSearchCaseSensitive(true);

        String tokenString = token.toTokenString();

        token = null;
        token = HistoryToken.fromTokenString(tokenString);

        assertThat(token.getView())
                .as("view should survive a round-trip to and from token string")
                .isEqualTo(MainView.Editor);
        assertThat(token.getDocumentPath())
                .as("document path should survive a round-trip to and from token string")
                .isEqualTo("some/document");
        assertThat(token.getDocFilterText())
                .as("document filter text should survive a round-trip to and from token string")
                .isEqualTo("myfilter");
        assertThat(token.getDocFilterExact())
                .as("document filter exact match flag should survive a round-trip to and from token string")
                .isTrue();
        assertThat(token.getEditorTextSearch())
                .as("search text should survive a round-trip to and from token string")
                .isEqualTo("searchtext");
        assertThat(token.getProjectSearchText())
                .as("project-wide search text should survive a round-trip to and from token string")
                .isEqualTo("projectsearchtext");
        assertThat(token.getProjectSearchReplacement())
                .as("project-wide search replacement text should survive a round-trip to and from token string")
                .isEqualTo("replacementtext");
        assertThat(token.getProjectSearchCaseSensitive())
                .as("project-wide search case sensitivity should survive a round-trip to and from token string")
                .isTrue();
    }

    @Test
    public void tokenStringRoundTripWithEncodedCharacters() {
        token = new HistoryToken();
        token.setDocumentPath("some:document;with!encodedchars");
        token.setDocFilterText("my!fil:ter;");
        token.setEditorTextSearch(":search!text;");
        token.setProjectSearchText("project:search;text!");
        token.setProjectSearchReplacement("re!place;ment:text");

        String tokenString = token.toTokenString();

        token = null;
        token = HistoryToken.fromTokenString(tokenString);

        assertThat(token.getDocumentPath())
                .as("encodable characters in document path should survive a round-trip to and from token string")
                .isEqualTo("some:document;with!encodedchars");

        assertThat(token.getDocFilterText())
                .as("encodable characters in document filter text should survive a round-trip to and from token string")
                .isEqualTo("my!fil:ter;");
        assertThat(token.getEditorTextSearch())
                .as("encodable characters in search text should survive a round-trip to and from token string")
                .isEqualTo(":search!text;");
        assertThat(token.getProjectSearchText())
                .as("encodable characters in project-wide search text should survive a round-trip to and from token string")
                .isEqualTo("project:search;text!");
        assertThat(token.getProjectSearchReplacement())
                .as("encodable characters in project-wide search replacement text should survive a round-trip to and from token string")
                .isEqualTo("re!place;ment:text");
    }

    @Test
    public void tokenStringHasFilterOption() {
        token = new HistoryToken();
        token.setFilterFuzzy(true);
        token.setFilterTranslated(true);

        String tokenString = token.toTokenString();
        assertThat(tokenString).contains("fuzzy:show", "translated:show");
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
        token.setFilterMT(true);

        String tokenString = token.toTokenString();

        assertThat(tokenString).isEqualTo("");
    }

}
