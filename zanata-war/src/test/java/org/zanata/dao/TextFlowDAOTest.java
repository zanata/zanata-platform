package org.zanata.dao;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.List;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;

import org.dbunit.operation.DatabaseOperation;
import org.hamcrest.Matchers;
import org.hibernate.Query;
import org.hibernate.Session;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.zanata.ZanataDbunitJpaTest;
import org.zanata.model.HLocale;
import org.zanata.model.HTextFlow;
import org.zanata.model.HTextFlowTarget;
import org.zanata.search.FilterConstraints;
import org.zanata.webtrans.shared.model.ContentStateGroup;
import org.zanata.webtrans.shared.model.DocumentId;

@Test(groups = { "jpa-tests" })
@Slf4j
public class TextFlowDAOTest extends ZanataDbunitJpaTest
{

   private TextFlowDAO dao;

   @Override
   protected void prepareDBUnitOperations()
   {
      beforeTestOperations.add(new DataSetOperation("org/zanata/test/model/ClearAllTables.dbunit.xml", DatabaseOperation.DELETE_ALL));
      beforeTestOperations.add(new DataSetOperation("org/zanata/test/model/ProjectsData.dbunit.xml", DatabaseOperation.CLEAN_INSERT));
      beforeTestOperations.add(new DataSetOperation("org/zanata/test/model/TextFlowTestData.dbunit.xml", DatabaseOperation.CLEAN_INSERT));
      beforeTestOperations.add(new DataSetOperation("org/zanata/test/model/LocalesData.dbunit.xml", DatabaseOperation.CLEAN_INSERT));
      beforeTestOperations.add(new DataSetOperation("org/zanata/test/model/AccountData.dbunit.xml", DatabaseOperation.CLEAN_INSERT));
   }

   @BeforeMethod(firstTimeOnly = true)
   public void setup()
   {
      dao = new TextFlowDAO((Session) getEm().getDelegate());
//      printTestData();
   }

   private void printTestData()
   {
      //single text flow with 4 targets
      HTextFlow textFlow = dao.findById(1L, false);
      log.info("text flow: {}", textFlow);
      for (Map.Entry<Long, HTextFlowTarget> entry : textFlow.getTargets().entrySet())
      {
         log.debug("locale id: {} - target state: {}", entry.getKey(), entry.getValue().getState());
      }

      //3 text flows with single en-US fuzzy target
//      List<HTextFlow> doc2TextFlows = dao.getTextFlowsByDocumentId(new DocumentId(2L, ""), hLocale, 0, 9999);
//      for (HTextFlow doc2tf : doc2TextFlows)
//      {
//         log.debug("text flow id {} - targets {}", doc2tf.getId(), doc2tf.getTargets());
//      }

      //single text flow no target
      HTextFlow textFlow6 = dao.findById(6L, false);
      log.debug("text flow {} target: {}", textFlow6.getId(), textFlow6.getTargets());

   }

   // FIXME looks like this test does not take more recently added states into account
   //       should ensure all states are in test data and check test logic
   @Test
   public void canGetAllUntranslatedTextFlowForADocument() {
      HLocale deLocale = getEm().find(HLocale.class, 3L);
      log.info("locale: {}", deLocale);

      FilterConstraints untranslated = FilterConstraints.builder().keepAll().excludeFuzzy().excludeTranslated().build();
      List<HTextFlow> result = dao.getTextFlowByDocumentIdWithConstraints(new DocumentId(1L, ""), deLocale, untranslated, 0, 10);
      assertThat(result.size(), is(0));

      HLocale frLocale = getEm().find(HLocale.class, 6L);
      result = dao.getTextFlowByDocumentIdWithConstraints(new DocumentId(1L, ""), frLocale, untranslated, 0, 10);
      assertThat(result.size(), is(1));
   }

   @Test
   public void canGetTextFlowWithNullTarget() {
      HLocale deLocale = getEm().find(HLocale.class, 3L);

      FilterConstraints untranslated = FilterConstraints.builder().keepAll().excludeFuzzy().excludeTranslated().build();
      List<HTextFlow> result = dao.getTextFlowByDocumentIdWithConstraints(new DocumentId(4L, ""), deLocale, untranslated, 0, 10);
      assertThat(result, Matchers.hasSize(1));
   }

   @Test
   public void canGetTextFlowsByStatusNotNew()
   {
      HLocale enUSLocale = getEm().find(HLocale.class, 4L);
      // all 3 text flows are fuzzy for en-US in this document
      DocumentId documentId2 = new DocumentId(2L, "");
      List<HTextFlow> result = dao.getTextFlowByDocumentIdWithConstraints(documentId2, enUSLocale,
            FilterConstraints.builder().keepAll().excludeNew().build(), 0, 10);

      assertThat(result, Matchers.hasSize(3));
   }

   @Test
   public void canGetTextFlowsByStatusNotFuzzy()
   {
      // frLocale new in this document
      DocumentId documentId = new DocumentId(1L, "");
      HLocale frLocale = getEm().find(HLocale.class, 6L);
      FilterConstraints notFuzzy = FilterConstraints.builder().keepAll().excludeFuzzy().build();

      List<HTextFlow> result = dao.getTextFlowByDocumentIdWithConstraints(documentId, frLocale,
            notFuzzy, 0, 10);
      assertThat(result, Matchers.hasSize(1));
   }

   @Test
   public void canGetTextFlowsByStatusNotTranslatedNotNew()
   {
      // esLocale fuzzy in this document
      DocumentId documentId = new DocumentId(1L, "");
      HLocale esLocale = getEm().find(HLocale.class, 5L);
      FilterConstraints notNewOrTranslated = FilterConstraints.builder().keepAll().excludeTranslated().excludeNew().build();

      List<HTextFlow> result = dao.getTextFlowByDocumentIdWithConstraints(documentId, esLocale,
            notNewOrTranslated, 0, 10);
      assertThat(result, Matchers.hasSize(1));
   }

   @Test
   public void canGetTextFlowsByStatusNotFuzzyNotNew()
   {
      // deLocale approved in this document
      DocumentId documentId = new DocumentId(1L, "");
      HLocale deLocale = getEm().find(HLocale.class, 3L);
      FilterConstraints notNewOrFuzzy =
            FilterConstraints.builder().keepAll().excludeFuzzy().excludeNew().build();

      List<HTextFlow> result = dao.getTextFlowByDocumentIdWithConstraints(documentId, deLocale,
            notNewOrFuzzy, 0, 10);
      assertThat(result, Matchers.hasSize(1));
   }

   @Test
   public void canGetTextFlowsByStatusNotFuzzyNotTranslated()
   {
      HLocale enUSLocale = getEm().find(HLocale.class, 4L);
      // all 3 text flows are fuzzy for en-US in this document
      DocumentId documentId2 = new DocumentId(2L, "");
      FilterConstraints notFuzzyOrTranslated =
            FilterConstraints.builder().keepAll().excludeTranslated().excludeFuzzy().build();
      List<HTextFlow> result = dao.getTextFlowByDocumentIdWithConstraints(documentId2, enUSLocale,
            notFuzzyOrTranslated, 0, 10);
      assertThat(result, Matchers.<HTextFlow>empty());
   }

   @Test(enabled = false)
   public void thisBreaksForSomeReason() {
      // fails regardless of using different documentId, locale or constraints
      DocumentId id = new DocumentId(1L, "");
      HLocale locale = getEm().find(HLocale.class, 3L);
      FilterConstraints constraints = FilterConstraints.builder().build();

      dao.getTextFlowByDocumentIdWithConstraints(id, locale, constraints, 0, 10);
      dao.getTextFlowByDocumentIdWithConstraints(id, locale, constraints, 0, 10);
   }


   @Test
   public void canBuildAcceptAllQuery()
   {
      String contentStateCondition = TextFlowDAO.buildContentStateCondition(
            ContentStateGroup.builder().addAll().build(), "tft");
      assertThat("Conditional that accepts all should be '1'", contentStateCondition, is("1"));
   }

   // FIXME the 'none == all' logic should be limited to the editor
   @Test
   public void canBuildAcceptAllQueryWhenNoStatesSelected()
   {
      String contentStateCondition = TextFlowDAO.buildContentStateCondition(
            ContentStateGroup.builder().removeAll().build(), "tft");
      assertThat("Conditional that accepts all should be '1'", contentStateCondition, is("1"));
   }

   @Test
   public void canBuildNewOnlyConditional()
   {
      ContentStateGroup contentStates = ContentStateGroup.builder()
            .removeAll().includeNew(true).build();
      String contentStateCondition = TextFlowDAO.buildContentStateCondition(contentStates, "tft");
      assertThat(contentStateCondition, is("(tft.state=0 or tft.state is null)"));
   }

   @Test
   public void canBuildFuzzyOnlyConditional()
   {
      ContentStateGroup contentStates = ContentStateGroup.builder()
            .removeAll().includeFuzzy(true).build();
      String contentStateCondition = TextFlowDAO.buildContentStateCondition(contentStates, "tft");
      assertThat(contentStateCondition, is("(tft.state=1)"));
   }

   @Test
   public void canBuildTranslatedOnlyConditional()
   {
      ContentStateGroup contentStates = ContentStateGroup.builder()
            .removeAll().includeTranslated(true).build();
      String contentStateCondition = TextFlowDAO.buildContentStateCondition(contentStates, "tft");
      assertThat(contentStateCondition, is("(tft.state=2)"));
   }

   @Test
   public void canBuildApprovedOnlyConditional()
   {
      ContentStateGroup contentStates = ContentStateGroup.builder()
            .removeAll().includeApproved(true).build();
      String contentStateCondition = TextFlowDAO.buildContentStateCondition(contentStates, "tft");
      assertThat(contentStateCondition, is("(tft.state=3)"));
   }

   @Test
   public void canBuildRejectedOnlyConditional()
   {
      ContentStateGroup contentStates = ContentStateGroup.builder()
            .removeAll().includeRejected(true).build();
      String contentStateCondition = TextFlowDAO.buildContentStateCondition(contentStates, "tft");
      assertThat(contentStateCondition, is("(tft.state=4)"));
   }

   @Test
   public void canBuildNewAndFuzzyConditional()
   {
      ContentStateGroup contentStates = ContentStateGroup.builder()
            .removeAll().includeNew(true).includeFuzzy(true).build();
      String contentStateCondition = TextFlowDAO.buildContentStateCondition(contentStates, "tft");
      assertThat(contentStateCondition, is("(tft.state=0 or tft.state is null or tft.state=1)"));
   }
   @Test
   public void canBuildNewAndTranslatedConditional()
   {
      ContentStateGroup contentStates = ContentStateGroup.builder()
            .removeAll().includeNew(true).includeTranslated(true).build();
      String contentStateCondition = TextFlowDAO.buildContentStateCondition(contentStates, "tft");
      assertThat(contentStateCondition, is("(tft.state=0 or tft.state is null or tft.state=2)"));
   }

   @Test
   public void canBuildFuzzyAndTranslatedConditional()
   {
      ContentStateGroup contentStates = ContentStateGroup.builder()
            .removeAll().includeFuzzy(true).includeTranslated(true).build();
      String contentStateCondition = TextFlowDAO.buildContentStateCondition(contentStates, "tft");
      assertThat(contentStateCondition, is("(tft.state=1 or tft.state=2)"));
   }

   @Test
   public void canBuildSearchQuery()
   {
      // no search term
      assertThat(TextFlowDAO.buildSearchCondition(null, "tft"), Matchers.equalTo("1"));
      assertThat(TextFlowDAO.buildSearchCondition("", "tft"), Matchers.equalTo("1"));

      // with search term
      assertThat(TextFlowDAO.buildSearchCondition("a", "tft"), Matchers.equalTo("(lower(tft.content0) LIKE :searchstringlowercase or lower(tft.content1) LIKE :searchstringlowercase or lower(tft.content2) LIKE :searchstringlowercase or lower(tft.content3) LIKE :searchstringlowercase or lower(tft.content4) LIKE :searchstringlowercase or lower(tft.content5) LIKE :searchstringlowercase)"));
      assertThat(TextFlowDAO.buildSearchCondition("A", "tft"), Matchers.equalTo("(lower(tft.content0) LIKE :searchstringlowercase or lower(tft.content1) LIKE :searchstringlowercase or lower(tft.content2) LIKE :searchstringlowercase or lower(tft.content3) LIKE :searchstringlowercase or lower(tft.content4) LIKE :searchstringlowercase or lower(tft.content5) LIKE :searchstringlowercase)"));

   }

   @Test
   public void testGetTextFlowByDocumentIdWithConstraint()
   {
      HLocale deLocale = getEm().find(HLocale.class, 3L);

      List<HTextFlow> result = dao.getTextFlowByDocumentIdWithConstraints(
            new DocumentId(new Long(4), ""), deLocale,
            FilterConstraints.builder().filterBy("mssg").excludeTranslated().excludeFuzzy().build(),
            0, 10);

      assertThat(result, Matchers.hasSize(1));
   }

   // What is this testing? I can't tell if it is ensuring that no exception is thrown,
   // or if it is just half-written and useless.
   @Test
   public void queryTest1()
   {
      String queryString = "from HTextFlow tf left join tf.targets tft with (index(tft) = 3) " +
            "where (exists (from HTextFlowTarget where textFlow = tf and content0 like '%mssg%'))";
      Query query = getSession().createQuery(queryString);
      List result = query.list();
   }
}
