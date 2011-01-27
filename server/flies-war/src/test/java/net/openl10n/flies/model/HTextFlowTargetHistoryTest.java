package net.openl10n.flies.model;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.List;

import net.openl10n.flies.FliesDbunitJpaTest;
import net.openl10n.flies.common.ContentType;
import net.openl10n.flies.common.LocaleId;
import net.openl10n.flies.dao.LocaleDAO;

import org.dbunit.operation.DatabaseOperation;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class HTextFlowTargetHistoryTest extends FliesDbunitJpaTest
{
   private LocaleDAO localeDAO;
   HLocale en_US;
   HLocale de_DE;

   @BeforeMethod(firstTimeOnly = true)
   public void beforeMethod()
   {
      localeDAO = new LocaleDAO((Session) em.getDelegate());
      en_US = localeDAO.findByLocaleId(LocaleId.EN_US);
      de_DE = localeDAO.findByLocaleId(new LocaleId("de"));
   }

   @Override
   protected void prepareDBUnitOperations()
   {
      beforeTestOperations.add(new DataSetOperation("META-INF/testdata/ProjectsData.dbunit.xml", DatabaseOperation.CLEAN_INSERT));
      beforeTestOperations.add(new DataSetOperation("META-INF/testdata/LocalesData.dbunit.xml", DatabaseOperation.CLEAN_INSERT));
   }

   // FIXME this test only works if resources-dev is on the classpath
   @Test
   public void ensureHistoryIsRecorded()
   {
      Session session = getSession();
      HDocument d = new HDocument("/path/to/document.txt", ContentType.TextPlain, en_US);
      d.setProjectIteration((HProjectIteration) session.load(HProjectIteration.class, 1L));
      session.save(d);
      session.flush();

      HTextFlow tf = new HTextFlow(d, "mytf", "hello world");
      d.getTextFlows().add(tf);
      session.flush();

      HTextFlowTarget target = new HTextFlowTarget(tf, de_DE);
      target.setContent("helleu world");
      session.save(target);
      session.flush();

      List<HTextFlowTargetHistory> historyElems = getHistory(target);
      assertThat(historyElems.size(), is(0));

      target.setContent("blah!");
      session.flush();

      historyElems = getHistory(target);

      assertThat(historyElems.size(), is(1));

   }

   @SuppressWarnings("unchecked")
   private List<HTextFlowTargetHistory> getHistory(HTextFlowTarget tft)
   {
      return getSession().createCriteria(HTextFlowTargetHistory.class).add(Restrictions.eq("textFlowTarget", tft)).list();

   }
}
