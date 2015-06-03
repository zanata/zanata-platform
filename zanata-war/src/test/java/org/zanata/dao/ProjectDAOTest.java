package org.zanata.dao;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import org.dbunit.operation.DatabaseOperation;
import org.hibernate.Session;
import org.jboss.seam.security.Identity;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.zanata.ZanataDbunitJpaTest;
import org.zanata.model.HProject;

public class ProjectDAOTest extends ZanataDbunitJpaTest {

    private ProjectDAO dao;

    @Override
    protected void prepareDBUnitOperations() {
        beforeTestOperations.add(new DataSetOperation(
                "org/zanata/test/model/ProjectsData.dbunit.xml",
                DatabaseOperation.CLEAN_INSERT));
    }

    @BeforeClass
    public static void disableSecurity() {
        Identity.setSecurityEnabled(false);
    }

    @Before
    public void setup() {
        dao = new ProjectDAO((Session) getEm().getDelegate());
    }

    @Test
    public void getValidProjectBySlug() {
        HProject project = dao.getBySlug("sample-project");
        assertThat(project, notNullValue());
        assertThat(project.getName(), is("Sample Project"));
    }

    @Test
    public void getValidProjectById() {
        HProject project = dao.findById(1l, false);
        assertThat(project, notNullValue());
        assertThat(project.getName(), is("Sample Project"));
    }

    @Test
    public void getActiveIterations() {
        assertThat(dao.getActiveIterations("current-project").size(), is(1));
    }

    @Test
    public void getReadOnlyIterations() {
        assertThat(dao.getReadOnlyIterations("current-project").size(), is(1));
    }

    @Test
    public void getObsoleteIterations() {
        assertThat(dao.getObsoleteIterations("current-project").size(), is(1));
    }

    @Test
    public void getFilterProjectSizeAll() {
        assertThat(dao.getFilterProjectSize(false, false, false), is(4));
    }

    @Test
    public void getFilterProjectSizeOnlyActive() {
        assertThat(dao.getFilterProjectSize(false, true, true), is(2));
    }

    @Test
    public void getFilterProjectSizeOnlyReadOnly() {
        assertThat(dao.getFilterProjectSize(true, false, true), is(1));
    }

    @Test
    public void getFilterProjectSizeOnlyObsolete() {
        assertThat(dao.getFilterProjectSize(true, true, false), is(1));
    }

    @Test
    public void getFilterProjectSizeOnlyActiveAndReadOnly() {
        assertThat(dao.getFilterProjectSize(false, false, true), is(3));
    }

    @Test
    public void getFilterProjectSizeOnlyActiveAndObsolete() {
        assertThat(dao.getFilterProjectSize(false, true, false), is(3));
    }

    @Test
    public void getFilterProjectSizeOnlyObsoleteAndReadOnly() {
        assertThat(dao.getFilterProjectSize(true, false, false), is(2));
    }
}
