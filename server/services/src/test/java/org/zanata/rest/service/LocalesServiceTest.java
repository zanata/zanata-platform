package org.zanata.rest.service;

import org.dbunit.operation.DatabaseOperation;
import org.hibernate.Session;
import org.hibernate.search.jpa.FullTextEntityManager;
import org.jglue.cdiunit.AdditionalClasses;
import org.jglue.cdiunit.InRequestScope;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.zanata.ZanataDbunitJpaTest;
import org.zanata.cdi.StaticProducer;
import org.zanata.common.LocaleId;
import org.zanata.dao.LanguageRequestDAO;
import org.zanata.jpa.FullText;
import org.zanata.model.HAccount;
import org.zanata.model.HLocale;
import org.zanata.model.LanguageRequest;
import org.zanata.model.Request;
import org.zanata.model.type.RequestType;
import org.zanata.rest.dto.LocaleMember;
import org.zanata.rest.dto.LocalesResults;
import org.zanata.seam.security.CurrentUserImpl;
import org.zanata.security.ZanataIdentity;
import org.zanata.security.annotations.Authenticated;
import org.zanata.service.RequestService;
import org.zanata.service.impl.LocaleServiceImpl;
import org.zanata.servlet.annotations.SessionId;
import org.zanata.test.CdiUnitRunner;
import org.zanata.util.UrlUtil;

import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.ws.rs.core.Response;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
@RunWith(CdiUnitRunner.class)
@AdditionalClasses({ LocaleServiceImpl.class, CurrentUserImpl.class })
public class LocalesServiceTest extends ZanataDbunitJpaTest implements
        StaticProducer {

    private Response response;

    @Produces @Authenticated
    @Mock HAccount authenticatedAccount;

    @Inject
    private LocalesService localesService;

    @Produces @Mock(answer = Answers.RETURNS_DEEP_STUBS)
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

    @Produces @Mock
    private RequestService requestService;

    @Produces @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private LanguageRequestDAO languageRequestDAO;

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

    @Test
    @InRequestScope
    public void testGetRequests() {
        List<LanguageRequest> languageRequests = new ArrayList<>(Collections.singleton(
            new LanguageRequest(
                new Request(RequestType.LOCALE, new HAccount(), "", new Date()),
                new HLocale(new LocaleId("as")), false, false, true)));
        when(identity.hasRole("admin")).thenReturn(true);
        when(requestService.getPendingLanguageRequests(any())).thenReturn(languageRequests);

        response = localesService.get("as", null, 1, 1);
        assertThat(response.getStatus())
                .isEqualTo(Response.Status.OK.getStatusCode());
        LocalesResults results = (LocalesResults) response.getEntity();
        assertThat(results.getResults()).hasSize(1);
        assertThat(results.getResults().get(0).getRequestCount()).isEqualTo(1);
    }
}
