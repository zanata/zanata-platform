package net.openl10n.flies.model;

import java.util.List;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.*;

import net.openl10n.flies.FliesDbunitJpaTest;
import net.openl10n.flies.common.ContentType;
import net.openl10n.flies.model.HDocument;
import net.openl10n.flies.model.HProjectIteration;
import net.openl10n.flies.model.HTextFlow;
import net.openl10n.flies.model.HTextFlowTarget;
import net.openl10n.flies.model.HTextFlowTargetHistory;
import net.openl10n.flies.service.LocaleService;

import org.dbunit.operation.DatabaseOperation;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.testng.annotations.Test;

public class HTextFlowTargetHistoryTest extends FliesDbunitJpaTest
{
   private LocaleService localeServiceImpl;

   protected void prepareDBUnitOperations()
   {
      beforeTestOperations.add(new DataSetOperation("META-INF/testdata/ProjectsData.dbunit.xml", DatabaseOperation.CLEAN_INSERT));
   }

   @Test
   public void ensureHistoryIsRecorded()
   {
      Session session = getSession();
      HDocument d = new HDocument("/path/to/document.txt", ContentType.TextPlain, localeServiceImpl.getDefautLanguage());
      d.setProjectIteration((HProjectIteration) session.load(HProjectIteration.class, 1L));
      session.save(d);
      session.flush();

      HTextFlow tf = new HTextFlow(d, "mytf", "hello world");
      d.getTextFlows().add(tf);
      session.flush();

      HTextFlowTarget target = new HTextFlowTarget(tf, localeServiceImpl.getDefautLanguage());
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
