package org.fedorahosted.flies.repository.model;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.*;

import java.util.List;

import javax.persistence.EntityManager;

import org.dbunit.operation.DatabaseOperation;
import org.fedorahosted.flies.core.model.HIterationProject;
import org.fedorahosted.flies.core.model.HProjectIteration;
import org.fedorahosted.flies.core.model.HProjectSeries;
import org.hamcrest.core.IsEqual;
import org.jboss.seam.mock.DBUnitSeamTest;
import org.testng.annotations.Test;

@Test(groups={"seam-tests"})
public class GlossarySeamTest extends DBUnitSeamTest {

    protected void prepareDBUnitOperations() {
        beforeTestOperations.add(
                new DataSetOperation("META-INF/testdata/GlossaryData.dbunit.xml", DatabaseOperation.CLEAN_INSERT)
        );
    }

    @Test
    public void createGlossary() throws Exception {
        new FacesRequest() {

            protected void invokeApplication() throws Exception {
                EntityManager em = (EntityManager) getInstance("entityManager");
                HGlossary glossary = em.find(HGlossary.class, 1L);
                HConcept concept = em.find(HConcept.class, 1L);
                HTermEntry termEntry = em.find(HTermEntry.class, 1L);
                
                assertThat(glossary, notNullValue());
                assertThat(concept, notNullValue());
                assertThat(termEntry, notNullValue());
                
                assertThat("directory", is(concept.getTerm()));
            }
        }.run();
    }
    
}