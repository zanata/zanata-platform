package org.zanata.rest.service;

import org.dbunit.operation.DatabaseOperation;
import org.junit.Before;
import org.junit.Test;
import org.zanata.ZanataDbunitJpaTest;
import org.zanata.rest.dto.User;
import org.zanata.rest.editor.service.UserService;
import org.zanata.rest.service.ProjectVersionService;
import org.zanata.seam.SeamAutowire;
import org.zanata.service.impl.ConfigurationServiceImpl;
import org.zanata.service.impl.GravatarServiceImpl;
import org.zanata.service.impl.IndexingServiceImpl;
import org.zanata.service.impl.LocaleServiceImpl;

import javax.ws.rs.core.Response;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Alex Eng <a href="aeng@redhat.com">aeng@redhat.com</a>
 */
public class ProjectVersionTest extends ZanataDbunitJpaTest {

    private SeamAutowire seam = SeamAutowire.instance();

    private ProjectVersionService service;

    @Override
    protected void prepareDBUnitOperations() {
        beforeTestOperations.add(new DataSetOperation(
            "org/zanata/test/model/ClearAllTables.dbunit.xml",
            DatabaseOperation.DELETE_ALL));
        beforeTestOperations.add(new DataSetOperation(
            "org/zanata/test/model/LocalesData.dbunit.xml",
            DatabaseOperation.CLEAN_INSERT));
        beforeTestOperations.add(new DataSetOperation(
            "org/zanata/test/model/AccountData.dbunit.xml",
            DatabaseOperation.CLEAN_INSERT));
        beforeTestOperations.add(new DataSetOperation(
            "org/zanata/test/model/CopyVersionData.dbunit.xml",
            DatabaseOperation.CLEAN_INSERT));
    }

    @Before
    public void initializeSeam() {
        seam.reset()
            .useImpl(ProjectVersionService.class)
            .use("session", getSession())
            .useImpl(LocaleServiceImpl.class)
            .useImpl(GravatarServiceImpl.class)
            .useImpl(ConfigurationServiceImpl.class)
            .useImpl(UserService.class)
            .ignoreNonResolvable();
        service = seam.autowire(ProjectVersionService.class);
    }

    @Test
    public void getContributors() {
        String projectSlug = "sample-project";
        String versionSlug = "1.0";
        String dateRange = "2010-01-01..2010-12-01";
        Response response = service.getContributors(projectSlug, versionSlug,
                dateRange);
        List<User> userList = (List<User>) response.getEntity();
        assertThat(userList).extracting("username").contains("admin", "bob",
                "demo");
    }

    @Test
    public void getContributors2() {
        String projectSlug = "sample-project";
        String versionSlug = "1.0";
        String dateRange = "2015-01-01..2015-12-01";
        Response response = service.getContributors(projectSlug, versionSlug,
            dateRange);
        List<User> userList = (List<User>) response.getEntity();
        assertThat(userList).isEmpty();
    }
}
