package org.fedorahosted.flies.test.model;



import javax.persistence.EntityManager;

import org.dbunit.operation.DatabaseOperation;
import org.fedorahosted.flies.core.model.HPerson;
import org.jboss.seam.mock.DBUnitSeamTest;
import org.testng.annotations.Test;

@Test(groups={"seam-tests"})
public class PersonSeamTest extends DBUnitSeamTest {

    protected void prepareDBUnitOperations() {
        beforeTestOperations.add(
                new DataSetOperation("org/fedorahosted/flies/test/model/FliesBaseData.dbunit.xml", DatabaseOperation.CLEAN_INSERT)
        );
    }

    @Test
    public void findAllCommentsFlat() throws Exception {
        new FacesRequest() {

            protected void invokeApplication() throws Exception {
                EntityManager em = (EntityManager) getInstance("entityManager");
                HPerson p = (HPerson) em.createQuery("select p from Person p where p.id = :id")
                                .setParameter("id", 1l)
                                .getSingleResult();
                assert p.getName().equals("Mr Bean");
            }
        }.run();
    }
}