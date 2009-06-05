package org.fedorahosted.flies.repository.model;



import org.dbunit.operation.DatabaseOperation;
import org.fedorahosted.flies.core.model.IterationProject;
import org.fedorahosted.flies.core.model.Person;
import org.fedorahosted.flies.core.model.Project;
import org.fedorahosted.flies.core.model.ProjectSeries;
import org.fedorahosted.flies.core.model.ProjectIteration;
import org.jboss.seam.mock.DBUnitSeamTest;
import org.testng.annotations.Test;

import javax.persistence.EntityManager;
import java.util.List;
import java.util.Date;
import static org.testng.Assert.*;

public class DocumentTests extends DBUnitSeamTest {

    protected void prepareDBUnitOperations() {
        beforeTestOperations.add(
                new DataSetOperation("org/fedorahosted/flies/repository/model/ProjectsBaseData.dbunit.xml", DatabaseOperation.CLEAN_INSERT)
        );
    }

    @Test
    public void traverseProjectGraph() throws Exception {
        new FacesRequest() {

            protected void invokeApplication() throws Exception {
                EntityManager em = (EntityManager) getInstance("entityManager");
                IterationProject project = em.find(IterationProject.class, 1l);
                assertNotNull(project, "Failed to find project by Id");
                
                List<ProjectSeries> projectSeries =  project.getProjectSeries();
                assertEquals(projectSeries.size(), 1, "Project should have n series");
                
                List<ProjectIteration> projectTargets = project.getProjectIterations();
                assertEquals(projectTargets.size(), 1, "Project should have n targets");
                
                ProjectIteration target = projectTargets.get(0);
                assertEquals(target.getId(), Long.valueOf(1l), "expected target with id 1");
                
            }
        }.run();
    }
}