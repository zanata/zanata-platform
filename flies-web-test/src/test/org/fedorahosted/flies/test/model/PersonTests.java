package org.fedorahosted.flies.test.model;



import org.dbunit.operation.DatabaseOperation;
import org.fedorahosted.flies.core.model.Person;
import org.jboss.seam.mock.DBUnitSeamTest;
import org.testng.annotations.Test;

import javax.persistence.EntityManager;
import java.util.List;
import java.util.Date;

public class PersonTests extends DBUnitSeamTest {

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
                Person p = new Person();
                p.setName("Mr Bean");
                p.setEmail("asgeirf@gmail.com");
                em.persist(p);

                p = (Person) em.createQuery("select p from Person p where p.id = :id")
                                .setParameter("id", 1l)
                                .getSingleResult();
                assert p == null;
                //assert p.getName().equals("Mr Bean");
            }
        }.run();
    }
}