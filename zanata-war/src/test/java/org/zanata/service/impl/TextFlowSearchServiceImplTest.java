package org.zanata.service.impl;

import java.util.List;

import org.dbunit.operation.DatabaseOperation;
import org.hamcrest.Matchers;
import org.hibernate.search.impl.FullTextSessionImpl;
import org.hibernate.search.jpa.impl.FullTextEntityManagerImpl;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.zanata.ZanataDbunitJpaTest;
import org.zanata.common.LocaleId;
import org.zanata.dao.DocumentDAO;
import org.zanata.dao.ProjectIterationDAO;
import org.zanata.model.HLocale;
import org.zanata.model.HTextFlow;
import org.zanata.model.TestFixture;
import org.zanata.seam.SeamAutowire;
import org.zanata.search.FilterConstraints;
import org.zanata.service.LocaleService;
import org.zanata.service.TextFlowSearchService;
import org.zanata.webtrans.shared.model.WorkspaceId;

import lombok.extern.slf4j.Slf4j;
import static org.hamcrest.MatcherAssert.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.when;

/**
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@Test(groups = { "jpa-tests" })
@Slf4j
public class TextFlowSearchServiceImplTest extends ZanataDbunitJpaTest
{
   private TextFlowSearchService service;
   @Mock
   private LocaleService localeService;
   private final LocaleId localeId = new LocaleId("ja");
   private final WorkspaceId workspaceId = TestFixture.workspaceId(localeId, "plurals", "master");
   private HLocale jaHLocale;

   @Override
   protected void prepareDBUnitOperations()
   {
      beforeTestOperations.add(new DataSetOperation("performance/GetTransUnitListTest.dbunit.xml", DatabaseOperation.CLEAN_INSERT));
   }

   @BeforeMethod
   public void beforeMethod()
   {
      MockitoAnnotations.initMocks(this);
      // @formatter:off
      service = SeamAutowire.instance()
            .use("localeServiceImpl", localeService)
            .use("documentDAO", new DocumentDAO(getSession()))
            .use("projectIterationDAO", new ProjectIterationDAO(getSession()))
            .use("entityManager", new FullTextEntityManagerImpl(getEm()))
            .use("session", new FullTextSessionImpl(getSession()))
            .autowire(TextFlowSearchServiceImpl.class);
      // @formatter:on
      jaHLocale = getEm().find(HLocale.class, 3L);
      when(localeService.validateLocaleByProjectIteration(localeId, workspaceId.getProjectIterationId().getProjectSlug(), workspaceId.getProjectIterationId().getIterationSlug())).thenReturn(jaHLocale);
   }

   @Test
   public void testFindTextFlows() throws Exception
   {
      List<HTextFlow> result = service.findTextFlows(workspaceId, FilterConstraints.filterBy("file"));

      assertThat(result.size(), Matchers.equalTo(7));
   }
}
