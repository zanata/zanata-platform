package org.zanata.dao;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import org.dbunit.operation.DatabaseOperation;
import org.hamcrest.Matchers;
import org.hibernate.Session;
import org.hibernate.ejb.HibernateEntityManagerFactory;
import org.junit.Before;
import org.junit.Test;
import org.zanata.ZanataDbunitJpaTest;
import org.zanata.model.HProject;
import org.zanata.model.HProjectIteration;
import org.zanata.model.HTextFlow;
import org.zanata.util.CloseableIterator;
import com.google.common.collect.Iterators;

public class TextFlowStreamingDAOTest extends ZanataDbunitJpaTest {

    private static final int TEXTFLOWS_IN_SAMPLE_PROJECT_10 = 5;
    private ProjectDAO projectDao;
    private ProjectIterationDAO projectIterDao;
    private TextFlowStreamingDAO dao;
    private Session session;

    @Override
    protected void prepareDBUnitOperations() {
        beforeTestOperations.add(new DataSetOperation(
                "org/zanata/test/model/ProjectsData.dbunit.xml",
                DatabaseOperation.CLEAN_INSERT));
        beforeTestOperations.add(new DataSetOperation(
                "org/zanata/test/model/TextFlowTestData.dbunit.xml",
                DatabaseOperation.CLEAN_INSERT));
        beforeTestOperations.add(new DataSetOperation(
                "org/zanata/test/model/LocalesData.dbunit.xml",
                DatabaseOperation.CLEAN_INSERT));
        beforeTestOperations.add(new DataSetOperation(
                "org/zanata/test/model/AccountData.dbunit.xml",
                DatabaseOperation.CLEAN_INSERT));
    }

    @Before
    public void setup() {
        dao = new TextFlowStreamingDAO(
                (HibernateEntityManagerFactory) getEmf());
        session = getSession();
        projectDao = new ProjectDAO(session);
        projectIterDao = new ProjectIterationDAO(session);
    }

    @Test
    public void findAllTextFlows() throws Exception {
        CloseableIterator<HTextFlow> iter = dao.findTextFlows();
        try {
            assertThat(Iterators.size(iter),
                    equalTo(TEXTFLOWS_IN_SAMPLE_PROJECT_10));
        } finally {
            if (iter != null) {
                iter.close();
            }
        }
    }

    @Test
    public void findTextFlowsForProject() throws Exception {
        HProject proj = projectDao.getBySlug("sample-project");
        CloseableIterator<HTextFlow> iter = dao.findTextFlowsByProject(proj);
        try {
            assertThat(Iterators.size(iter),
                    equalTo(TEXTFLOWS_IN_SAMPLE_PROJECT_10));
        } finally {
            if (iter != null) {
                iter.close();
            }
        }
    }

    @Test
    public void findTextFlowsForEmptyProject() throws Exception {
        HProject proj = projectDao.getBySlug("retired-project");
        CloseableIterator<HTextFlow> iter = dao.findTextFlowsByProject(proj);
        try {
            assertThat(iter.hasNext(), Matchers.not(true));
        } finally {
            if (iter != null) {
                iter.close();
            }
        }
    }

    @Test
    public void findTextFlowsForProjectIter() throws Exception {
        HProjectIteration projIter =
                projectIterDao.getBySlug("sample-project", "1.0");
        CloseableIterator<HTextFlow> iter =
                dao.findTextFlowsByProjectIteration(projIter);
        try {
            assertThat(Iterators.size(iter),
                    equalTo(TEXTFLOWS_IN_SAMPLE_PROJECT_10));
        } finally {
            if (iter != null) {
                iter.close();
            }
        }
    }

    @Test
    public void findTextFlowsForEmptyProjectIteration() throws Exception {
        HProjectIteration projIter =
                projectIterDao.getBySlug("retired-project", "retired-current");
        CloseableIterator<HTextFlow> iter =
                dao.findTextFlowsByProjectIteration(projIter);
        try {
            assertThat(iter.hasNext(), Matchers.not(true));
        } finally {
            if (iter != null) {
                iter.close();
            }
        }
    }
}
