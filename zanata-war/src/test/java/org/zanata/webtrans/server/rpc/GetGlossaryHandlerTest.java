package org.zanata.webtrans.server.rpc;

import java.util.List;

import org.hamcrest.Matchers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.zanata.common.LocaleId;
import org.zanata.dao.GlossaryDAO;
import org.zanata.model.HGlossaryEntry;
import org.zanata.model.HGlossaryTerm;
import org.zanata.model.HLocale;
import org.zanata.model.TestFixture;
import org.zanata.seam.SeamAutowire;
import org.zanata.security.ZanataIdentity;
import org.zanata.service.LocaleService;
import org.zanata.webtrans.shared.rpc.GetGlossary;
import org.zanata.webtrans.shared.rpc.GetGlossaryResult;
import org.zanata.webtrans.shared.rpc.HasSearchType;

import com.google.common.collect.Lists;

import lombok.extern.slf4j.Slf4j;
import static com.google.common.collect.Lists.newArrayList;
import static org.hamcrest.MatcherAssert.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@Test(groups = "unit-tests")
@Slf4j
public class GetGlossaryHandlerTest
{
   private static final HLocale SRC_LOCALE = new HLocale(LocaleId.EN_US);
   private static final HLocale TARGET_LOCALE = new HLocale(LocaleId.FR);
   private GetGlossaryHandler handler;
   @Mock
   private ZanataIdentity identity;
   @Mock
   private LocaleService localeService;
   @Mock
   private GlossaryDAO glossaryDAO;

   @BeforeMethod
   public void setUp() throws Exception
   {
      MockitoAnnotations.initMocks(this);
      // @formatter:off
      handler = SeamAutowire.instance()
            .use("identity", identity)
            .use("localeServiceImpl", localeService)
            .use("glossaryDAO", glossaryDAO)
            .ignoreNonResolvable()
            .autowire(GetGlossaryHandler.class);
      // @formatter:on

      when(localeService.getByLocaleId(TARGET_LOCALE.getLocaleId())).thenReturn(TARGET_LOCALE);
   }

   private static HGlossaryEntry entry(long id)
   {
      HGlossaryEntry entry = new HGlossaryEntry();
      entry.setSrcLocale(SRC_LOCALE);
      TestFixture.setId(id, entry);
      return entry;
   }

   private static HGlossaryTerm createTermAndAddToEntry(String content, HGlossaryEntry glossaryEntry)
   {
      HGlossaryTerm term = new HGlossaryTerm(content);
      term.setGlossaryEntry(glossaryEntry);
      return term;
   }

   @Test
   public void testExecute() throws Exception
   {
      // Given: we have 2 glossary entries and each have 2 terms in it
      GetGlossary action = new GetGlossary("term", TARGET_LOCALE.getLocaleId(), LocaleId.EN_US, HasSearchType.SearchType.FUZZY);
      // 2 entries
      HGlossaryEntry glossaryEntry1 = entry(1L);
      HGlossaryEntry glossaryEntry2 = entry(2L);
      // entry one has two terms
      HGlossaryTerm glossaryTerm1 = createTermAndAddToEntry("source glossary term 1", glossaryEntry1);
      glossaryEntry1.getGlossaryTerms().put(SRC_LOCALE, glossaryTerm1);
      glossaryEntry1.getGlossaryTerms().put(TARGET_LOCALE, createTermAndAddToEntry("target glossary term 1", glossaryEntry1));
      // entry two has two terms
      HGlossaryTerm glossaryTerm2 = createTermAndAddToEntry("source glossary term 2", glossaryEntry2);
      glossaryEntry2.getGlossaryTerms().put(SRC_LOCALE, glossaryTerm2);
      glossaryEntry2.getGlossaryTerms().put(TARGET_LOCALE, createTermAndAddToEntry("target glossary term 2", glossaryEntry2));

      when(glossaryDAO.getEntriesByLocaleId(TARGET_LOCALE.getLocaleId())).thenReturn(Lists.newArrayList(glossaryEntry1, glossaryEntry2));
      // found 3 matches: first has score 1, second is null, third has score 1.1
      when(glossaryDAO.getSearchResult(action.getQuery(), action.getSearchType(), action.getSrcLocaleId(), 20)).thenReturn(newArrayList(match(1.0F, glossaryTerm1), match(0.7F, null), match(1.1F, glossaryTerm2)));

      // When:
      GetGlossaryResult result = handler.execute(action, null);

      // Then:
      verify(identity).checkLoggedIn();
      assertThat(result.getGlossaries(), Matchers.hasSize(2));
      assertThat(result.getGlossaries().get(0).getSource(), Matchers.equalTo("source glossary term 2"));
      assertThat(result.getGlossaries().get(0).getTarget(), Matchers.equalTo("target glossary term 2"));
      assertThat(result.getGlossaries().get(1).getSource(), Matchers.equalTo("source glossary term 1"));
      assertThat(result.getGlossaries().get(1).getTarget(), Matchers.equalTo("target glossary term 1"));
   }

   @Test
   public void testExecuteWithEntriesDontContainTargetLocale() throws Exception
   {
      // Given: we have 2 glossary entries and first one do not contain target locale term
      GetGlossary action = new GetGlossary("term", TARGET_LOCALE.getLocaleId(), LocaleId.EN_US, HasSearchType.SearchType.FUZZY);
      // 2 entries
      HGlossaryEntry glossaryEntry1 = entry(1L);
      HGlossaryEntry glossaryEntry2 = entry(2L);
      // entry one has only one term
      HGlossaryTerm glossaryTerm1 = createTermAndAddToEntry("source glossary term 1", glossaryEntry1);
      glossaryEntry1.getGlossaryTerms().put(SRC_LOCALE, glossaryTerm1);
      // entry two has two terms
      HGlossaryTerm glossaryTerm2 = createTermAndAddToEntry("source glossary term 2", glossaryEntry2);
      glossaryEntry2.getGlossaryTerms().put(SRC_LOCALE, glossaryTerm2);
      glossaryEntry2.getGlossaryTerms().put(TARGET_LOCALE, createTermAndAddToEntry("target glossary term 2", glossaryEntry2));

      when(glossaryDAO.getEntriesByLocaleId(TARGET_LOCALE.getLocaleId())).thenReturn(Lists.newArrayList(glossaryEntry1, glossaryEntry2));
      // found 3 matches: first has score 1, second is null, third has score 1.1
      when(glossaryDAO.getSearchResult(action.getQuery(), action.getSearchType(), action.getSrcLocaleId(), 20)).thenReturn(newArrayList(match(1.0F, glossaryTerm1), match(0.7F, null), match(1.1F, glossaryTerm2)));

      // When:
      GetGlossaryResult result = handler.execute(action, null);

      // Then:
      verify(identity).checkLoggedIn();
      assertThat(result.getGlossaries(), Matchers.hasSize(1));
      assertThat(result.getGlossaries().get(0).getSource(), Matchers.equalTo("source glossary term 2"));
      assertThat(result.getGlossaries().get(0).getTarget(), Matchers.equalTo("target glossary term 2"));
   }

   private static Object[] match(Float score, HGlossaryTerm glossaryTerm)
   {
      return new Object[]{score, glossaryTerm == null ? null : glossaryTerm};
   }

   @Test
   public void testRollback() throws Exception
   {
      handler.rollback(null, null, null);
   }
}
