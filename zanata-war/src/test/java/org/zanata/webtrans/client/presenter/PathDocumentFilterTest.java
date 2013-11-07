package org.zanata.webtrans.client.presenter;

import static org.hamcrest.MatcherAssert.assertThat;

import java.util.Date;
import java.util.HashMap;

import org.hamcrest.Matchers;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.zanata.common.LocaleId;
import org.zanata.rest.dto.stats.ContainerTranslationStatistics;
import org.zanata.webtrans.shared.model.AuditInfo;
import org.zanata.webtrans.shared.model.DocumentId;
import org.zanata.webtrans.shared.model.DocumentInfo;

/**
 * @author Patrick Huang <a
 *         href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@Test(groups = "unit-tests")
public class PathDocumentFilterTest {
    private PathDocumentFilter filter;

    @BeforeMethod
    public void setUp() throws Exception {
        filter = new PathDocumentFilter();
    }

    private static DocumentInfo docInfo(String name, String path) {
        return new DocumentInfo(new DocumentId(1L, ""), name, path,
                LocaleId.EN_US, new ContainerTranslationStatistics(),
                new AuditInfo(new Date(), "Translator"),
                new HashMap<String, String>(), new AuditInfo(new Date(),
                        "last translator"));
    }

    @Test
    public void testAcceptWithCaseInsensitiveAndNotExactMatch()
            throws Exception {
        filter.setPattern("a,b b,   c   , , d");

        assertThat(filter.accept(docInfo("a", "/pot/")), Matchers.equalTo(true));
        assertThat(filter.accept(docInfo("b b", "/pot/")),
                Matchers.equalTo(true));
        assertThat(filter.accept(docInfo("c", "/pot/")), Matchers.equalTo(true));
        assertThat(filter.accept(docInfo("C", "/pot/")), Matchers.equalTo(true));
        assertThat(filter.accept(docInfo("d", "/pot/")), Matchers.equalTo(true));
        assertThat(filter.accept(docInfo("b", "/pot/")),
                Matchers.equalTo(false));
    }

    @Test
    public void testAcceptWithCaseSensitiveAndNotExactMatch() throws Exception {
        filter.setPattern("a").setCaseSensitive(true);

        assertThat(filter.accept(docInfo("a", "/pot/")), Matchers.equalTo(true));
        assertThat(filter.accept(docInfo("A", "/pot/")),
                Matchers.equalTo(false));
    }

    @Test
    public void testSetFullText() throws Exception {
        filter.setPattern("/pot/a").setFullText(true);

        assertThat(filter.accept(docInfo("a", "/pot/")), Matchers.equalTo(true));
        assertThat(filter.accept(docInfo("a", "")), Matchers.equalTo(false));
    }

    @Test
    public void alwaysAcceptIfNoPattern() {
        assertThat(filter.accept(docInfo("a", "/pot/")), Matchers.equalTo(true));
    }

    @Test
    public void testSetPatternAgainWillClearPreviousPattern() throws Exception {
        filter.setPattern("a");
        assertThat(filter.accept(docInfo("a", "/pot/")), Matchers.equalTo(true));

        filter.setPattern("b");
        assertThat(filter.accept(docInfo("b", "/pot/")), Matchers.equalTo(true));
        assertThat(filter.accept(docInfo("a", "/pot/")),
                Matchers.equalTo(false));
    }
}
