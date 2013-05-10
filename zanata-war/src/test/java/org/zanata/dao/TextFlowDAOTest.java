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
import org.zanata.webtrans.shared.model.DocumentId;

@Test(groups = { "jpa-tests" })
@Slf4j
public class TextFlowDAOTest extends ZanataDbunitJpaTest
{

   private TextFlowDAO dao;

   @Override
   protected void prepareDBUnitOperations()
   {
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
      List<HTextFlow> doc2TextFlows = dao.getTextFlowsByDocumentId(new DocumentId(2L, ""), 0, 9999);
      for (HTextFlow doc2tf : doc2TextFlows)
      {
         log.debug("text flow id {} - targets {}", doc2tf.getId(), doc2tf.getTargets());
      }

      //single text flow no target
      HTextFlow textFlow6 = dao.findById(6L, false);
      log.debug("text flow {} target: {}", textFlow6.getId(), textFlow6.getTargets());

   }

   @Test
   public void canGetAllUntranslatedTextFlowForADocument() {
      HLocale deLocale = getEm().find(HLocale.class, 3L);
      log.info("locale: {}", deLocale);

      FilterConstraints untranslated = FilterConstraints.keepAll().excludeFuzzy().excludeApproved();
      List<HTextFlow> result = dao.getTextFlowByDocumentIdWithConstraint(new DocumentId(1L, ""), deLocale, untranslated, 0, 10);
      assertThat(result.size(), is(0));

      HLocale frLocale = getEm().find(HLocale.class, 6L);
      result = dao.getTextFlowByDocumentIdWithConstraint(new DocumentId(1L, ""), frLocale, untranslated, 0, 10);
      assertThat(result.size(), is(1));

   }

   @Test
   public void canGetTextFlowWithNullTarget() {
      HLocale deLocale = getEm().find(HLocale.class, 3L);

      FilterConstraints untranslated = FilterConstraints.keepAll().excludeFuzzy().excludeApproved();
      List<HTextFlow> result = dao.getTextFlowByDocumentIdWithConstraint(new DocumentId(4L, ""), deLocale, untranslated, 0, 10);
      assertThat(result, Matchers.hasSize(1));
   }

   @Test
   public void canGetTextFlowsByStatus() {
      HLocale esLocale = getEm().find(HLocale.class, 5L);
      HLocale frLocale = getEm().find(HLocale.class, 6L);
      HLocale deLocale = getEm().find(HLocale.class, 3L);

      DocumentId documentId1 = new DocumentId(1L, ""); // esLocale fuzzy,
                                                       // frLocale new, deLocale
                                                       // approved
      List<HTextFlow> result = dao.getTextFlowByDocumentIdWithConstraint(documentId1, esLocale, FilterConstraints.keepAll().excludeApproved().excludeNew(), 0, 10);
      assertThat(result, Matchers.hasSize(1));

      result = dao.getTextFlowByDocumentIdWithConstraint(documentId1, frLocale, FilterConstraints.keepAll().excludeFuzzy(), 0, 10);
      assertThat(result, Matchers.hasSize(1));

      result = dao.getTextFlowByDocumentIdWithConstraint(documentId1, deLocale, FilterConstraints.keepAll().excludeFuzzy().excludeNew(), 0, 10);
      assertThat(result, Matchers.hasSize(1));

      HLocale enUSLocale = getEm().find(HLocale.class, 4L);
      DocumentId documentId2 = new DocumentId(2L, ""); // all 3 text flows has
                                                       // en-US fuzzy target

      result = dao.getTextFlowByDocumentIdWithConstraint(documentId2, enUSLocale, FilterConstraints.keepAll().excludeApproved().excludeFuzzy(), 0, 10);
      assertThat(result, Matchers.<HTextFlow>empty());

      result = dao.getTextFlowByDocumentIdWithConstraint(documentId2, enUSLocale, FilterConstraints.keepAll().excludeNew(), 0, 10);
      assertThat(result, Matchers.hasSize(3));
   }

   @Test
   public void canBuildContentStateQuery()
   {
      // accept all
      assertThat(TextFlowDAO.buildContentStateCondition(true, true, true, "tft"), Matchers.equalTo("1"));
      assertThat(TextFlowDAO.buildContentStateCondition(false, false, false, "tft"), Matchers.equalTo("1"));

      // single status filter
      assertThat(TextFlowDAO.buildContentStateCondition(true, false, false, "tft"), Matchers.equalTo("(tft.state=2)"));
      assertThat(TextFlowDAO.buildContentStateCondition(false, true, false, "tft"), Matchers.equalTo("(tft.state=1)"));
      assertThat(TextFlowDAO.buildContentStateCondition(false, false, true, "tft"), Matchers.equalTo("(tft.state=0 or tft.state is null)"));

      // two status
      assertThat(TextFlowDAO.buildContentStateCondition(true, false, true, "tft"), Matchers.equalTo("(tft.state=2 or tft.state=0 or tft.state is null)"));
      assertThat(TextFlowDAO.buildContentStateCondition(true, true, false, "tft"), Matchers.equalTo("(tft.state=2 or tft.state=1)"));
      assertThat(TextFlowDAO.buildContentStateCondition(false, true, true, "tft"), Matchers.equalTo("(tft.state=1 or tft.state=0 or tft.state is null)"));
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

      List<HTextFlow> result = dao.getTextFlowByDocumentIdWithConstraint(new DocumentId(new Long(4), ""), deLocale, FilterConstraints.filterBy("mssg").excludeApproved().excludeFuzzy(), 0, 10);

      assertThat(result, Matchers.hasSize(1));
   }

   @Test
   public void queryTest1()
   {
      String queryString = "from HTextFlow tf left join tf.targets tft with (index(tft) = 3) " +
            "where (exists (from HTextFlowTarget where textFlow = tf and content0 like '%mssg%'))";
      Query query = getSession().createQuery(queryString);
      List result = query.list();

   }
}
