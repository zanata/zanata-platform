package org.zanata.model;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.List;

import javax.persistence.EntityManager;

import org.dbunit.operation.DatabaseOperation;
import org.hamcrest.Matchers;
import org.hibernate.Session;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.zanata.ZanataDbunitJpaTest;
import org.zanata.common.ContentType;
import org.zanata.common.LocaleId;
import org.zanata.dao.LocaleDAO;
import org.zanata.security.ZanataIdentity;
import com.google.common.base.Function;
import com.google.common.collect.Lists;

public class DocumentJPATest extends ZanataDbunitJpaTest {

    private LocaleDAO localeDAO;
    HLocale en_US;
    HLocale de_DE;

    protected void prepareDBUnitOperations() {
        beforeTestOperations.add(new DataSetOperation(
                "org/zanata/test/model/ProjectsData.dbunit.xml",
                DatabaseOperation.CLEAN_INSERT));
        beforeTestOperations.add(new DataSetOperation(
                "org/zanata/test/model/LocalesData.dbunit.xml",
                DatabaseOperation.CLEAN_INSERT));
    }

    private void syncRevisions(HDocument doc, HTextFlow... textFlows) {
        int rev = doc.getRevision();
        syncRevisions(doc, rev, textFlows);
    }

    private void syncRevisions(HDocument doc, int revision,
            HTextFlow... textFlows) {
        doc.setRevision(revision);
        for (HTextFlow textFlow : textFlows) {
            textFlow.setRevision(revision);
        }
    }

    @BeforeClass
    public static void disableSecurity() {
        ZanataIdentity.setSecurityEnabled(false);
    }

    @Before
    public void beforeMethod() {
        localeDAO = new LocaleDAO((Session) em.getDelegate());
        en_US = localeDAO.findByLocaleId(LocaleId.EN_US);
        de_DE = localeDAO.findByLocaleId(new LocaleId("de"));
    }

    @Test
    public void traverseProjectGraph() throws Exception {
        EntityManager em = getEm();
        HProject project = em.find(HProject.class, 1l);
        assertThat(project, notNullValue());

        List<HProjectIteration> projectTargets = project.getProjectIterations();
        assertThat("Project should have 3 targets", projectTargets.size(),
                is(3));

        List<Long> iterationIds = Lists.transform(projectTargets,
                new Function<HProjectIteration, Long>() {
                    @Override
                    public Long apply(HProjectIteration input) {
                        return input.getId();
                    }
                });
        assertThat(iterationIds, Matchers.containsInAnyOrder(1L, 2L, 900L));
    }

    @Test
    public void checkPositionsNotNull() throws Exception {
        EntityManager em = getEm();
        HProject project = em.find(HProject.class, 1l);
        // assertThat( project, notNullValue() );

        HDocument hdoc =
                new HDocument("fullpath", ContentType.TextPlain, en_US);
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
        syncRevisions(hdoc, flow1);

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

    // FIXME this test only works if resources-dev is on the classpath.
    // workaround (disabled history)
    @SuppressWarnings("unchecked")
    @Ignore
    @Test
    public void ensureHistoryOnTextFlow() {
        EntityManager em = getEm();
        HProject project = em.find(HProject.class, 1l);
        // assertThat( project, notNullValue() );

        HDocument hdoc =
                new HDocument("fullpath", ContentType.TextPlain, en_US);
        hdoc.setProjectIteration(project.getProjectIterations().get(0));

        List<HTextFlow> textFlows = hdoc.getTextFlows();
        HTextFlow flow1 = new HTextFlow(hdoc, "textflow3", "some content");
        HTextFlow flow2 = new HTextFlow(hdoc, "textflow4", "more content");
        textFlows.add(flow1);
        textFlows.add(flow2);
        em.persist(hdoc);
        em.flush();

        hdoc.incrementRevision();

        flow1.setContents("nwe content!");

        syncRevisions(hdoc, flow1);

        em.flush();

        HTextFlowTarget target = new HTextFlowTarget(flow1, de_DE);
        target.setContents("hello world");
        em.persist(target);
        em.flush();
        target.setContents("h2");
        em.flush();

        List<HTextFlowTargetHistory> hist =
                em.createQuery(
                        "from HTextFlowTargetHistory h where h.textFlowTarget =:target")
                        .setParameter("target", target).getResultList();
        assertThat(hist, notNullValue());
        assertThat(hist.size(), not(0));

    }

}
