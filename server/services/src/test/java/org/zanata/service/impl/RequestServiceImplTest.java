package org.zanata.service.impl;

import org.dbunit.operation.DatabaseOperation;
import org.hibernate.Session;
import org.jglue.cdiunit.InRequestScope;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.zanata.ApplicationConfiguration;
import org.zanata.ZanataDbunitJpaTest;
import org.zanata.dao.AccountDAO;
import org.zanata.dao.LocaleDAO;
import org.zanata.dao.RequestDAO;
import org.zanata.events.RequestUpdatedEvent;
import org.zanata.i18n.Messages;
import org.zanata.model.HAccount;
import org.zanata.model.HLocale;
import org.zanata.model.LanguageRequest;
import org.zanata.model.Request;
import org.zanata.model.type.RequestState;
import org.zanata.model.type.RequestType;
import org.zanata.service.EmailService;
import org.zanata.test.CdiUnitRunner;
import org.zanata.test.EventListener;

import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Alex Eng <a href="aeng@redhat.com">aeng@redhat.com</a>
 */
@RunWith(CdiUnitRunner.class)
public class RequestServiceImplTest extends ZanataDbunitJpaTest {

    @Inject
    private RequestServiceImpl service;

    @Inject
    private RequestDAO requestDAO;

    @Inject
    private LocaleDAO localeDAO;

    @Inject
    private AccountDAO accountDAO;

    @Inject
    private EventListener eventListener;

    @Produces @Mock ApplicationConfiguration applicationConfiguration;
    @Produces @Mock EmailService emailService;
    @Produces @Mock Messages messages;

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

    @Test
    @InRequestScope
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
    @InRequestScope
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

        List<RequestUpdatedEvent> firedEvents =
                eventListener.getFiredEvents(RequestUpdatedEvent.class);
        assertThat(firedEvents.size()).isEqualTo(1);

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
