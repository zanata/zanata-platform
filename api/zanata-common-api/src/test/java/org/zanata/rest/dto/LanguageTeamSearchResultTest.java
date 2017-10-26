package org.zanata.rest.dto;

import org.junit.Test;
import org.zanata.common.LocaleId;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

/**
 * @author Alex Eng<a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
public class LanguageTeamSearchResultTest {
    @Test
    public void testConstructor() {
        LanguageTeamSearchResult searchResults = new LanguageTeamSearchResult();
        assertThat(searchResults.getType(), equalTo(
                SearchResult.SearchResultType.LanguageTeam));
    }

    @Test
    public void testLocaleDetails() {
        LocaleDetails localeDetails =
                new LocaleDetails(LocaleId.DE, "German", null, null, true, true,
                        null, false);
        LanguageTeamSearchResult searchResults = new LanguageTeamSearchResult();
        searchResults.setLocaleDetails(localeDetails);
        assertThat(searchResults.getLocaleDetails(), equalTo(localeDetails));
    }

    @Test
    public void testMemberCount() {
        long count = 100L;
        LanguageTeamSearchResult searchResults = new LanguageTeamSearchResult();
        searchResults.setMemberCount(count);
        assertThat(searchResults.getMemberCount(), equalTo(count));
    }
}
