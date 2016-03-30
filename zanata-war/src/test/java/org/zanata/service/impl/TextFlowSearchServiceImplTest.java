package org.zanata.service.impl;

import java.util.List;

import org.dbunit.operation.DatabaseOperation;
import org.hamcrest.Matchers;
import org.hibernate.Session;
import org.hibernate.search.FullTextSession;
import org.hibernate.search.jpa.FullTextEntityManager;
import org.hibernate.search.jpa.Search;
import org.jglue.cdiunit.InRequestScope;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.zanata.ZanataDbunitJpaTest;
import org.zanata.common.LocaleId;
import org.zanata.common.ProjectType;
import org.zanata.jpa.FullText;
import org.zanata.model.HLocale;
import org.zanata.model.HTextFlow;
import org.zanata.model.TestFixture;
import org.zanata.search.FilterConstraints;
import org.zanata.service.LocaleService;
import org.zanata.test.CdiUnitRunner;
import org.zanata.webtrans.shared.model.WorkspaceId;

import javax.enterprise.inject.Produces;
import javax.inject.Inject;

import static org.hamcrest.MatcherAssert.*;
import static org.mockito.Mockito.*;

/**
 * @author Patrick Huang <a
 *         href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@RunWith(CdiUnitRunner.class)
public class TextFlowSearchServiceImplTest extends ZanataDbunitJpaTest {

    @Inject
    private TextFlowSearchServiceImpl service;

    private final LocaleId localeId = new LocaleId("ja");
    private final WorkspaceId workspaceId = TestFixture.workspaceId(localeId,
            "plurals", "master", ProjectType.Podir);
    private HLocale jaHLocale;

    @Produces @Mock private LocaleService localeService;

    @Override
    @Produces
    protected Session getSession() {
        return super.getSession();
    }

    @Produces
    @FullText
    FullTextEntityManager getFullTextEntityManager() {
        return Search.getFullTextEntityManager(getEm());
    }

    @Produces
    @FullText
    FullTextSession getFullTextSession() {
        return org.hibernate.search.Search.getFullTextSession(getSession());
    }

    @Override
    protected void prepareDBUnitOperations() {
        beforeTestOperations.add(new DataSetOperation(
                "performance/GetTransUnitListTest.dbunit.xml",
                DatabaseOperation.CLEAN_INSERT));
    }

    @Before
    public void beforeMethod() {
        jaHLocale = getEm().find(HLocale.class, 3L);
        when(
                localeService.validateLocaleByProjectIteration(localeId,
                        workspaceId.getProjectIterationId().getProjectSlug(),
                        workspaceId.getProjectIterationId().getIterationSlug()))
                .thenReturn(jaHLocale);
    }

    @Test
    @InRequestScope
    public void testFindTextFlows() throws Exception {
        List<HTextFlow> result =
                service.findTextFlows(workspaceId, FilterConstraints.builder()
                        .filterBy("file").build());

        assertThat(result.size(), Matchers.equalTo(7));
    }
}
