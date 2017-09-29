package org.zanata.webtrans.server.rpc;

import java.util.ArrayList;
import java.util.List;
import org.dbunit.operation.DatabaseOperation;
import org.hibernate.Session;
import org.hibernate.search.jpa.FullTextEntityManager;
import org.jglue.cdiunit.InRequestScope;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.zanata.ZanataDbunitJpaTest;
import org.zanata.common.LocaleId;
import org.zanata.common.ProjectType;
import org.zanata.dao.GlossaryDAO;
import org.zanata.jpa.FullText;
import org.zanata.model.HGlossaryTerm;
import org.zanata.model.HLocale;
import org.zanata.rest.service.ProjectService;
import org.zanata.security.ZanataIdentity;
import org.zanata.service.LocaleService;
import org.zanata.service.impl.GlossarySearchServiceImpl;
import org.zanata.test.CdiUnitRunner;
import org.zanata.util.GlossaryUtil;
import org.zanata.webtrans.shared.model.GlossaryResultItem;
import org.zanata.webtrans.shared.model.ProjectIterationId;
import org.zanata.webtrans.shared.rpc.GetGlossary;
import org.zanata.webtrans.shared.rpc.HasSearchType;
import com.google.common.collect.Lists;
import javax.enterprise.inject.Produces;
import javax.persistence.EntityManager;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

/**
 * @author Patrick Huang
 *         <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@RunWith(CdiUnitRunner.class)
public class GetGlossaryHandlerJpaTest extends ZanataDbunitJpaTest {
    private static final org.slf4j.Logger log =
            org.slf4j.LoggerFactory.getLogger(GetGlossaryHandlerJpaTest.class);

    private static final LocaleId TARGET_LOCALE_ID = new LocaleId("zh");

    @Produces
    @Mock
    private ZanataIdentity identity;
    @Produces
    @Mock
    private LocaleService localeService;
    @Produces
    @Mock
    @FullText
    private FullTextEntityManager fullTextEntityManager;
    private HLocale targetHLocale;
    private GlossaryDAO glossaryDAO;
    private GlossarySearchServiceImpl glossarySearchService;

    @Override
    @Produces
    protected EntityManager getEm() {
        return super.getEm();
    }

    @Override
    @Produces
    protected Session getSession() {
        return super.getSession();
    }

    @Override
    protected void prepareDBUnitOperations() {
        beforeTestOperations
                .add(new DataSetOperation("performance/GlossaryTest.dbunit.xml",
                        DatabaseOperation.CLEAN_INSERT));
    }

    @Before
    public void setUp() throws Exception {
        GlossaryDAO dao = new GlossaryDAO(getSession());
        glossaryDAO = spy(dao);
        targetHLocale = getEm().find(HLocale.class, 2L);
        glossarySearchService =
                new GlossarySearchServiceImpl(glossaryDAO, localeService, "/");
    }

    @Test
    @InRequestScope
    public void canGetGlossary() throws Exception {
        // Given:
        when(localeService.getByLocaleId(TARGET_LOCALE_ID))
                .thenReturn(targetHLocale);
        ProjectIterationId id = new ProjectIterationId("progSlug",
                "versionSlug", ProjectType.File);
        GetGlossary action = new GetGlossary("fedora", id, TARGET_LOCALE_ID,
                LocaleId.EN_US, HasSearchType.SearchType.FUZZY);
        // hibernate search result - global
        HGlossaryTerm srcGlossaryTerm1 = getEm().find(HGlossaryTerm.class, 42L);
        // hibernate search result - project
        HGlossaryTerm srcGlossaryTerm2 = getEm().find(HGlossaryTerm.class, 46L);
        Object[] projMatches = new Object[] { 1.1F, srcGlossaryTerm2 };
        List<Object[]> projResults = Lists.newArrayList();
        projResults.add(projMatches);
        String projQualifiedName = ProjectService.getGlossaryQualifiedName(
                action.getProjectIterationId().getProjectSlug());
        doReturn(projResults).when(glossaryDAO).getSearchResult("fedora",
                HasSearchType.SearchType.FUZZY, LocaleId.EN_US, 20,
                projQualifiedName);
        Object[] matches = new Object[] { 1.0F, srcGlossaryTerm1 };
        List<Object[]> results = Lists.newArrayList();
        results.add(matches);
        doReturn(results).when(glossaryDAO).getSearchResult("fedora",
                HasSearchType.SearchType.FUZZY, LocaleId.EN_US, 20,
                GlossaryUtil.GLOBAL_QUALIFIED_NAME);
        // When:
        long start = System.nanoTime();
        // FIXME this line gives a NPE, but glossarySearchServiceImpl can be
        //       asserted as non-null value
        ArrayList<GlossaryResultItem> result = glossarySearchService.searchGlossary(
                LocaleId.EN_US, TARGET_LOCALE_ID, "fedora",
                HasSearchType.SearchType.FUZZY, 20, "progSlug");
        double duration = (System.nanoTime() - start) / 1.0E9;
        log.info("************** {} second", duration);

        // Then:
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getSource()).isEqualTo("Planet Fedora");
        assertThat(result.get(0).getTarget()).isEqualTo("Fedora 博客聚集");
        assertThat(result.get(1).getSource()).isEqualTo("Fedora Artwork");
        assertThat(result.get(1).getTarget()).isEqualTo("Fedora 美工");
    }
}
