package org.fedorahosted.flies.repository.model;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.*;

import java.util.List;

import javax.persistence.EntityManager;

import org.dbunit.operation.DatabaseOperation;
import org.fedorahosted.flies.core.model.HIterationProject;
import org.fedorahosted.flies.core.model.ProjectIteration;
import org.fedorahosted.flies.core.model.ProjectSeries;
import org.jboss.seam.mock.DBUnitSeamTest;
import org.testng.annotations.Test;

@Test(groups={"seam-tests"})
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
                HIterationProject project = em.find(HIterationProject.class, 1l);
                assertThat( project, notNullValue() );
                
                List<ProjectSeries> projectSeries =  project.getProjectSeries();
                assertThat ("Project should have 1 series", projectSeries.size(), is(1));
                
                List<ProjectIteration> projectTargets = project.getProjectIterations();
                assertThat ("Project should have 2 targets", projectTargets.size(), is(2));
                
                ProjectIteration target = projectTargets.get(0);
                assertThat ("Expect target with id 1", target.getId(), is(1l));
                
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