package org.zanata.webtrans.server.rpc;

import static org.hamcrest.MatcherAssert.assertThat;
import lombok.extern.slf4j.Slf4j;

import org.dbunit.operation.DatabaseOperation;
import org.hamcrest.Matchers;
import org.hibernate.Session;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.zanata.ZanataDbunitJpaTest;
import org.zanata.common.ContentState;
import org.zanata.dao.TextFlowDAO;
import org.zanata.model.HLocale;
import org.zanata.model.TestFixture;
import org.zanata.seam.SeamAutowire;
import org.zanata.webtrans.client.service.GetTransUnitActionContext;
import org.zanata.webtrans.shared.model.DocumentId;
import org.zanata.webtrans.shared.model.TransUnitId;
import org.zanata.webtrans.shared.rpc.GetTransUnitsNavigation;
import org.zanata.webtrans.shared.rpc.GetTransUnitsNavigationResult;

/**
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@Test(groups = { "jpa-tests" })
@Slf4j
public class GetTransUnitsNavigationServiceTest extends ZanataDbunitJpaTest
{
   private GetTransUnitsNavigationService service;
   private final DocumentId documentId = new DocumentId(1, "");
   private HLocale jaLocale;

   @Override
   protected void prepareDBUnitOperations()
   {
      beforeTestOperations.add(new DataSetOperation("performance/GetTransUnitListTest.dbunit.xml", DatabaseOperation.CLEAN_INSERT));
   }

   @BeforeMethod
   public void setUp() throws Exception
   {
      MockitoAnnotations.initMocks(this);
      TextFlowDAO dao = new TextFlowDAO((Session) getEm().getDelegate());
      // @formatter:off
      service = SeamAutowire.instance()
            .use("textFlowDAO", dao)
            .autowire(GetTransUnitsNavigationService.class);
      // @formatter:on
      jaLocale = getEm().find(HLocale.class, 3L);
   }

   @Test
   public void testExecuteWithNoFilterOptions() throws Exception
   {
      GetTransUnitsNavigation action = GetTransUnitsNavigation.newAction(new GetTransUnitActionContext(documentId));

      long startTime = System.nanoTime();
      GetTransUnitsNavigationResult result = service.getNavigationIndexes(action, jaLocale);
      GetTransUnitsNavigationServiceTest.log.info("********** duration :{} second", (System.nanoTime() - startTime) / 1000000000.0);

      assertThat(result.getTransIdStateList().size(), Matchers.equalTo(10));
      assertThat(result.getTransIdStateList(), Matchers.hasEntry(new TransUnitId(1L), ContentState.Approved));
      assertThat(result.getTransIdStateList(), Matchers.hasEntry(new TransUnitId(2L), ContentState.Approved));
      assertThat(result.getTransIdStateList(), Matchers.hasEntry(new TransUnitId(3L), ContentState.NeedReview));
      assertThat(result.getTransIdStateList(), Matchers.hasEntry(new TransUnitId(4L), ContentState.Approved));
      assertThat(result.getTransIdStateList(), Matchers.hasEntry(new TransUnitId(5L), ContentState.NeedReview));
      assertThat(result.getTransIdStateList(), Matchers.hasEntry(new TransUnitId(6L), ContentState.NeedReview));
      assertThat(result.getTransIdStateList(), Matchers.hasEntry(new TransUnitId(7L), ContentState.New));
      assertThat(result.getTransIdStateList(), Matchers.hasEntry(new TransUnitId(8L), ContentState.New));
      assertThat(result.getTransIdStateList(), Matchers.hasEntry(new TransUnitId(9L), ContentState.New));
      assertThat(result.getTransIdStateList(), Matchers.hasEntry(new TransUnitId(10L), ContentState.New));

      assertThat(TestFixture.asLongs(result.getIdIndexList()), Matchers.contains(1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L, 9L, 10L));
   }

   @Test
   public void testExecuteWithAllStatus() throws Exception
   {
      // filter with all status set to tru
      GetTransUnitActionContext context = new GetTransUnitActionContext(documentId).changeFilterNeedReview(true).changeFilterUntranslated(true).changeFilterTranslated(true);
      GetTransUnitsNavigation action = GetTransUnitsNavigation.newAction(context);

      GetTransUnitsNavigationResult result = service.getNavigationIndexes(action, jaLocale);

      assertThat(result.getTransIdStateList().size(), Matchers.equalTo(10));
      assertThat(result.getIdIndexList(), Matchers.hasSize(10));
   }

   @Test
   public void testExecuteWithStatus() throws Exception
   {
      // filter fuzzy and new status
      GetTransUnitActionContext context = new GetTransUnitActionContext(documentId).changeFilterNeedReview(true).changeFilterUntranslated(true);
      GetTransUnitsNavigation action = GetTransUnitsNavigation.newAction(context);

      GetTransUnitsNavigationResult result = service.getNavigationIndexes(action, jaLocale);

      assertThat(result.getTransIdStateList().size(), Matchers.equalTo(7));
      assertThat(result.getTransIdStateList(), Matchers.hasEntry(new TransUnitId(3L), ContentState.NeedReview));
      assertThat(result.getTransIdStateList(), Matchers.hasEntry(new TransUnitId(5L), ContentState.NeedReview));
      assertThat(result.getTransIdStateList(), Matchers.hasEntry(new TransUnitId(6L), ContentState.NeedReview));
      assertThat(result.getTransIdStateList(), Matchers.hasEntry(new TransUnitId(7L), ContentState.New));
      assertThat(result.getTransIdStateList(), Matchers.hasEntry(new TransUnitId(8L), ContentState.New));
      assertThat(result.getTransIdStateList(), Matchers.hasEntry(new TransUnitId(9L), ContentState.New));
      assertThat(result.getTransIdStateList(), Matchers.hasEntry(new TransUnitId(10L), ContentState.New));

      assertThat(TestFixture.asLongs(result.getIdIndexList()), Matchers.contains(3L, 5L, 6L, 7L, 8L, 9L, 10L));
   }

   @Test
   public void testExecuteWithSearch() throws Exception
   {
      // filter by search term in mixed cases
      GetTransUnitActionContext context = new GetTransUnitActionContext(documentId).changeFindMessage("FiLe");
      GetTransUnitsNavigation action = GetTransUnitsNavigation.newAction(context);

      GetTransUnitsNavigationResult result = service.getNavigationIndexes(action, jaLocale);

      assertThat(result.getTransIdStateList().size(), Matchers.equalTo(7));
      assertThat(result.getTransIdStateList(), Matchers.hasEntry(new TransUnitId(1L), ContentState.Approved));
      assertThat(result.getTransIdStateList(), Matchers.hasEntry(new TransUnitId(2L), ContentState.Approved));
      assertThat(result.getTransIdStateList(), Matchers.hasEntry(new TransUnitId(3L), ContentState.NeedReview));
      assertThat(result.getTransIdStateList(), Matchers.hasEntry(new TransUnitId(4L), ContentState.Approved));
      assertThat(result.getTransIdStateList(), Matchers.hasEntry(new TransUnitId(5L), ContentState.NeedReview));
      assertThat(result.getTransIdStateList(), Matchers.hasEntry(new TransUnitId(6L), ContentState.NeedReview));
      assertThat(result.getTransIdStateList(), Matchers.hasEntry(new TransUnitId(8L), ContentState.New));

      assertThat(TestFixture.asLongs(result.getIdIndexList()), Matchers.contains(1L, 2L, 3L, 4L, 5L, 6L, 8L));
   }

   @Test
   public void testExecuteWithSearchAndStatusFilter() throws Exception
   {
      // filter by search term in mixed cases and accept fuzzy and new
      GetTransUnitActionContext context = new GetTransUnitActionContext(documentId).changeFindMessage("FiLe").changeFilterNeedReview(true).changeFilterUntranslated(true);
      GetTransUnitsNavigation action = GetTransUnitsNavigation.newAction(context);

      GetTransUnitsNavigationResult result = service.getNavigationIndexes(action, jaLocale);

      assertThat(result.getTransIdStateList().size(), Matchers.equalTo(4));
      assertThat(result.getTransIdStateList(), Matchers.hasEntry(new TransUnitId(3L), ContentState.NeedReview));
      assertThat(result.getTransIdStateList(), Matchers.hasEntry(new TransUnitId(5L), ContentState.NeedReview));
      assertThat(result.getTransIdStateList(), Matchers.hasEntry(new TransUnitId(6L), ContentState.NeedReview));
      assertThat(result.getTransIdStateList(), Matchers.hasEntry(new TransUnitId(8L), ContentState.New));

      assertThat(TestFixture.asLongs(result.getIdIndexList()), Matchers.contains(3L, 5L, 6L, 8L));
   }
}
