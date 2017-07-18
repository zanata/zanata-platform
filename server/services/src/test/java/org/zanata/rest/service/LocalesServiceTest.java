package org.zanata.rest.service;

import org.dbunit.operation.DatabaseOperation;
import org.hibernate.Session;
import org.hibernate.search.jpa.FullTextEntityManager;
import org.jglue.cdiunit.AdditionalClasses;
import org.jglue.cdiunit.InRequestScope;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.zanata.ZanataDbunitJpaTest;
import org.zanata.cdi.StaticProducer;
import org.zanata.common.LocaleId;
import org.zanata.jpa.FullText;
import org.zanata.rest.dto.LocaleMember;
import org.zanata.security.ZanataIdentity;
import org.zanata.service.impl.LocaleServiceImpl;
import org.zanata.servlet.annotations.SessionId;
import org.zanata.test.CdiUnitRunner;
import org.zanata.util.UrlUtil;

import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.ws.rs.core.Response;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
@RunWith(CdiUnitRunner.class)
@AdditionalClasses({ LocaleServiceImpl.class })
public class LocalesServiceTest extends ZanataDbunitJpaTest implements
        StaticProducer {

    private Response response;

    @Inject
    private LocalesService localesService;

    @Inject
    private ZanataIdentity identity;

    @Produces @SessionId String sessionId = "";

    @Produces @Mock
    private UrlUtil urlUtil;

    @Produces
    public Session getSession() {
        return super.getSession();
    }

    @Produces @Mock
    private EntityManager entityManager;

    @Produces @Mock @FullText FullTextEntityManager fullTextEntityManager;

    /**
     * Implement this in a subclass.
     * <p/>
     * Use it to stack DBUnit <tt>DataSetOperation</tt>'s with the
     * <tt>beforeTestOperations</tt> and <tt>afterTestOperations</tt> lists.
     */
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
    }

    @After
    public void afterMethod() {
        response = null;
    }

    @Test
    @InRequestScope
    public void testGetLocales() {
        response = localesService.get("test", null, 1, 1);
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
    }

    @Test
    @InRequestScope
    public void testGetMembersEmptyLocale() {
        response = localesService.getMembers("");
        assertThat(response.getStatus())
                .isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    @InRequestScope
    public void testGetMembersNotSupportedLocale() {
        response = localesService.getMembers("invalidLocale");
        assertThat(response.getStatus())
                .isEqualTo(Response.Status.NOT_FOUND.getStatusCode());
    }

    @Test
    @InRequestScope
    public void testGetMembers() {
        response = localesService.getMembers(LocaleId.FR.getId());
        assertThat(response.getStatus())
                .isEqualTo(Response.Status.OK.getStatusCode());
        @SuppressWarnings("unchecked")
        List<LocaleMember> results = (List<LocaleMember>) response.getEntity();
        assertThat(results).hasSize(1);
    }
}
