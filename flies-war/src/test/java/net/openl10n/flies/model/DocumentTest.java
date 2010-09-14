package net.openl10n.flies.model;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.List;

import javax.persistence.EntityManager;

import net.openl10n.flies.FliesDbunitJpaTest;
import net.openl10n.flies.common.ContentType;
import net.openl10n.flies.common.LocaleId;
import net.openl10n.flies.dao.DocumentDAO;
import net.openl10n.flies.dao.LocaleDAO;

import org.dbunit.operation.DatabaseOperation;
import org.hibernate.Session;
import org.jboss.seam.security.Identity;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@Test(groups = { "jpa-tests" })
public class DocumentTest extends FliesDbunitJpaTest
{

   private DocumentDAO dao;
   private LocaleDAO localeDAO;
   HLocale en_US;
   HLocale de_DE;

   protected void prepareDBUnitOperations()
   {
      beforeTestOperations.add(new DataSetOperation("META-INF/testdata/ProjectsData.dbunit.xml", DatabaseOperation.CLEAN_INSERT));
      beforeTestOperations.add(new DataSetOperation("META-INF/testdata/LocalesData.dbunit.xml", DatabaseOperation.CLEAN_INSERT));
   }

   @BeforeClass
   void beforeClass()
   {
      Identity.setSecurityEnabled(false);
   }

   @BeforeMethod(firstTimeOnly = true)
   public void beforeMethod()
   {
      dao = new DocumentDAO((Session) getEm().getDelegate());
      localeDAO = new LocaleDAO((Session) em.getDelegate());
      en_US = localeDAO.findByLocaleId(LocaleId.EN_US);
      de_DE = localeDAO.findByLocaleId(new LocaleId("de-DE"));
   }

   @Test
   public void traverseProjectGraph() throws Exception
   {
      EntityManager em = getEm();
      HIterationProject project = em.find(HIterationProject.class, 1l);
      assertThat(project, notNullValue());

      List<HProjectIteration> projectTargets = project.getProjectIterations();
      assertThat("Project should have 2 targets", projectTargets.size(), is(2));

      HProjectIteration target = projectTargets.get(0);
      assertThat("Expect target with id 1", target.getId(), is(1l));
   }

   @Test
   public void checkPositionsNotNull() throws Exception
   {
      EntityManager em = getEm();
      HIterationProject project = em.find(HIterationProject.class, 1l);
      // assertThat( project, notNullValue() );

      HDocument hdoc = new HDocument("fullpath", ContentType.TextPlain, en_US);
      hdoc.setProjectIteration(project.getProjectIterations().get(0));

      List<HTextFlow> textFlows = hdoc.getTextFlows();
      HTextFlow flow1 = new HTextFlow(hdoc, "textflow1", "some content");
      HTextFlow flow2 = new HTextFlow(hdoc, "textflow2", "more content");
      textFlows.add(flow1);
      textFlows.add(flow2);
      em.persist(hdoc);
      em.flush();
      // em.clear();
      // hdoc = em.find(HDocument.class, docId);
      em.refresh(hdoc);

      List<HTextFlow> textFlows2 = hdoc.getTextFlows();
      assertThat(textFlows2.size(), is(2));
      flow1 = textFlows2.get(0);
      assertThat(flow1, notNullValue());
      flow2 = textFlows2.get(1);
      assertThat(flow2, notNullValue());

      // TODO: we should automate this...
      hdoc.incrementRevision();

      textFlows2.remove(flow1);
      flow1.setObsolete(true);
      dao.syncRevisions(hdoc, flow1);

      // flow1.setPos(null);
      em.flush();
      em.refresh(hdoc);
      em.refresh(flow1);
      em.refresh(flow2);
      assertThat(hdoc.getTextFlows().size(), is(1));
      flow2 = hdoc.getTextFlows().get(0);
      assertThat(flow2.getResId(), equalTo("textflow2"));

      flow1 = hdoc.getAllTextFlows().get("textflow1");
      // assertThat(flow1.getPos(), nullValue());
      assertThat(flow1.isObsolete(), is(true));
      assertThat(flow1.getRevision(), is(2));
      flow2 = hdoc.getAllTextFlows().get("textflow2");
      // assertThat(flow1.getPos(), is(0));
      assertThat(flow2.isObsolete(), is(false));
   }

   // FIXME this test only works if resources-dev is on the classpath
   @SuppressWarnings("unchecked")
   @Test
   public void ensureHistoryOnTextFlow()
   {
      EntityManager em = getEm();
      HIterationProject project = em.find(HIterationProject.class, 1l);
      // assertThat( project, notNullValue() );

      HDocument hdoc = new HDocument("fullpath", ContentType.TextPlain, en_US);
      hdoc.setProjectIteration(project.getProjectIterations().get(0));

      List<HTextFlow> textFlows = hdoc.getTextFlows();
      HTextFlow flow1 = new HTextFlow(hdoc, "textflow3", "some content");
      HTextFlow flow2 = new HTextFlow(hdoc, "textflow4", "more content");
      textFlows.add(flow1);
      textFlows.add(flow2);
      em.persist(hdoc);
      em.flush();

      hdoc.incrementRevision();

      flow1.setContent("nwe content!");

      dao.syncRevisions(hdoc, flow1);

      em.flush();

      HTextFlowTarget target = new HTextFlowTarget(flow1, de_DE);
      target.setContent("hello world");
      em.persist(target);
      em.flush();
      target.setContent("h2");
      em.flush();

      List<HTextFlowTargetHistory> hist = em.createQuery("from HTextFlowTargetHistory h where h.textFlowTarget =:target").setParameter("target", target).getResultList();
      assertThat(hist, notNullValue());
      assertThat(hist.size(), not(0));

   }

}