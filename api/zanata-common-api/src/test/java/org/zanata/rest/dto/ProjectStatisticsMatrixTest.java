package org.zanata.rest.dto;

import org.junit.Test;
import org.zanata.common.ContentState;
import org.zanata.common.LocaleId;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.core.Is.is;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
public class ProjectStatisticsMatrixTest {

    @Test
    public void testEqualsAndHashcode() {
        ProjectStatisticsMatrix matrix = new ProjectStatisticsMatrix("today", LocaleId.DE, "German",
                ContentState.Approved, 10);

        ProjectStatisticsMatrix other = new ProjectStatisticsMatrix("today", LocaleId.DE, "German",
                ContentState.Approved, 10);
        assertThat(matrix.equals(other), is(true));
        assertThat(matrix.hashCode(), equalTo(other.hashCode()));

        other = new ProjectStatisticsMatrix("not today", LocaleId.DE, "German",
                ContentState.Approved, 10);
        assertThat(matrix.equals(other), is(false));
        assertThat(matrix.hashCode(), not(equalTo(other.hashCode())));

        other = new ProjectStatisticsMatrix("today", LocaleId.FR, "German",
                ContentState.Approved, 10);
        assertThat(matrix.equals(other), is(false));
        assertThat(matrix.hashCode(), not(equalTo(other.hashCode())));

        other = new ProjectStatisticsMatrix("today", LocaleId.DE, "not german",
                ContentState.Approved, 10);
        assertThat(matrix.equals(other), is(false));
        assertThat(matrix.hashCode(), not(equalTo(other.hashCode())));

        other = new ProjectStatisticsMatrix("today", LocaleId.DE, "German",
                ContentState.NeedReview, 10);
        assertThat(matrix.equals(other), is(false));
        assertThat(matrix.hashCode(), not(equalTo(other.hashCode())));

        other = new ProjectStatisticsMatrix("today", LocaleId.DE, "German",
                ContentState.Approved, 11);
        assertThat(matrix.equals(other), is(false));
        assertThat(matrix.hashCode(), not(equalTo(other.hashCode())));
    }

    @Test
    public void testSetter() {
        ProjectStatisticsMatrix matrix = new ProjectStatisticsMatrix("today", LocaleId.DE, "German",
                ContentState.Approved, 10);
        matrix.setLocaleDisplayName("new display name");
        assertThat(matrix.getLocaleDisplayName(), equalTo("new display name"));

        matrix.setLocaleId(LocaleId.FR);
        assertThat(matrix.getLocaleId(), equalTo(LocaleId.FR));

        matrix.setSavedDate("saved date");
        assertThat(matrix.getSavedDate(), equalTo("saved date"));

        matrix.setSavedState(ContentState.NeedReview);
        assertThat(matrix.getSavedState(), equalTo(ContentState.NeedReview));

        matrix.setWordCount(99);
        assertThat(matrix.getWordCount(), equalTo(99L));
    }

    @Test
    public void testToString() {
        ProjectStatisticsMatrix matrix = new ProjectStatisticsMatrix("today", LocaleId.DE, "German",
                ContentState.Approved, 10);
        String string = matrix.toString();
        assertThat(string, containsString("today"));
        assertThat(string, containsString("German"));
        assertThat(string, containsString(LocaleId.DE.toString()));
        assertThat(string, containsString(ContentState.Approved.toString()));
        assertThat(string, containsString("10"));
    }
}
