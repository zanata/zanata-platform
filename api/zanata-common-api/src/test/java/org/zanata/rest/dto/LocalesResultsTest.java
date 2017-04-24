package org.zanata.rest.dto;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;

/**
 * @author Alex Eng<a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
public class LocalesResultsTest {

    @Test
    public void testConstructor() {
        int totalCount = 100;
        List<LanguageTeamSearchResult> searchResults = new ArrayList<>();

        LocalesResults results = new LocalesResults(totalCount, searchResults);
        assertThat(results.getTotalCount(), equalTo(totalCount));
        assertThat(results.getResults(), equalTo(searchResults));
    }

    @Test
    public void testEqualsAndHashCode() {
        int totalCount = 100;
        List<LanguageTeamSearchResult> searchResults = new ArrayList<>();

        LocalesResults results1 = new LocalesResults(totalCount, searchResults);
        LocalesResults results2 = new LocalesResults(totalCount, searchResults);
        assertThat(results1.hashCode(), equalTo(results2.hashCode()));
        assertThat(results1.equals(results2), equalTo(true));

        results2 = new LocalesResults(101, searchResults);
        assertThat(results1.hashCode(), not(equalTo(results2.hashCode())));
        assertThat(results1.equals(results2), equalTo(false));

        results2 = new LocalesResults(totalCount, null);
        assertThat(results1.hashCode(), not(equalTo(results2.hashCode())));
        assertThat(results1.equals(results2), equalTo(false));
    }
}
