package org.zanata.dao;

import org.dbunit.operation.DatabaseOperation;
import org.hibernate.Session;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.zanata.ZanataDbunitJpaTest;
import org.zanata.model.HPerson;
import org.zanata.model.HProject;
import org.zanata.security.ZanataIdentity;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class ProjectDAOTest extends ZanataDbunitJpaTest {

    private ProjectDAO dao;

    private PersonDAO personDAO;

    @Override
    protected void prepareDBUnitOperations() {
        beforeTestOperations.add(new DataSetOperation(
                "org/zanata/test/model/ProjectsData.dbunit.xml",
                DatabaseOperation.CLEAN_INSERT));
        beforeTestOperations.add(new DataSetOperation(
                "org/zanata/test/model/AccountData.dbunit.xml",
                DatabaseOperation.CLEAN_INSERT));
    }

    @BeforeClass
    public static void disableSecurity() {
        ZanataIdentity.setSecurityEnabled(false);
    }

    @Before
    public void setup() {
        dao = new ProjectDAO((Session) getEm().getDelegate());
        personDAO = new PersonDAO((Session) getEm().getDelegate());
    }

    @Test
    public void getValidProjectBySlug() {
        HProject project = dao.getBySlug("sample-project");
        assertThat(project).isNotNull();
        assertThat(project.getName()).isEqualTo("Sample Project");
    }

    @Test
    public void getValidProjectById() {
        HProject project = dao.findById(1l, false);
        assertThat(project).isNotNull();
        assertThat(project.getName()).isEqualTo("Sample Project");
    }

    @Test
    public void getActiveIterations() {
        assertThat(dao.getActiveIterations("current-project").size()).isEqualTo(1);
    }

    @Test
    public void getReadOnlyIterations() {
        assertThat(dao.getReadOnlyIterations("current-project").size()).isEqualTo(1);
    }

    @Test
    public void getObsoleteIterations() {
        assertThat(dao.getObsoleteIterations("current-project").size()).isEqualTo(1);
    }

    @Test
    public void getFilterProjectSizeAll() {
        assertThat(dao.getFilterProjectSize(false, false, false, null)).isEqualTo(4);
    }

    @Test
    public void getFilterProjectSizeOnlyActive() {
        assertThat(dao.getFilterProjectSize(false, true, true, null)).isEqualTo(2);
    }

    @Test
    public void getFilterProjectSizeOnlyReadOnly() {
        assertThat(dao.getFilterProjectSize(true, false, true, null)).isEqualTo(1);
    }

    @Test
    public void getFilterProjectSizeOnlyObsolete() {
        assertThat(dao.getFilterProjectSize(true, true, false, null)).isEqualTo(1);
    }

    @Test
    public void getFilterProjectSizeOnlyActiveAndReadOnly() {
        assertThat(dao.getFilterProjectSize(false, false, true, null)).isEqualTo(3);
    }

    @Test
    public void getFilterProjectSizeOnlyActiveAndObsolete() {
        assertThat(dao.getFilterProjectSize(false, true, false, null)).isEqualTo(3);
    }

    @Test
    public void getFilterProjectSizeOnlyObsoleteAndReadOnly() {
        assertThat(dao.getFilterProjectSize(true, false, false, null)).isEqualTo(2);
    }

    @Test
    public void getOffsetList() {
        List<HProject> projects =
                dao.getOffsetList(-1, -1, false, false, false, null);
        assertThat(projects.size()).isEqualTo(4);
    }

    @Test
    public void getOffsetListPrivateProject() {
        HPerson person = personDAO.findById(4L);
        List<HProject> projects = dao.getOffsetList(-1, -1, false, false, false, person);
        assertThat(projects).hasSize(5);
    }
}
