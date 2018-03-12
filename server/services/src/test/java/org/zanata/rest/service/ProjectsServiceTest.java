package org.zanata.rest.service;

import org.dbunit.operation.DatabaseOperation;
import org.hibernate.Session;
import org.hibernate.search.jpa.FullTextEntityManager;
import org.jglue.cdiunit.AdditionalClasses;
import org.jglue.cdiunit.InRequestScope;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.zanata.ZanataDbunitJpaTest;
import org.zanata.jpa.FullText;
import org.zanata.model.HAccount;
import org.zanata.rest.dto.Project;
import org.zanata.seam.security.CurrentUserImpl;
import org.zanata.security.ZanataIdentity;
import org.zanata.security.annotations.Authenticated;
import org.zanata.test.CdiUnitRunner;

import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
@RunWith(CdiUnitRunner.class)
@AdditionalClasses({ ProjectsService.class, CurrentUserImpl.class})
public class ProjectsServiceTest extends ZanataDbunitJpaTest {
    @Inject
    private ProjectsService projectsService;

    @Produces @Mock
    @FullText
    FullTextEntityManager fullTextEntityManager;

    @Override
    @Produces
    protected EntityManager getEm() {
        return super.getEm();
    }

    @Override
    @Produces
    protected Session getSession() {
        return super.getSession();
    }

    @Produces @Authenticated
    @Mock
    protected HAccount authenticatedAccount;

    @Produces
    @Mock
    private ZanataIdentity identity;

    @Override
    protected void prepareDBUnitOperations() {
        beforeTestOperations.add(new DataSetOperation(
                "org/zanata/test/model/ClearAllTables.dbunit.xml",
                DatabaseOperation.CLEAN_INSERT));
        beforeTestOperations.add(new DataSetOperation(
                "org/zanata/test/model/ProjectsData.dbunit.xml",
                DatabaseOperation.CLEAN_INSERT));
        afterTestOperations.add(new DataSetOperation(
                "org/zanata/test/model/ProjectsData.dbunit.xml",
                DatabaseOperation.DELETE_ALL));
    }

    @Test
    @InRequestScope
    public void testGet() {
        projectsService.setMediaType(MediaType.APPLICATION_XML_TYPE);
        Response response = projectsService.get();
        assertThat(response.getStatus())
                .isEqualTo(Response.Status.OK.getStatusCode());
        @SuppressWarnings("unchecked")
        List<Project> projects = (List<Project>) response.getEntity();
        assertThat(projects).hasSize(3);
    }
}
