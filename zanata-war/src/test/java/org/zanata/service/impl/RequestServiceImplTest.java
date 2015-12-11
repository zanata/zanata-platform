package org.zanata.service.impl;

import org.dbunit.operation.DatabaseOperation;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.zanata.ZanataDbunitJpaTest;
import org.zanata.dao.AccountDAO;
import org.zanata.dao.LanguageRequestDAO;
import org.zanata.dao.LocaleDAO;
import org.zanata.dao.RequestDAO;
import org.zanata.events.RequestUpdatedEvent;
import org.zanata.model.HAccount;
import org.zanata.model.HLocale;
import org.zanata.model.LanguageRequest;
import org.zanata.model.Request;
import org.zanata.model.type.RequestState;
import org.zanata.model.type.RequestType;
import org.zanata.seam.SeamAutowire;

import javax.enterprise.event.Event;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.verify;

/**
 * @author Alex Eng <a href="aeng@redhat.com">aeng@redhat.com</a>
 */
public class RequestServiceImplTest extends ZanataDbunitJpaTest {
    private SeamAutowire seam = SeamAutowire.instance();

    private RequestServiceImpl service;

    private RequestDAO requestDAO;

    private LanguageRequestDAO languageRequestDAO;

    private AccountDAO accountDAO;

    private LocaleDAO localeDAO;

    @Mock
    private Event<RequestUpdatedEvent> requestUpdatedEvent;

    @Override
    protected void prepareDBUnitOperations() {
        beforeTestOperations.add(new DataSetOperation(
            "org/zanata/test/model/ClearAllTables.dbunit.xml",
            DatabaseOperation.CLEAN_INSERT));
        beforeTestOperations.add(new DataSetOperation(
            "org/zanata/test/model/AccountData.dbunit.xml",
            DatabaseOperation.CLEAN_INSERT));
        beforeTestOperations.add(new DataSetOperation(
            "org/zanata/test/model/LocalesData.dbunit.xml",
            DatabaseOperation.CLEAN_INSERT));
    }

    @Before
    public void beforeMethod() throws Exception {
        MockitoAnnotations.initMocks(this);
        seam.reset();
        requestDAO = new RequestDAO(getSession());
        languageRequestDAO = new LanguageRequestDAO(getSession());
        accountDAO = new AccountDAO(getSession());
        localeDAO = new LocaleDAO(getSession());

        service = seam
            .use("requestDAO", requestDAO)
            .use("languageRequestDAO", languageRequestDAO)
            .use("requestUpdatedEvent", requestUpdatedEvent)
            .use("entityManager", getEm())
            .use("session", getSession())
            .ignoreNonResolvable()
            .autowire(RequestServiceImpl.class);
    }

    @Test
    public void testCreateRequest() {
        //admin user
        HAccount requester = accountDAO.findById(1L);
        //as locale
        HLocale locale = localeDAO.findById(1L);

        boolean reqAsCoordinator = true;
        boolean reqAsReviewer = false;
        boolean reqAsTranslator = false;

        service.createLanguageRequest(requester, locale, reqAsCoordinator,
            reqAsReviewer, reqAsTranslator);

        List<LanguageRequest> result =
            service.getPendingLanguageRequests(locale.getLocaleId());

        assertThat(result).isNotEmpty().hasSize(1);

        LanguageRequest request = result.get(0);
        assertThat(request.isCoordinator()).isEqualTo(reqAsCoordinator);
        assertThat(request.isReviewer()).isEqualTo(reqAsReviewer);
        assertThat(request.isTranslator()).isEqualTo(reqAsTranslator);
        assertThat(request.getLocale().getLocaleId())
            .isEqualTo(locale.getLocaleId());
        assertThat(request.getRequest().getRequester()).isEqualTo(requester);
        assertThat(request.getRequest().getState()).isEqualTo(RequestState.NEW);

        assertThat(service.doesLanguageRequestExist(requester, locale)).isTrue();
    }

    /**
     * Use result from testCreateRequest
     */
    @Test
    public void testUpdateLanguageRequest() {
        //admin user
        HAccount requester = accountDAO.findById(1L);
        //bn locale
        HLocale locale = localeDAO.findById(2L);

        boolean reqAsCoordinator = true;
        boolean reqAsReviewer = false;
        boolean reqAsTranslator = false;

        LanguageRequest languageRequest = service.createLanguageRequest(requester,
            locale, reqAsCoordinator,
            reqAsReviewer, reqAsTranslator);

        String comment = "Accepted by admin";
        String entityId = languageRequest.getRequest().getEntityId();

        service.updateLanguageRequest(languageRequest.getId(), requester,
            RequestState.ACCEPTED, comment);

        verify(requestUpdatedEvent).fire(isA(RequestUpdatedEvent.class));

        assertThat(
            service.getPendingLanguageRequests(requester, locale.getLocaleId()))
            .isNull();

        List<Request> requests = service.getRequestHistoryByEntityId(entityId);
        assertThat(requests).hasSize(2);

        for(Request request: requests) {
            assertThat(request.getRequester()).isEqualTo(requester);
            assertThat(request.getRequestType()).isEqualTo(RequestType.LOCALE);
        }

        //can be equal or before
        assertThat(
                requests.get(0).getValidFrom()
                        .after(requests.get(1).getValidFrom())).isFalse();

        assertThat(requests).extracting("state")
            .contains(RequestState.NEW, RequestState.ACCEPTED);
    }
}
