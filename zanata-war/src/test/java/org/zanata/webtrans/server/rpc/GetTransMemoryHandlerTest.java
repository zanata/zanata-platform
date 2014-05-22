package org.zanata.webtrans.server.rpc;

import java.util.List;

import org.apache.lucene.queryParser.QueryParser;
import org.hamcrest.Matchers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.zanata.common.LocaleId;
import org.zanata.seam.SeamAutowire;
import org.zanata.security.ZanataIdentity;
import org.zanata.service.TranslationMemoryService;
import org.zanata.webtrans.shared.model.TransMemoryQuery;
import org.zanata.webtrans.shared.model.TransMemoryResultItem;
import org.zanata.webtrans.shared.rpc.GetTranslationMemory;
import org.zanata.webtrans.shared.rpc.GetTranslationMemoryResult;
import org.zanata.webtrans.shared.rpc.HasSearchType;
import com.google.common.collect.Lists;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Patrick Huang <a
 *         href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@Test(groups = "jpa-tests")
public class GetTransMemoryHandlerTest {
    private GetTransMemoryHandler handler;

    @Mock
    private ZanataIdentity identity;
    @Mock
    private TranslationMemoryService translationMemoryService;

    private LocaleId targetLocaleId = new LocaleId("ja");
    private LocaleId sourceLocaleId = LocaleId.EN_US;

    private SeamAutowire seam = SeamAutowire.instance();

    @BeforeMethod
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        handler =
                seam.reset()
                        .use("identity", identity)
                        .use("translationMemoryServiceImpl",
                                translationMemoryService).ignoreNonResolvable()
                        .autowire(GetTransMemoryHandler.class);

        // @formatter:off
    }

    @Test
    public void testExecute() throws Exception {
        // Given: hibernate search can not parse query
        TransMemoryQuery query =
                new TransMemoryQuery("file removed",
                        HasSearchType.SearchType.FUZZY);

        List<TransMemoryResultItem> dummyResults = Lists.newArrayList();

        when(translationMemoryService.searchTransMemory(targetLocaleId, sourceLocaleId, query)).thenReturn(dummyResults);

        GetTranslationMemory action =
                new GetTranslationMemory(query, targetLocaleId, sourceLocaleId);

        // When:
        GetTranslationMemoryResult result = handler.execute(action, null);

        // Then:
        verify(identity).checkLoggedIn();
        assertThat(result.getMemories(), Matchers.hasSize(0));
    }

    @Test
    public void testRollback() throws Exception {
        handler.rollback(null, null, null);
    }

    // list of special chars taken from
    // http://lucene.apache.org/java/2_4_1/queryparsersyntax.html#Escaping%20Special%20Characters
    @Test
    public void testLuceneQuery() {
        assertThat(QueryParser.escape("plaintext"), is("plaintext"));
        assertThat(
                QueryParser.escape("lucene special characters + - && || ! ( ) "
                        + "{ } [ ] ^ \" ~ * ? : \\ plus % _"),
                is("lucene special characters \\+ \\- \\&\\& \\|\\| \\! \\( \\) "
                        + "\\{ \\} \\[ \\] \\^ \\\" \\~ \\* \\? \\: \\\\ plus % _"));
    }
}
