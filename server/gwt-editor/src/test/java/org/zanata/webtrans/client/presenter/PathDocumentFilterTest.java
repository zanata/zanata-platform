package org.zanata.webtrans.client.presenter;

import java.util.Date;
import java.util.HashMap;

import org.junit.Before;
import org.junit.Test;
import org.zanata.common.LocaleId;
import org.zanata.rest.dto.stats.ContainerTranslationStatistics;
import org.zanata.webtrans.shared.model.AuditInfo;
import org.zanata.webtrans.shared.model.DocumentId;
import org.zanata.webtrans.shared.model.DocumentInfo;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Patrick Huang <a
 *         href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
public class PathDocumentFilterTest {
    private PathDocumentFilter filter;

    @Before
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

        assertThat(filter.accept(docInfo("a", "/pot/"))).isTrue();
        assertThat(filter.accept(docInfo("b b", "/pot/"))).isTrue();
        assertThat(filter.accept(docInfo("c", "/pot/"))).isTrue();
        assertThat(filter.accept(docInfo("C", "/pot/"))).isTrue();
        assertThat(filter.accept(docInfo("d", "/pot/"))).isTrue();
        assertThat(filter.accept(docInfo("b", "/pot/"))).isFalse();
    }

    @Test
    public void testAcceptWithCaseSensitiveAndNotExactMatch() throws Exception {
        filter.setPattern("a").setCaseSensitive(true);

        assertThat(filter.accept(docInfo("a", "/pot/"))).isTrue();
        assertThat(filter.accept(docInfo("A", "/pot/"))).isFalse();
    }

    @Test
    public void testSetFullText() throws Exception {
        filter.setPattern("/pot/a").setFullText(true);

        assertThat(filter.accept(docInfo("a", "/pot/"))).isTrue();
        assertThat(filter.accept(docInfo("a", ""))).isFalse();
    }

    @Test
    public void alwaysAcceptIfNoPattern() {
        assertThat(filter.accept(docInfo("a", "/pot/"))).isTrue();
    }

    @Test
    public void testSetPatternAgainWillClearPreviousPattern() throws Exception {
        filter.setPattern("a");
        assertThat(filter.accept(docInfo("a", "/pot/"))).isTrue();

        filter.setPattern("b");
        assertThat(filter.accept(docInfo("b", "/pot/"))).isTrue();
        assertThat(filter.accept(docInfo("a", "/pot/"))).isFalse();
    }
}
