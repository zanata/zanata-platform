/*
 * Copyright 2015, Red Hat, Inc. and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.zanata.service.impl;

import com.google.common.collect.Lists;
import org.apache.commons.lang.StringUtils;
import org.apache.deltaspike.jpa.api.transaction.Transactional;
import org.zanata.ApplicationConfiguration;
import org.zanata.common.LocaleId;
import org.zanata.dao.LanguageRequestDAO;
import org.zanata.dao.RequestDAO;
import org.zanata.email.DeclineLanguageRequestEmailStrategy;
import org.zanata.email.EmailStrategy;
import org.zanata.events.RequestUpdatedEvent;
import org.zanata.exception.RequestExistsException;
import org.zanata.i18n.Messages;
import org.zanata.model.HAccount;
import org.zanata.model.HLocale;
import org.zanata.model.LanguageRequest;
import org.zanata.model.Request;
import org.zanata.model.type.RequestState;
import org.zanata.model.type.RequestType;
import org.zanata.service.EmailService;
import org.zanata.service.RequestService;
import javax.enterprise.context.RequestScoped;
import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.EntityNotFoundException;
import java.util.Date;
import java.util.List;

/**
 * @author Alex Eng <a href="aeng@redhat.com">aeng@redhat.com</a>
 */
@Named("requestServiceImpl")
@RequestScoped
@Transactional
public class RequestServiceImpl implements RequestService {
    private static final org.slf4j.Logger log =
            org.slf4j.LoggerFactory.getLogger(RequestServiceImpl.class);

    @Inject
    private RequestDAO requestDAO;
    @Inject
    private LanguageRequestDAO languageRequestDAO;
    @Inject
    private Event<RequestUpdatedEvent> requestUpdatedEvent;
    @Inject
    private ApplicationConfiguration applicationConfiguration;
    @Inject
    private EmailService emailServiceImpl;
    @Inject
    private Messages msgs;

    @Override
    public LanguageRequest createLanguageRequest(HAccount requester,
            HLocale locale, boolean isRequestAsCoordinator,
            boolean isRequestAsReviewer, boolean isRequestAsTranslator)
            throws RequestExistsException {
        // search if there's any existing language request
        // of the same requester, account and locale
        LanguageRequest languageRequest = languageRequestDAO
                .findRequesterPendingRequests(requester, locale.getLocaleId());
        if (languageRequest != null) {
            throw new RequestExistsException(
                    RequestType.LOCALE + ", " + requester.getUsername() + ", , "
                            + locale.getDisplayName());
        }
        String entityId = requestDAO.generateEntityId();
        Date now = new Date();
        Request request =
                new Request(RequestType.LOCALE, requester, entityId, now);
        LanguageRequestBuilder requestBuilder = new LanguageRequestBuilder()
                .setRequest(request).setLocale(locale)
                .setCoordinator(isRequestAsCoordinator)
                .setReviewer(isRequestAsReviewer)
                .setTranslator(isRequestAsTranslator);
        languageRequest = requestBuilder.build();
        requestDAO.makePersistent(request);
        languageRequestDAO.makePersistent(languageRequest);
        return languageRequest;
    }

    @Override
    public boolean doesLanguageRequestExist(HAccount requester,
            HLocale locale) {
        LanguageRequest languageRequest = languageRequestDAO
                .findRequesterPendingRequests(requester, locale.getLocaleId());
        return languageRequest != null;
    }

    @Override
    public void updateLanguageRequest(Long requestId, HAccount actor,
            RequestState state, String comment) throws EntityNotFoundException {
        LanguageRequest languageRequest =
                languageRequestDAO.findById(requestId);
        if (languageRequest != null) {
            Request oldRequest = languageRequest.getRequest();
            Request newRequest =
                    oldRequest.update(actor, state, comment, new Date());
            requestDAO.makePersistent(oldRequest);
            languageRequest.setRequest(newRequest);
            requestDAO.makePersistent(oldRequest);
            requestDAO.makePersistent(newRequest);
            languageRequestDAO.makePersistent(languageRequest);
            requestUpdatedEvent.fire(new RequestUpdatedEvent(newRequest.getId(),
                    languageRequest.getId(), actor.getId(), state));
        } else {
            throw new EntityNotFoundException();
        }
    }

    public void onRequestUpdated(@Observes RequestUpdatedEvent event) {
        Request request = requestDAO.findById(event.getId());
        if (request.getRequestType().equals(RequestType.LOCALE)) {
            processOnLanguageRequestUpdated(event);
        }
    }

    /**
     * send out decline email if state equals reject
     */
    private void processOnLanguageRequestUpdated(RequestUpdatedEvent event) {
        if (event.getState().equals(RequestState.REJECTED)) {
            LanguageRequest languageRequest =
                    languageRequestDAO.findById(event.getRequestId());
            String contactCoordinatorLink =
                    applicationConfiguration.getServerPath() + "/language/view/"
                            + languageRequest.getLocale().getLocaleId();
            HAccount requester = languageRequest.getRequest().getRequester();
            String message = languageRequest.getRequest().getComment();
            EmailStrategy strategy = new DeclineLanguageRequestEmailStrategy(
                    requester.getUsername(), getRequestRoles(languageRequest),
                    contactCoordinatorLink,
                    languageRequest.getLocale().retrieveDisplayName(),
                    languageRequest.getRequest().getComment());
            try {
                emailServiceImpl.sendToLanguageRequester(strategy,
                        requester.getPerson());
            } catch (Exception e) {
                String subject = strategy.getSubject(msgs);
                StringBuilder sb = new StringBuilder()
                        .append("Failed to send email with subject \'")
                        .append(strategy.getSubject(msgs))
                        .append("\' , message \'").append(message).append("\'");
                log.error(
                        "Failed to send email: toName \'{}\', subject \'{}\', message \'{}\'. {}",
                        requester.getUsername(), subject, message, e);
            }
        }
    }

    private String getRequestRoles(LanguageRequest languageRequest) {
        StringBuilder sb = new StringBuilder();
        if (languageRequest.isCoordinator()) {
            sb.append(StringUtils.lowerCase(msgs.get("jsf.Coordinator")));
        }
        if (languageRequest.isReviewer()) {
            sb.append(StringUtils.lowerCase(msgs.get("jsf.Reviewer")));
        }
        if (languageRequest.isTranslator()) {
            sb.append(StringUtils.lowerCase(msgs.get("jsf.Translator")));
        }
        return sb.toString();
    }

    @Override
    public LanguageRequest getLanguageRequest(long languageRequestId) {
        return languageRequestDAO.findById(languageRequestId);
    }

    @Override
    public LanguageRequest getPendingLanguageRequests(HAccount account,
            LocaleId localeId) {
        return languageRequestDAO.findRequesterPendingRequests(account,
                localeId);
    }

    @Override
    public List<LanguageRequest>
            getPendingLanguageRequests(LocaleId... localeIds) {
        return languageRequestDAO
                .findPendingRequests(Lists.newArrayList(localeIds));
    }

    @Override
    public Request getPendingRequestByEntityId(String entityId) {
        return requestDAO.getEntityById(entityId);
    }

    @Override
    public List<Request> getRequestHistoryByEntityId(String entityId) {
        return requestDAO.getHistoryByEntityId(entityId);
    }

    /**
     * Return true if request have been processed (validTo is not null)
     *
     * @param request
     */
    private boolean isRequestProcessed(Request request) {
        return request.getValidTo() != null;
    }

    /**
     * Builder for languageRequest TODO: use @Builder in LanguageRequest but
     * issue with @NoArgsConstructor and @AllArgsConstructor
     */
    public class LanguageRequestBuilder {

        private Request request;
        private HLocale locale;
        private boolean coordinator;
        private boolean reviewer;
        private boolean translator;

        public LanguageRequestBuilder setRequest(Request request) {
            this.request = request;
            return this;
        }

        public LanguageRequestBuilder setLocale(HLocale locale) {
            this.locale = locale;
            return this;
        }

        public LanguageRequestBuilder setCoordinator(boolean coordinator) {
            this.coordinator = coordinator;
            return this;
        }

        public LanguageRequestBuilder setReviewer(boolean reviewer) {
            this.reviewer = reviewer;
            return this;
        }

        public LanguageRequestBuilder setTranslator(boolean translator) {
            this.translator = translator;
            return this;
        }

        public LanguageRequest build() {
            return new LanguageRequest(request, locale, coordinator, reviewer,
                    translator);
        }
    }
}
