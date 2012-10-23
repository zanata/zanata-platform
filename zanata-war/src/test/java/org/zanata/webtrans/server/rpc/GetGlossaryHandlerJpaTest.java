package org.zanata.webtrans.server.rpc;

import java.util.List;

import org.dbunit.operation.DatabaseOperation;
import org.hamcrest.Matchers;
import org.hibernate.Session;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.zanata.ZanataDbunitJpaTest;
import org.zanata.common.LocaleId;
import org.zanata.dao.GlossaryDAO;
import org.zanata.model.HGlossaryTerm;
import org.zanata.model.HLocale;
import org.zanata.seam.SeamAutowire;
import org.zanata.security.ZanataIdentity;
import org.zanata.service.LocaleService;
import org.zanata.webtrans.shared.rpc.GetGlossary;
import org.zanata.webtrans.shared.rpc.GetGlossaryResult;
import org.zanata.webtrans.shared.rpc.HasSearchType;

import com.google.common.collect.Lists;

import lombok.extern.slf4j.Slf4j;
import static org.hamcrest.MatcherAssert.*;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

/**
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@Test(groups = "jpa-tests")
@Slf4j
public class GetGlossaryHandlerJpaTest extends ZanataDbunitJpaTest
{
   private static final LocaleId TARGET_LOCALE_ID = new LocaleId("zh");
   private GetGlossaryHandler handler;
   @Mock
   private ZanataIdentity identity;
   @Mock
   private LocaleService localeService;
   private HLocale targetHLocale;
   private GlossaryDAO glossaryDAOSpy;

   @Override
   protected void prepareDBUnitOperations()
   {
      beforeTestOperations.add(new DataSetOperation("performance/GlossaryTest.dbunit.xml", DatabaseOperation.CLEAN_INSERT));
   }

   @BeforeMethod
   public void setUp() throws Exception
   {
      MockitoAnnotations.initMocks(this);
      GlossaryDAO dao = new GlossaryDAO((Session) getEm().getDelegate());
      glossaryDAOSpy = spy(dao);

      // @formatter:off
      handler = SeamAutowire.instance()
            .use("identity", identity)
            .use("localeServiceImpl", localeService)
            .use("glossaryDAO", glossaryDAOSpy)
            .ignoreNonResolvable()
            .autowire(GetGlossaryHandler.class);
      // @formatter:on
      targetHLocale = getEm().find(HLocale.class, 2L);
   }

   @Test
   public void canGetGlossary() throws Exception
   {
      // Given:
      when(localeService.getByLocaleId(TARGET_LOCALE_ID)).thenReturn(targetHLocale);
      GetGlossary action = new GetGlossary("fedora", TARGET_LOCALE_ID, LocaleId.EN_US, HasSearchType.SearchType.FUZZY);
      // hibernate search result
      HGlossaryTerm srcGlossaryTerm1 = getEm().find(HGlossaryTerm.class, 42L);
      HGlossaryTerm srcGlossaryTerm2 = getEm().find(HGlossaryTerm.class, 46L);
      List<Object[]> matches = Lists.newArrayList(new Object[] {1.0F, srcGlossaryTerm1}, new Object[] {1.1F, srcGlossaryTerm2});
      doReturn(matches).when(glossaryDAOSpy).getSearchResult("fedora", HasSearchType.SearchType.FUZZY, LocaleId.EN_US, 20);

      // When:
      long start = System.nanoTime();
      GetGlossaryResult result = handler.execute(action, null);
      double duration = (System.nanoTime() - start) / 1000000000.0;
      log.info("************** {} second", duration);

      // Then:
      assertThat(result.getGlossaries(), Matchers.hasSize(2));
      assertThat(result.getGlossaries().get(0).getSource(), Matchers.equalTo("Planet Fedora"));
      assertThat(result.getGlossaries().get(0).getTarget(), Matchers.equalTo("Fedora 博客聚集"));
      assertThat(result.getGlossaries().get(1).getSource(), Matchers.equalTo("Fedora Artwork"));
      assertThat(result.getGlossaries().get(1).getTarget(), Matchers.equalTo("Fedora 美工"));
   }

   @Test
   public void testRollback() throws Exception
   {
      handler.rollback(null, null, null);
   }
}
