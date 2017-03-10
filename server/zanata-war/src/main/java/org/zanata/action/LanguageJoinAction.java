/*
 * Copyright 2013, Red Hat, Inc. and individual contributors as indicated by the
 * @author tags. See the copyright.txt file in the distribution for a full
 * listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this software; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
 * site: http://www.fsf.org.
 */
package org.zanata.action;

import java.io.Serializable;
import java.util.List;
import javax.enterprise.inject.Model;
import javax.faces.bean.ViewScoped;
import javax.validation.constraints.Size;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.deltaspike.jpa.api.transaction.Transactional;
import org.zanata.ApplicationConfiguration;
import org.zanata.security.annotations.Authenticated;
import org.apache.commons.lang.StringUtils;
import org.zanata.exception.RequestExistsException;
import org.zanata.model.LanguageRequest;
import org.zanata.model.type.RequestState;
import org.zanata.security.ZanataIdentity;
import org.zanata.common.LocaleId;
import org.zanata.dao.LocaleMemberDAO;
import org.zanata.email.EmailStrategy;
import org.zanata.email.RequestToJoinLanguageEmailStrategy;
import org.zanata.i18n.Messages;
import org.zanata.model.HAccount;
import org.zanata.model.HLocale;
import org.zanata.model.HLocaleMember;
import org.zanata.service.EmailService;
import org.zanata.service.LanguageTeamService;
import org.zanata.service.LocaleService;
import org.zanata.service.RequestService;
import org.zanata.ui.faces.FacesMessages;

import static org.apache.commons.lang3.ObjectUtils.firstNonNull;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
@Named("languageJoinAction")
@ViewScoped
@Model
@Transactional
public class LanguageJoinAction implements Serializable {
    private static final org.slf4j.Logger log =
            org.slf4j.LoggerFactory.getLogger(LanguageJoinAction.class);

    private static final long serialVersionUID = 1L;
    @Inject
    private LocaleMemberDAO localeMemberDAO;
    @Inject
    private LocaleService localeServiceImpl;
    @Inject
    private EmailService emailServiceImpl;
    @Inject
    private FacesMessages facesMessages;
    @Inject
    private Messages msgs;
    private String language;
    private HLocale locale;
    @Size(max = 1000)
    private String message;
    @Size(max = 255)
    private String declineMessage;
    @Inject
    private RequestService requestServiceImpl;
    @Inject
    private ZanataIdentity identity;
    @Inject
    private LanguageTeamService languageTeamServiceImpl;
    @Inject
    @Authenticated
    private HAccount authenticatedAccount;
    @Inject
    private ApplicationConfiguration applicationConfiguration;

    /**
     * Return localised roles requested
     */
    public String getLocalisedRequestedRoles(LanguageRequest request) {
        return Joiner.on(", ").skipNulls().join(
                request.isCoordinator() ? msgs.get("jsf.Coordinator") : null,
                request.isReviewer() ? msgs.get("jsf.Reviewer") : null,
                request.isTranslator() ? msgs.get("jsf.Translator") : null);
    }

    public void acceptRequest(Long languageRequestId) {
        identity.checkPermission(getLocale(), "manage-language-team");
        LanguageRequest request =
                requestServiceImpl.getLanguageRequest(languageRequestId);
        Long personId = request.getRequest().getRequester().getPerson().getId();
        HLocaleMember member = firstNonNull(localeMemberDAO.findByPersonAndLocale(personId,
                new LocaleId(language)), new HLocaleMember());
        boolean updateAsTranslator = member.isTranslator() || request.isTranslator();
        boolean updateAsReviewer = member.isReviewer() || request.isReviewer();
        boolean updateAsCoordinator = member.isCoordinator() || request.isCoordinator();

        languageTeamServiceImpl.joinOrUpdateRoleInLanguageTeam(language,
                personId, updateAsTranslator, updateAsReviewer,
                updateAsCoordinator);
        requestServiceImpl.updateLanguageRequest(languageRequestId,
                authenticatedAccount, RequestState.ACCEPTED, "");
        facesMessages.addGlobal(msgs.get("jsf.language.request.updated"));
    }

    public void declineRequest(Long languageRequestId) {
        identity.checkPermission(locale, "manage-language-team");
        requestServiceImpl.updateLanguageRequest(languageRequestId,
                authenticatedAccount, RequestState.REJECTED, declineMessage);
        facesMessages.addGlobal(msgs.get("jsf.language.request.updated"));
    }

    public void clearMessage() {
        message = "";
    }

    public void processRequest(boolean translator, boolean reviewer,
            boolean coordinator) {
        try {
            // Grant translator only request automatically if enabled
            if (applicationConfiguration.isAutoAcceptRequests() &&
                    (translator && !(coordinator || reviewer))) {
                languageTeamServiceImpl.joinOrUpdateRoleInLanguageTeam(language,
                        authenticatedAccount.getId(), translator, reviewer,
                        coordinator);
                log.info("User {} was added to the language team {}",
                        authenticatedAccount.getUsername(), language);
            } else {
                requestServiceImpl.createLanguageRequest(authenticatedAccount,
                        getLocale(), coordinator, reviewer, translator);
                sendRequestEmail(coordinator, reviewer, translator);
            }
        } catch (RequestExistsException e) {
            String message = msgs.format("jsf.language.request.exists",
                    authenticatedAccount.getUsername(),
                    getLocale().retrieveDisplayName());
            facesMessages.addGlobal(message);
        } finally {
            clearMessage();
        }
    }

    private void sendRequestEmail(boolean requestAsCoordinator,
            boolean requestAsReviewer, boolean requestAsTranslator) {
        String fromName = authenticatedAccount.getPerson().getName();
        String fromLoginName = authenticatedAccount.getUsername();
        String replyEmail = authenticatedAccount.getPerson().getEmail();
        EmailStrategy strategy = new RequestToJoinLanguageEmailStrategy(
                fromLoginName, fromName, replyEmail,
                locale.getLocaleId().getId(), locale.retrieveNativeName(),
                message, requestAsTranslator, requestAsReviewer,
                requestAsCoordinator);
        try {
            facesMessages.addGlobal(emailServiceImpl.sendToLanguageCoordinators(
                    locale.getLocaleId(), strategy));
        } catch (Exception e) {
            String subject = strategy.getSubject(msgs);
            StringBuilder sb = new StringBuilder()
                    .append("Failed to send email with subject \'")
                    .append(strategy.getSubject(msgs)).append("\' , message \'")
                    .append(message).append("\'");
            log.error(
                    "Failed to send email: fromName \'{}\', fromLoginName \'{}\', replyEmail \'{}\', subject \'{}\', message \'{}\'. {}",
                    fromName, fromLoginName, replyEmail, subject, message, e);
            facesMessages.addGlobal(sb.toString());
        }
    }

    public boolean isUserAlreadyRequest() {
        return requestServiceImpl.doesLanguageRequestExist(authenticatedAccount,
                getLocale());
    }

    public void cancelRequest() {
        LanguageRequest languageRequest =
                requestServiceImpl.getPendingLanguageRequests(
                        authenticatedAccount, getLocale().getLocaleId());
        if (languageRequest == null) {
            facesMessages.addGlobal(msgs.get("jsf.language.request.processed"));
            return;
        }
        String comment = "Request cancelled by requester {"
                + authenticatedAccount.getUsername() + "}";
        requestServiceImpl.updateLanguageRequest(languageRequest.getId(),
                authenticatedAccount, RequestState.CANCELLED, comment);
        facesMessages.addGlobal(msgs.format("jsf.language.request.cancelled",
                authenticatedAccount.getUsername()));
    }

    public String getMyLocalisedRoles() {
        if (authenticatedAccount == null) {
            return "";
        }
        HLocaleMember localeMember = getLocaleMember();
        if (localeMember == null) {
            return "";
        }
        if (localeMember.isCoordinator()) {
            return msgs.format("jsf.language.myRoles",
                    StringUtils.lowerCase(msgs.get("jsf.Coordinator")));
        }
        List<String> roles = Lists.newArrayList();
        if (localeMember.isTranslator()) {
            roles.add(StringUtils.lowerCase(msgs.get("jsf.Translator")));
        }
        if (localeMember.isReviewer()) {
            roles.add(StringUtils.lowerCase(msgs.get("jsf.Reviewer")));
        }
        return msgs.format("jsf.language.myRoles",
                Joiner.on(" and ").join(roles));
    }

    public HLocale getLocale() {
        /*
         * Preload the HLocaleMember objects. This line is needed as Hibernate
         * has problems when invoking lazily loaded collections from postLoad
         * entity listener methods. In this case, the drools engine will attempt
         * to access the 'members' collection from inside the security
         * listener's postLoad method to evaluate rules.
         */
        if (locale == null) {
            locale = localeServiceImpl.getByLocaleId(new LocaleId(language));
            locale.getMembers();
        }
        return locale;
    }

    public boolean isTranslator() {
        HLocaleMember member = getLocaleMember();
        return member == null ? false : getLocaleMember().isTranslator();
    }

    public boolean isReviewer() {
        HLocaleMember member = getLocaleMember();
        return member == null ? false : getLocaleMember().isReviewer();
    }

    public boolean isCoordinator() {
        HLocaleMember member = getLocaleMember();
        return member == null ? false : getLocaleMember().isCoordinator();
    }

    private HLocaleMember getLocaleMember() {
        return localeMemberDAO.findByPersonAndLocale(
                authenticatedAccount.getPerson().getId(),
                new LocaleId(language));
    }

    public void setLanguage(final String language) {
        this.language = language;
    }

    public String getLanguage() {
        return this.language;
    }

    public String getMessage() {
        return this.message;
    }

    public void setMessage(final String message) {
        this.message = message;
    }

    public void setDeclineMessage(final String declineMessage) {
        this.declineMessage = declineMessage;
    }

    public String getDeclineMessage() {
        return this.declineMessage;
    }
}
