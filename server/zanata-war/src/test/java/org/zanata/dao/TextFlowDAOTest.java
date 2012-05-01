package org.zanata.dao;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.List;

import org.dbunit.operation.DatabaseOperation;
import org.hibernate.Session;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.zanata.ZanataDbunitJpaTest;
import org.zanata.common.LocaleId;

@Test(groups = { "jpa-tests" })
public class TextFlowDAOTest extends ZanataDbunitJpaTest
{

   private TextFlowDAO dao;

   @Override
   protected void prepareDBUnitOperations()
   {
      beforeTestOperations.add(new DataSetOperation("org/zanata/test/model/TextFlowTestData.dbunit.xml", DatabaseOperation.CLEAN_INSERT));
      beforeTestOperations.add(new DataSetOperation("org/zanata/test/model/LocalesData.dbunit.xml", DatabaseOperation.CLEAN_INSERT));
   }

   @BeforeMethod(firstTimeOnly = true)
   public void setup()
   {
      dao = new TextFlowDAO((Session) getEm().getDelegate());
   }

   // FIXME TextFlowDAO can't find the named query during the test
   @Test(enabled = false)
   public void getIdsWithTranslations()
   {
      List<Long> de = dao.findIdsWithTranslations(new LocaleId("de"));
      System.out.println(de);
      assertThat(de.size(), is(1));

      List<Long> es = dao.findIdsWithTranslations(new LocaleId("es"));
      System.out.println(es);
      assertThat(es.size(), is(0));

      List<Long> fr = dao.findIdsWithTranslations(new LocaleId("fr"));
      System.out.println(fr);
      assertThat(fr.size(), is(0));
   }

}
