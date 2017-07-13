package org.zanata.dao;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.function.Function;

import javax.persistence.EntityManager;

import org.apache.lucene.queryparser.classic.ParseException;
import org.hibernate.search.jpa.Search;
import org.jglue.cdiunit.InRequestScope;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.zanata.ZanataJpaTest;
import org.zanata.model.HProject;
import org.zanata.security.ZanataIdentity;


public class ProjectDAOJPATest extends ZanataJpaTest {

    private ProjectDAO dao;

    @BeforeClass
    public static void disableSecurity() {
        ZanataIdentity.setSecurityEnabled(false);
    }

    @Before
    public void setUp() {
        dao = new ProjectDAO(Search.getFullTextEntityManager(getEm()), getSession());
    }

    private static HProject makeProject(String slug, String name, String description,
            EntityManager em) {
        HProject hProject = new HProject();
        hProject.setSlug(slug);
        hProject.setName(name);
        hProject.setDescription(description);
        em.persist(hProject);
        return hProject;
    }

    private void doInTransaction(Function<EntityManager, HProject> function) {
        EntityManager em = getEmf().createEntityManager();
        em.getTransaction().begin();
        function.apply(em);
        em.getTransaction().commit();
    }

    @After
    public void cleanUp() {
        deleteAllTables();
    }

    @Test
    @InRequestScope
    public void canDoFullTextSearch() throws Exception {
        String slug = "Sample-Project";
        // hibernate search only works with transaction in place
        doInTransaction(em -> makeProject(slug, "Sample Project", "An example project", em));

        HProject expected = dao.getBySlug(slug);


        assertThat(searchProjects("sam")).contains(expected);
        assertThat(searchProjects("SaM")).contains(expected);
        assertThat(searchProjects("sampl ")).contains(expected);
        assertThat(searchProjects("sample-")).contains(expected);
        assertThat(searchProjects("proj"))
                .as("wildcard search only for full slug")
                .isEmpty();
        assertThat(searchProjects("an project")).contains(expected)
                .as("search by description");
    }

    private List<HProject> searchProjects(String sam) throws ParseException {
        return dao.searchProjects(sam, 10, 0, false);
    }
}
