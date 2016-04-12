package org.zanata.rest.client;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.ws.rs.core.MediaType;

import org.hamcrest.MatcherAssert;
import org.junit.Test;

import static org.hamcrest.Matchers.equalTo;
import static org.zanata.rest.client.InvalidContentTypeFilter.isContentTypeCompatible;

public class InvalidContentTypeFilterTest {
    // WildFly 8 and 9 have been known to add ";charset=ISO-8859-1" to
    // Content-Type headers.
    private static final String LATIN_1 = "ISO-8859-1";

    private String sampleText =
            "<!DOCTYPE composition PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">\n" +
                    "\n" +
                    "  <html class=\"new-zanata-html\"><head>\n" +
                    "    <meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\" />\n" +
                    "    <title>Zanata: Home</title>\n" +
                    "    <link rel=\"shortcut icon\" href=\"/javax.faces.resource/jars/assets/img/logo/logo.ico\" type=\"image/x-icon\" />\n" +
                    "    <link type=\"text/css\" rel=\"stylesheet\" href=\"/resources/fontello/css/fontello.css\" />\n" +
                    "    <link type=\"text/css\" rel=\"stylesheet\" class=\"user\" href=\"/javax.faces.resource/jars/assets/css/zanata.css\" />\n" +
                    "    <link type=\"text/css\" rel=\"stylesheet\" href=\"/javax.faces.resource/jars/assets/css/style.min.css\" />";

    private String text = "    <title>Zanata: Home</title>  \n";

    @Test
    public void testPatternMatch() {
        Pattern pattern = Pattern.compile(".*<title>(.*)</title>.*",
                Pattern.CASE_INSENSITIVE);

        Matcher matcher = pattern.matcher(sampleText.replaceAll("\\n", " "));

        MatcherAssert.assertThat(matcher.matches(), equalTo(true));
        MatcherAssert.assertThat(matcher.group(1), equalTo("Zanata: Home"));
    }

    @Test
    public void testValidateContentTypes() {

        MatcherAssert.assertThat(
                isContentTypeCompatible(MediaType.TEXT_HTML_TYPE),
                equalTo(false));
        MatcherAssert.assertThat(
                isContentTypeCompatible(
                                MediaType.APPLICATION_FORM_URLENCODED_TYPE),
                equalTo(false));
        MatcherAssert.assertThat(
                isContentTypeCompatible(MediaType.MULTIPART_FORM_DATA_TYPE),
                equalTo(false));

        MatcherAssert.assertThat(
                isContentTypeCompatible(MediaType.APPLICATION_XML_TYPE),
                equalTo(true));
        MatcherAssert.assertThat(
                isContentTypeCompatible(MediaType.APPLICATION_JSON_TYPE),
                equalTo(true));
        MatcherAssert.assertThat(
                isContentTypeCompatible(MediaType.WILDCARD_TYPE),
                equalTo(true));

        MediaType zanataXmlType = new MediaType(
                "application", "vnd.zanata+xml");
        MatcherAssert.assertThat(isContentTypeCompatible(zanataXmlType),
                equalTo(true));
        MatcherAssert.assertThat(isContentTypeCompatible(
                zanataXmlType.withCharset(LATIN_1)),
                equalTo(true));

        MediaType zanataVersionXmlType = new MediaType(
                "application", "vnd.zanata.version+xml");
        MatcherAssert.assertThat(isContentTypeCompatible(zanataVersionXmlType),
                equalTo(true));
        MatcherAssert.assertThat(isContentTypeCompatible(
                zanataVersionXmlType.withCharset(LATIN_1)),
                equalTo(true));

        MatcherAssert.assertThat(
                isContentTypeCompatible(MediaType.TEXT_PLAIN_TYPE),
                equalTo(true));
        MatcherAssert.assertThat(
                isContentTypeCompatible(null),
                equalTo(true));
    }
}
