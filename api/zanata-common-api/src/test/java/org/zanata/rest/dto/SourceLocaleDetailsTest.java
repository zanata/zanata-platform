package org.zanata.rest.dto;

import org.junit.Test;
import org.zanata.common.LocaleId;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
public class SourceLocaleDetailsTest {
    @Test
    public void testEmptyConstructor() {
        SourceLocaleDetails details = new SourceLocaleDetails();
        assertThat(details.getDocCount(), equalTo(null));
        assertThat(details.getLocaleDetails(), equalTo(null));
    }

    @Test
    public void testConstructor() {
        LocaleDetails localeDetails =
                new LocaleDetails(LocaleId.DE, "German", "", "", true,
                        false, "", false);
        SourceLocaleDetails details =
                new SourceLocaleDetails(10, localeDetails);

        assertThat(details.getDocCount(), equalTo(10));
        assertThat(details.getLocaleDetails(), equalTo(localeDetails));
    }

    @Test
    public void testGetterSetter() {
        SourceLocaleDetails details = new SourceLocaleDetails();
        details.setDocCount(100);
        assertThat(details.getDocCount(), equalTo(100));

        LocaleDetails localeDetails =
                new LocaleDetails(LocaleId.DE, "German", "", "", true,
                        false, "", false);
        details.setLocaleDetails(localeDetails);
        assertThat(details.getLocaleDetails(), equalTo(localeDetails));

    }

    @Test
    public void testEqualsAndHashCode() {
        LocaleDetails localeDetails =
                new LocaleDetails(LocaleId.DE, "German", "", "", true,
                        false, "", false);
        SourceLocaleDetails details1 =
                new SourceLocaleDetails(10, localeDetails);

        SourceLocaleDetails details2 =
                new SourceLocaleDetails(10, localeDetails);

        assertThat(details1.hashCode(), equalTo(details2.hashCode()));
        assertThat(details1.equals(details2), equalTo(true));

        details2 =
                new SourceLocaleDetails(100, localeDetails);

        assertThat(details1.hashCode(), not(equalTo(details2.hashCode())));
        assertThat(details1.equals(details2), equalTo(false));

    }
}
