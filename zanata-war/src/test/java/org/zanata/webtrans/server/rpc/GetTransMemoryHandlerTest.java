package org.zanata.webtrans.server.rpc;

import java.util.List;

import org.apache.lucene.queryParser.ParseException;
import org.dbunit.operation.DatabaseOperation;
import org.hamcrest.Matchers;
import org.hibernate.search.jpa.impl.FullTextEntityManagerImpl;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.zanata.ZanataDbunitJpaTest;
import org.zanata.common.LocaleId;
import org.zanata.dao.TextFlowDAO;
import org.zanata.model.HLocale;
import org.zanata.model.HTextFlow;
import org.zanata.model.HTextFlowTarget;
import org.zanata.seam.SeamAutowire;
import org.zanata.security.ZanataIdentity;
import org.zanata.service.LocaleService;
import org.zanata.webtrans.shared.model.TransMemoryQuery;
import org.zanata.webtrans.shared.rpc.GetTranslationMemory;
import org.zanata.webtrans.shared.rpc.GetTranslationMemoryResult;
import org.zanata.webtrans.shared.rpc.HasSearchType;

import com.google.common.collect.Lists;

import static org.hamcrest.MatcherAssert.*;
import static org.mockito.Mockito.*;

/**
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@Test(groups = "jpa-tests")
public class GetTransMemoryHandlerTest extends ZanataDbunitJpaTest
{
   private GetTransMemoryHandler handler;
   @Mock
   private ZanataIdentity identity;
   @Mock
   private LocaleService localeService;
   private LocaleId targetLocaleId = new LocaleId("ja");
   private LocaleId sourceLocaleId = LocaleId.EN_US;
   private TextFlowDAO textFlowDAOSpy;

   @Override
   protected void prepareDBUnitOperations()
   {
      beforeTestOperations.add(new DataSetOperation("performance/GetTransUnitListTest.dbunit.xml", DatabaseOperation.CLEAN_INSERT));
   }

   @BeforeMethod
   public void setUp() throws Exception
   {
      MockitoAnnotations.initMocks(this);
      SeamAutowire autoWireInstance = SeamAutowire.instance();
      autoWireInstance
            .use("identity", identity)
            .use("localeServiceImpl", localeService)
            .use("entityManager", new FullTextEntityManagerImpl(getEm()))
            .use("session", getSession());
      TextFlowDAO dao = autoWireInstance
            .autowire(TextFlowDAO.class);
      textFlowDAOSpy = spy(dao);
      // @formatter:off
      autoWireInstance.use("textFlowDAO", textFlowDAOSpy);
      handler = autoWireInstance.autowire(GetTransMemoryHandler.class);
      // @formatter:on
      when(localeService.getByLocaleId(targetLocaleId.getId())).thenReturn(getEm().find(HLocale.class, 3L));
   }

   @SuppressWarnings("unchecked")
   @Test
   public void testExecute() throws Exception
   {
      // Given: hibernate search finds 2 matches for query
      TransMemoryQuery query = new TransMemoryQuery(Lists.newArrayList("file removed"), HasSearchType.SearchType.FUZZY_PLURAL);
      HTextFlowTarget tmMatch1 = getEm().find(HTextFlowTarget.class, 60L);
      HTextFlowTarget tmMatch2 = getEm().find(HTextFlowTarget.class, 62L);
      List<Object[]> matches = Lists.newArrayList(new Object[] {1.0F, tmMatch1}, new Object[] {1.1F, tmMatch2});
      doReturn(matches).when(textFlowDAOSpy).getSearchResult(eq(query), eq(sourceLocaleId), eq(25));
      GetTranslationMemory action = new GetTranslationMemory(query, targetLocaleId, sourceLocaleId);

      // When:
      GetTranslationMemoryResult result = handler.execute(action, null);

      // Then:
      verify(identity).checkLoggedIn();
      assertThat(result.getMemories(), Matchers.hasSize(2));
      assertThat(result.getMemories().get(0).getTargetContents(), Matchers.contains("adsf"));
      assertThat(result.getMemories().get(1).getTargetContents(), Matchers.contains("%d files removed"));
   }

   // This does not apply to the current implementation, but is left in
   // case a return to the other implementation is required.
   @Test
   public void searchReturnNotApprovedResult() throws Exception
   {
      // Given: hibernate search finds 2 matches for query and they are not approved translation
      TransMemoryQuery query = new TransMemoryQuery(Lists.newArrayList("file removed"), HasSearchType.SearchType.FUZZY_PLURAL);
      HTextFlow tmMatch1 = getEm().find(HTextFlowTarget.class, 61L).getTextFlow();
      List<Object[]> matches = Lists.newArrayList(new Object[] {1.0F, tmMatch1}, new Object[] {1.1F, null});
      doReturn(matches).when(textFlowDAOSpy).getSearchResult(eq(query), anyList(), eq(sourceLocaleId), eq(10));
      GetTranslationMemory action = new GetTranslationMemory(query, targetLocaleId, sourceLocaleId);

      // When:
      GetTranslationMemoryResult result = handler.execute(action, null);

      // Then:
      verify(identity).checkLoggedIn();
      assertThat(result.getMemories(), Matchers.hasSize(0));
   }

   @SuppressWarnings("unchecked")
   @Test
   public void whenThereAreParseException() throws Exception
   {
      // Given: hibernate search can not parse query
      TransMemoryQuery query = new TransMemoryQuery(Lists.newArrayList("file removed"), HasSearchType.SearchType.FUZZY_PLURAL);
      doThrow(new ParseException("bad token")).when(textFlowDAOSpy).getSearchResult(eq(query), eq(sourceLocaleId), eq(25));
      GetTranslationMemory action = new GetTranslationMemory(query, targetLocaleId, sourceLocaleId);

      // When:
      GetTranslationMemoryResult result = handler.execute(action, null);

      // Then:
      verify(identity).checkLoggedIn();
      assertThat(result.getMemories(), Matchers.hasSize(0));
   }

   @Test
   public void testRollback() throws Exception
   {
      handler.rollback(null, null, null);
   }
}
