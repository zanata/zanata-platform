package org.zanata.tmx;

import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.zanata.tmx.TMXUtil.removeFormattingMarkup;

public class TMXUtilTest {

    @Test
    public void extractPlainTextContent() throws Exception {
        assertThat(
                removeFormattingMarkup("<seg><bpt i=\"1\" x=\"1\">{\\b </bpt>Special<ept i=\"1\">}</ept> text</seg>"),
                equalTo("Special text"));
        assertThat(
                removeFormattingMarkup("<seg><bpt i=\"1\" x=\"1\">{\\cf7 </bpt>Special<ept i=\"1\">}</ept> text</seg>"),
                equalTo("Special text"));
        assertThat(
                removeFormattingMarkup("<seg><bpt i=\"1\" x=\"1\">&lt;B></bpt>Special<ept i=\"1\">&lt;/B></ept> text</seg>"),
                equalTo("Special text"));

        assertThat(
                removeFormattingMarkup("<seg>The <bpt i=\"1\" x=\"1\">&lt;i></bpt><bpt i=\"2\" x=\"2\">&lt;b></bpt>"
                                + "big<ept i=\"2\">&lt;/b></ept> black<ept i=\"1\">&lt;/i></ept> cat.</seg>"),
                equalTo("The big black cat."));

        assertThat(
                removeFormattingMarkup("<seg>The icon <ph x=\"1\">&lt;img src=\"testNode.gif\"/></ph>represents "
                        + "a conditional node.</seg>"),
                equalTo("The icon represents a conditional node."));
    }

    @Test
    public void extractPlainTextContentWithNestedFootnote() throws Exception {
        // note the leading and trailing whitespace
        String segXML =
                " <seg>Elephants<ph type=\"fnote\">{\\cs16\\super \\chftn {\\footnote \\pard\\plain"
                        + "\\s15\\widctlpar \\f4\\fs20"
                        + "{\\cs16\\super \\chftn } <sub>An elephant is a very "
                        + "large animal.</sub>}}</ph> are big.</seg> ";
        assertThat(
                removeFormattingMarkup(segXML),
                equalTo("Elephants are big."));
    }
}
