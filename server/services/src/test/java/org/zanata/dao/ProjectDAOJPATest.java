package org.zanata.dao;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.List;

import javax.persistence.EntityManager;

import org.apache.lucene.queryparser.classic.ParseException;
import org.hibernate.search.jpa.Search;
import org.jglue.cdiunit.InRequestScope;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;
import org.zanata.ZanataJpaTest;
import org.zanata.model.HProject;
import org.zanata.security.ZanataIdentity;


public class ProjectDAOJPATest extends ZanataJpaTest {

    private ProjectDAO dao;

    private ZanataIdentity identity;

    @BeforeClass
    public static void disableSecurity() {
        ZanataIdentity.setSecurityEnabled(false);
    }

    @Before
    public void setUp() {
        identity = Mockito.mock(ZanataIdentity.class);
        dao = new ProjectDAO(Search.getFullTextEntityManager(getEm()),
                getSession(), identity);
    }

    private static void makeProject(String slug, String name, String description,
            EntityManager em) {
        HProject hProject = new HProject();
        hProject.setSlug(slug);
        hProject.setName(name);
        hProject.setDescription(description);
        em.persist(hProject);
    }

    @After
    public void cleanUp() {
        deleteAllTables();
    }

    @Test
    @InRequestScope
    public void canDoFullTextSearch() throws Exception {
        when(identity.hasPermission(any(), any())).thenReturn(true);
        String slug = "Sample-Project";
        // hibernate search only works with transaction in place
        doInTransaction(
                em -> {
                    makeProject(slug, "Sample Project", "An example project",
                            em);
                    makeProject("another-project", "another Project",
                            "Another project", em);
                });

        HProject expected = dao.getBySlug(slug);

        assertThat(searchProjects("blah")).isEmpty();
        assertThat(searchProjects("sam")).containsExactly(expected);
        assertThat(searchProjects("SaM")).containsExactly(expected);
        assertThat(searchProjects("sampl ")).containsExactly(expected);
        assertThat(searchProjects("sample-")).containsExactly(expected);
        assertThat(searchProjects("proj"))
                .as("wildcard search is only applied to full slug")
                .isEmpty();
        assertThat(searchProjects("example project")).containsExactly(expected)
                .as("search by description");
    }

    private List<HProject> searchProjects(String sam) throws ParseException {
        return dao.searchProjects(sam, 10, 0, false);
    }
}
