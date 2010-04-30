package org.fedorahosted.flies.repository.model;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.*;

import java.util.List;

import javax.persistence.EntityManager;

import org.dbunit.operation.DatabaseOperation;
import org.fedorahosted.flies.FliesDbunitJpaTest;
import org.fedorahosted.flies.common.ContentType;
import org.fedorahosted.flies.common.LocaleId;
import org.fedorahosted.flies.core.model.HIterationProject;
import org.fedorahosted.flies.core.model.HProjectIteration;
import org.fedorahosted.flies.core.model.HProjectSeries;
import org.testng.annotations.Test;

@Test(groups = { "jpa-tests" })
public class DocumentSeamTest extends FliesDbunitJpaTest {

	protected void prepareDBUnitOperations() {
		beforeTestOperations.add(new DataSetOperation(
				"META-INF/testdata/ProjectsData.dbunit.xml",
				DatabaseOperation.CLEAN_INSERT));
	}

	@Test
	public void traverseProjectGraph() throws Exception {
		EntityManager em = getEm();
		HIterationProject project = em.find(HIterationProject.class, 1l);
		assertThat(project, notNullValue());

		List<HProjectSeries> projectSeries = project.getProjectSeries();
		assertThat("Project should have 1 series", projectSeries.size(), is(1));

		List<HProjectIteration> projectTargets = project.getProjectIterations();
		assertThat("Project should have 2 targets", projectTargets.size(),
				is(2));

		HProjectIteration target = projectTargets.get(0);
		assertThat("Expect target with id 1", target.getId(), is(1l));
	}

	@Test
	public void checkPositionsNotNull() throws Exception {
		EntityManager em = getEm();
		HIterationProject project = em.find(HIterationProject.class, 1l);
		// assertThat( project, notNullValue() );

		HDocument hdoc = new HDocument("fullpath", ContentType.TextPlain,
				LocaleId.EN_US);
		hdoc.setProject(project.getProjectIterations().get(0).getContainer());

		List<HTextFlow> textFlows = hdoc.getTextFlows();
		HTextFlow flow1 = new HTextFlow(hdoc, "textflow1", "some content");
		HTextFlow flow2 = new HTextFlow(hdoc, "textflow2", "more content");
		textFlows.add(flow1);
		textFlows.add(flow2);
		em.persist(hdoc);
		Long docId = hdoc.getId();
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

		textFlows2.remove(flow1);
		flow1.setObsolete(true);
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
		flow2 = hdoc.getAllTextFlows().get("textflow2");
		// assertThat(flow1.getPos(), is(0));
		assertThat(flow2.isObsolete(), is(false));
	}

}