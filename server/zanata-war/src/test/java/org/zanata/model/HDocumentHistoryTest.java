package org.zanata.model;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.Date;
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

public class HDocumentHistoryTest extends ZanataDbunitJpaTest
{
   private LocaleDAO localeDAO;
   HLocale de_DE;

   @BeforeMethod(firstTimeOnly = true)
   public void beforeMethod()
   {
      localeDAO = new LocaleDAO((Session) em.getDelegate());
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
      HDocument d = new HDocument("/path/to/document.txt", ContentType.TextPlain, de_DE);
      d.setProjectIteration((HProjectIteration) session.load(HProjectIteration.class, 1L));
      session.save(d);
      session.flush();

      Date lastChanged = d.getLastChanged();

      d.incrementRevision();
      d.setContentType(ContentType.PO);
      session.update(d);
      session.flush();

      List<HDocumentHistory> historyElems = loadHistory(d);

      assertThat(historyElems.size(), is(1));
      HDocumentHistory history = historyElems.get(0);
      assertThat(history.getDocId(), is(d.getDocId()));
      assertThat(history.getContentType(), is(ContentType.TextPlain));
      assertThat(history.getLastChanged(), is(lastChanged));
      assertThat(history.getLastModifiedBy(), nullValue());
      assertThat(history.getLocale().getLocaleId(), is(de_DE.getLocaleId()));
      assertThat(history.getName(), is(d.getName()));
      assertThat(history.getPath(), is(d.getPath()));
      assertThat(history.getRevision(), is(d.getRevision() - 1));

      d.incrementRevision();
      d.setName("name2");
      session.update(d);
      session.flush();

      historyElems = loadHistory(d);
      assertThat(historyElems.size(), is(2));

   }

   @SuppressWarnings("unchecked")
   private List<HDocumentHistory> loadHistory(HDocument d)
   {
      return getSession().createCriteria(HDocumentHistory.class).add(Restrictions.eq("document", d)).list();
   }

}
