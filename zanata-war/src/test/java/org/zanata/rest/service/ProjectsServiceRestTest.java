package org.zanata.rest.service;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.testng.Assert.assertFalse;

import org.dbunit.operation.DatabaseOperation;
import org.jboss.resteasy.client.ClientRequestFactory;
import org.jboss.resteasy.client.ClientResponse;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.zanata.ZanataRestTest;
import org.zanata.common.EntityStatus;
import org.zanata.rest.client.IProjectsResource;
import org.zanata.rest.dto.Project;
import org.zanata.seam.SeamAutowire;

public class ProjectsServiceRestTest extends ZanataRestTest {

    ClientRequestFactory clientRequestFactory;
    IProjectsResource projectService;

    @Override
    protected void prepareResources() {
        SeamAutowire seamAutowire = getSeamAutowire();
        seamAutowire.use("session", getSession());

        ProjectsService projectsService =
                seamAutowire.autowire(ProjectsService.class);

        resources.add(projectsService);
    }

    @BeforeMethod(dependsOnMethods = "prepareRestEasyFramework")
    public void prepareClient() throws Exception {
        projectService =
                getClientRequestFactory().createProxy(IProjectsResource.class);
    }

    @Override
    protected void prepareDBUnitOperations() {
        beforeTestOperations.add(new DataSetOperation(
                "org/zanata/test/model/ProjectsData.dbunit.xml",
                DatabaseOperation.CLEAN_INSERT));
    }

    @Test
    public void retrieveListOfProjects() throws Exception {
        ClientResponse<Project[]> response = projectService.get();

        assertThat(response.getStatus(), is(200));
        assertThat(response.getEntity(), notNullValue());
        // Obsolete projects should not appear
        for (Project p : response.getEntity()) {
            assertFalse(p.getStatus() == EntityStatus.OBSOLETE);
        }

    }
}
