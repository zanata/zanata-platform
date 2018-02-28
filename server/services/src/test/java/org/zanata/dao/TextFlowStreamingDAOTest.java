package org.zanata.dao;

import org.dbunit.operation.DatabaseOperation;
import org.hibernate.Session;
import org.junit.Before;
import org.junit.Test;
import org.zanata.ZanataDbunitJpaTest;
import org.zanata.common.LocaleId;
import org.zanata.model.HProject;
import org.zanata.model.HProjectIteration;
import org.zanata.model.HTextFlow;
import org.zanata.seam.security.AltCurrentUser;
import org.zanata.util.CloseableIterator;
import com.google.common.collect.Iterators;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

public class TextFlowStreamingDAOTest extends ZanataDbunitJpaTest {

    private static final int TEXTFLOWS_IN_SAMPLE_PROJECT_10 = 5;
    private ProjectDAO projectDao;
    private ProjectIterationDAO projectIterDao;
    private TextFlowStreamingDAO dao;

    @Override
    protected void prepareDBUnitOperations() {
        beforeTestOperations.add(new DataSetOperation(
                "org/zanata/test/model/LocalesData.dbunit.xml",
                DatabaseOperation.CLEAN_INSERT));
        beforeTestOperations.add(new DataSetOperation(
                "org/zanata/test/model/AccountData.dbunit.xml",
                DatabaseOperation.CLEAN_INSERT));
        beforeTestOperations.add(new DataSetOperation(
                "org/zanata/test/model/ProjectsData.dbunit.xml",
                DatabaseOperation.CLEAN_INSERT));
        beforeTestOperations.add(new DataSetOperation(
                "org/zanata/test/model/TextFlowTestData.dbunit.xml",
                DatabaseOperation.CLEAN_INSERT));
    }

    @Before
    public void setup() {
        dao = new TextFlowStreamingDAO(getEmf().getSessionFactory());
        Session session = getSession();
        projectDao = new ProjectDAO(session, new AltCurrentUser());
        projectIterDao = new ProjectIterationDAO(session);
    }

    @Test
    public void findAllTextFlows() throws Exception {
        CloseableIterator<HTextFlow> iter = dao.findTextFlows(Optional.of(
                LocaleId.EN_US));
        try {
            assertThat(Iterators.size(iter)).isEqualTo(TEXTFLOWS_IN_SAMPLE_PROJECT_10);
        } finally {
            if (iter != null) {
                iter.close();
            }
        }
    }

    @Test
    public void findAllTextFlows2() throws Exception {
        CloseableIterator<HTextFlow> iter = dao.findTextFlows(Optional.of(LocaleId.DE));
        try {
            assertThat(Iterators.size(iter)).isEqualTo(0);
        } finally {
            if (iter != null) {
                iter.close();
            }
        }
    }

    @Test
    public void findAllTextFlowsNullSrcLocale() throws Exception {
        CloseableIterator<HTextFlow> iter = dao.findTextFlows(Optional.empty());
        try {
            assertThat(Iterators.size(iter)).isEqualTo(TEXTFLOWS_IN_SAMPLE_PROJECT_10);
        } finally {
            if (iter != null) {
                iter.close();
            }
        }
    }

    @Test
    public void findTextFlowsForProject() throws Exception {
        HProject proj = projectDao.getBySlug("sample-project");
        CloseableIterator<HTextFlow> iter =
                dao.findTextFlowsByProject(proj, Optional.of(LocaleId.EN_US));
        try {
            assertThat(Iterators.size(iter)).isEqualTo(
                    TEXTFLOWS_IN_SAMPLE_PROJECT_10);
        } finally {
            if (iter != null) {
                iter.close();
            }
        }
    }

    @Test
    public void findTextFlowsForEmptyProject() throws Exception {
        HProject proj = projectDao.getBySlug("retired-project");
        CloseableIterator<HTextFlow> iter =
                dao.findTextFlowsByProject(proj, Optional.of(LocaleId.EN_US));
        try {
            assertThat(iter.hasNext()).isFalse();
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
                dao.findTextFlowsByProjectIteration(projIter,
                        Optional.of(LocaleId.EN_US));
        try {
            assertThat(Iterators.size(iter))
                    .isEqualTo(TEXTFLOWS_IN_SAMPLE_PROJECT_10);
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
                dao.findTextFlowsByProjectIteration(projIter,
                        Optional.of(LocaleId.EN_US));
        try {
            assertThat(iter.hasNext()).isFalse();
        } finally {
            if (iter != null) {
                iter.close();
            }
        }
    }
}
