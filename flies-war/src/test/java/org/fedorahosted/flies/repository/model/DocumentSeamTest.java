package org.fedorahosted.flies.repository.model;



import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

import java.util.List;

import javax.persistence.EntityManager;

import org.dbunit.operation.DatabaseOperation;
import org.fedorahosted.flies.core.model.IterationProject;
import org.fedorahosted.flies.core.model.ProjectIteration;
import org.fedorahosted.flies.core.model.ProjectSeries;
import org.jboss.seam.mock.DBUnitSeamTest;
import org.testng.annotations.Test;

public class DocumentSeamTest extends DBUnitSeamTest {

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
    
    @Test
    public void myTsets() throws Exception{
        new FacesRequest() {

            protected void invokeApplication() throws Exception {
                EntityManager em = (EntityManager) getInstance("entityManager");
                /*
                ProjectContainer prCont = new ProjectContainer();
                for(int i=0;i<10;i++){
                	Document doc = new Document();
                	doc.setContentType("po");
                	doc.setName("mydoc "+i);
                	doc.setRevision(1);
                	//prCont.getItems().add(doc);
                }
                em.persist(prCont);
                
                assertNotNull(prCont.getId());
                */
                //assertNotNull(prCont.getItems().get(0).getId());
                
            	
            }
        }.run();
    }
}