package org.zanata.dao;

import org.dbunit.operation.DatabaseOperation;
import org.hibernate.Session;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.zanata.ZanataDbunitJpaTest;
import org.zanata.model.HPerson;
import org.zanata.model.HProject;
import org.zanata.seam.security.AltCurrentUser;
import org.zanata.security.ZanataIdentity;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.zanata.test.model.ProjectsDataKt.*;

public class ProjectDAOTest extends ZanataDbunitJpaTest {

    private ProjectDAO dao;
    private PersonDAO personDAO;
    private AltCurrentUser currentUser = new AltCurrentUser();

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
        dao = new ProjectDAO((Session) getEm().getDelegate(),
                currentUser);
        personDAO = new PersonDAO((Session) getEm().getDelegate());
    }

    // NB note that these tests all use the projects defined by ProjectsData.dbunit.xml



    @Test
    public void getValidProjectBySlug() {
        HProject project = dao.getBySlug("sample-project");
        assertThat(project).isNotNull();
        assertThat(project.getName()).isEqualTo("Sample Project");
    }

    @Test
    public void getValidProjectById() {
        // project 1 in ProjectsData.dbunit.xml is sample-project
        HProject project = dao.findById(PROJECT_ID_1, false);
        assertThat(project).isNotNull();
        assertThat(project.getName()).isEqualTo("Sample Project");
    }

    @Test
    public void activeIterationsInCurrentProject() {
        assertThat(dao.getActiveIterations("current-project").size())
                .isEqualTo(ACTIVE_ITERS_IN_CURRENT_PROJ);
    }

    @Test
    public void readOnlyIterationsInCurrentProject() {
        assertThat(dao.getReadOnlyIterations("current-project").size())
                .isEqualTo(READONLY_ITERS_IN_CURRENT_PROJ);
    }

    @Test
    public void obsoleteIterationsInCurrentProject() {
        assertThat(dao.getObsoleteIterations("current-project").size())
                .isEqualTo(OBSOLETE_ITERS_IN_CURRENT_PROJ);
    }

    // NB For the following getFilterProjectSize tests, note that the boolean
    // parameters have these names in this order:
    //     filterOutActive, filterOutReadonly, filterOutObsolete
    //
    // A project status can only be active, read-only or obsolete, so
    // filtering out active and read-only will leave only obsolete projects
    // (for example).

    @Test
    public void getFilterProjectSizeAllExceptPrivate() {
        assertThat(dao.getFilterProjectSize(false, false, false))
                .isEqualTo(ACTIVE_PUBLIC_PROJECTS + READONLY_PUBLIC_PROJECTS + OBSOLETE_PUBLIC_PROJECTS);
    }

    @Test
    public void getFilterProjectSizeOnlyActiveButNotPrivate() {
        assertThat(dao.getFilterProjectSize(false, true, true))
                .isEqualTo(ACTIVE_PUBLIC_PROJECTS);
    }

    @Test
    public void getFilterProjectSizeOnlyReadOnlyButNotPrivate() {
        // ie non-active/non-obsolete projects
        assertThat(dao.getFilterProjectSize(true, false, true))
                .isEqualTo(READONLY_PUBLIC_PROJECTS);
    }

    @Test
    public void getFilterProjectSizeOnlyObsoleteButNotPrivate() {
        // ie non-readonly/non-active projects
        assertThat(dao.getFilterProjectSize(true, true, false)).isEqualTo(OBSOLETE_PUBLIC_PROJECTS);
    }

    @Test
    public void getFilterProjectSizeOnlyActiveAndReadOnlyButNotPrivate() {
        // ie non-obsolete projects
        assertThat(dao.getFilterProjectSize(false, false, true)).isEqualTo(ACTIVE_PUBLIC_PROJECTS + READONLY_PUBLIC_PROJECTS);
    }

    @Test
    public void getFilterProjectSizeOnlyActiveAndObsoleteButNotPrivate() {
        // ie non-readonly projects
        assertThat(dao.getFilterProjectSize(false, true, false)).isEqualTo(ACTIVE_PUBLIC_PROJECTS + OBSOLETE_PUBLIC_PROJECTS);
    }

    @Test
    public void getFilterProjectSizeOnlyObsoleteAndReadOnlyButNotPrivate() {
        // ie inactive projects
        assertThat(dao.getFilterProjectSize(true, false, false)).isEqualTo(OBSOLETE_PUBLIC_PROJECTS + READONLY_PUBLIC_PROJECTS);
    }

    @Test
    public void getOffsetListForAllProjectsButNotPrivate() {
        List<HProject> projects =
                dao.getOffsetList(-1, -1, false, false, false);
        // all projects in ProjectsData.dbunit.xml, except the private project
        assertThat(projects.size()).isEqualTo(ACTIVE_PUBLIC_PROJECTS + OBSOLETE_PUBLIC_PROJECTS + READONLY_PUBLIC_PROJECTS);
        int size = dao.getFilterProjectSize(false, false, false);
        assertThat(projects.size()).isEqualTo(size);
    }

    @Test
    public void getOffsetListForAllProjectsIncludingPrivate() {
        HPerson person = personDAO.findById(PERSON_ID_4);
        currentUser.account = person.getAccount();
        List<HProject> projects = dao.getOffsetList(-1, -1, false, false, false);
        // all projects in ProjectsData.dbunit.xml, including the private project
        assertThat(projects).hasSize(ACTIVE_PUBLIC_PROJECTS + OBSOLETE_PUBLIC_PROJECTS + READONLY_PUBLIC_PROJECTS + PROJECTS_FOR_PERSON_4);
        int size = dao.getFilterProjectSize(false, false, false);
        assertThat(projects.size()).isEqualTo(size);
    }

    @Test
    public void getProjectsForMember() {
        HPerson person = personDAO.findById(4L);
        List<HProject> projects =
                dao.getProjectsForMember(person, null, 0, 10);
        assertThat(projects).hasSize(1);

    }

    @Test
    public void getProjectsForMemberCount() {
        HPerson person = personDAO.findById(4L);
        int count = dao.getProjectsForMemberCount(person, null);
        assertThat(count).isEqualTo(1);
    }
}
