package org.zanata.rest.service;

import org.apache.deltaspike.core.spi.scope.window.WindowContext;
import org.dbunit.operation.DatabaseOperation;
import org.hibernate.Session;
import org.hibernate.search.jpa.FullTextEntityManager;
import org.jglue.cdiunit.InRequestScope;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.zanata.ApplicationConfiguration;
import org.zanata.ZanataDbunitJpaTest;
import org.zanata.i18n.Messages;
import org.zanata.jpa.FullText;
import org.zanata.model.HAccount;
import org.zanata.rest.dto.User;
import org.zanata.rest.editor.service.resource.UserResource;
import org.zanata.seam.security.IdentityManager;
import org.zanata.security.annotations.Authenticated;
import org.zanata.service.ConfigurationService;
import org.zanata.service.GravatarService;
import org.zanata.service.LocaleService;
import org.zanata.servlet.annotations.ContextPath;
import org.zanata.servlet.annotations.ServerPath;
import org.zanata.servlet.annotations.SessionId;
import org.zanata.test.CdiUnitRunner;
import org.zanata.util.DefaultLocale;
import org.zanata.util.UrlUtil;

import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.ws.rs.core.Response;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Alex Eng <a href="aeng@redhat.com">aeng@redhat.com</a>
 */
@RunWith(CdiUnitRunner.class)
public class ProjectVersionTest extends ZanataDbunitJpaTest {

    @Inject
    private ProjectVersionService service;

    // mocked dependencies
    @Produces @SessionId String sessionId = "";
    @Produces @ContextPath String contextPath = "";
    @Produces @ServerPath String serverPath = "";
    @Produces @Mock LocaleService localeService;
    @Produces @Mock GravatarService gravatarService;
    @Produces @Mock ConfigurationService configurationService;
    @Produces @Mock UserResource userResource;
    @Produces @Authenticated @Mock HAccount authenticatedAccount;
    @Produces @FullText @Mock FullTextEntityManager fullTextEntityManager;
    @Produces @DefaultLocale @Mock Messages messages;
    @Produces @Mock ApplicationConfiguration applicationConfiguration;
    @Produces @Mock WindowContext windowContext;
    @Produces @Mock UrlUtil urlUtil;
    @Produces @Mock IdentityManager identityManager;

    @Override
    @Produces
    protected Session getSession() {
        super.setupEM();
        return super.getSession();
    }

    @Override
    @Produces
    protected EntityManager getEm() {
        return super.getEm();
    }

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

    @Test
    @InRequestScope
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
    @InRequestScope
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
