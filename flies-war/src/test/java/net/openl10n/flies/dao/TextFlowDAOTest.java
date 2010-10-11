package net.openl10n.flies.dao;

import java.util.List;

import net.openl10n.flies.FliesDbunitJpaTest;
import net.openl10n.flies.common.ContentState;
import net.openl10n.flies.common.LocaleId;
import net.openl10n.flies.dao.TextFlowDAO;

import org.dbunit.operation.DatabaseOperation;
import org.hibernate.Session;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.*;

@Test(groups = { "jpa-tests" })
public class TextFlowDAOTest extends FliesDbunitJpaTest
{

   private TextFlowDAO dao;

   @Override
   protected void prepareDBUnitOperations()
   {
      beforeTestOperations.add(new DataSetOperation("net/openl10n/flies/test/model/TextFlowTestData.dbunit.xml", DatabaseOperation.CLEAN_INSERT));
      beforeTestOperations.add(new DataSetOperation("META-INF/testdata/LocalesData.dbunit.xml", DatabaseOperation.CLEAN_INSERT));
   }

   @BeforeMethod(firstTimeOnly = true)
   public void setup()
   {
      dao = new TextFlowDAO((Session) getEm().getDelegate());
   }

   public void getIdsWithTranslations()
   {
      List<Long> de = dao.getIdsByTargetState(new LocaleId("de"), ContentState.Approved);
      System.out.println(de);
      assertThat(de.size(), is(1));

      List<Long> es = dao.getIdsByTargetState(new LocaleId("es"), ContentState.Approved);
      System.out.println(es);
      assertThat(es.size(), is(0));

      List<Long> fr = dao.getIdsByTargetState(new LocaleId("fr"), ContentState.Approved);
      System.out.println(fr);
      assertThat(fr.size(), is(0));
   }

}
