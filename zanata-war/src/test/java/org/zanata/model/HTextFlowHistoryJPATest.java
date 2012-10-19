package org.zanata.model;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.Arrays;
import java.util.List;

import org.dbunit.operation.DatabaseOperation;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.zanata.ZanataDbunitJpaTest;
import org.zanata.common.ContentType;
import org.zanata.common.LocaleId;
import org.zanata.dao.LocaleDAO;

public class HTextFlowHistoryJPATest extends ZanataDbunitJpaTest
{
   private LocaleDAO localeDAO;
   HLocale en_US;

   @BeforeMethod(firstTimeOnly = true)
   public void beforeMethod()
   {
      localeDAO = new LocaleDAO((Session) em.getDelegate());
      en_US = localeDAO.findByLocaleId(LocaleId.EN_US);
   }

   @Override
   protected void prepareDBUnitOperations()
   {
      beforeTestOperations.add(new DataSetOperation("org/zanata/test/model/ProjectsData.dbunit.xml", DatabaseOperation.CLEAN_INSERT));
      beforeTestOperations.add(new DataSetOperation("org/zanata/test/model/LocalesData.dbunit.xml", DatabaseOperation.CLEAN_INSERT));
   }

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

      List<HTextFlowHistory> historyElems = getHistory(tf);

      assertThat("Incorrect History size on persist", historyElems.size(), is(0));

      d.incrementRevision();
      tf.setContents("hello world again");
      tf.setRevision(d.getRevision());
      session.flush();

      historyElems = getHistory(tf);

      assertThat("Incorrect History size on first update", historyElems.size(), is(1));
      assertThat(historyElems.get(0).getContents(), is(Arrays.asList("hello world")));
      
      d.incrementRevision();
      tf.setContents("hello world a third time");
      tf.setRevision( d.getRevision() );
      session.flush();
      
      historyElems = getHistory(tf);

      assertThat("Incorrect History size on second update", historyElems.size(), is(2));
      assertThat(historyElems.get(1).getContents(), is(Arrays.asList("hello world again")));
   }

   @Test
   public void ensureHistoryIsRecordedPlural()
   {
      Session session = getSession();
      HDocument d = new HDocument("/path/to/document.txt", ContentType.TextPlain, en_US);
      d.setProjectIteration((HProjectIteration) session.load(HProjectIteration.class, 1L));
      session.save(d);
      session.flush();

      HTextFlow tf = new HTextFlow(d, "mytf", "hello world");
      d.getTextFlows().add(tf);

      session.flush();

      List<HTextFlowHistory> historyElems = getHistory(tf);

      assertThat("Incorrect History size on persist", historyElems.size(), is(0));

      d.incrementRevision();
      tf.setContents("hello world 1", "hello world 2");
      tf.setRevision(d.getRevision());
      session.flush();

      historyElems = getHistory(tf);

      assertThat("Incorrect History size on first update", historyElems.size(), is(1));
      assertThat(historyElems.get(0).getContents(), is(Arrays.asList("hello world")));

      d.incrementRevision();
      tf.setContents("hello world 4", "hello world 5", "hellow world 6");
      tf.setRevision( d.getRevision() );
      session.flush();

      historyElems = getHistory(tf);

      assertThat("Incorrect History size on second update", historyElems.size(), is(2));
      assertThat(historyElems.get(1).getContents(), is(Arrays.asList("hello world 1", "hello world 2")));
   }

   @SuppressWarnings("unchecked")
   private List<HTextFlowHistory> getHistory(HTextFlow tf)
   {
      return getSession().createCriteria(HTextFlowHistory.class).add(Restrictions.eq("textFlow", tf)).list();
   }
}
