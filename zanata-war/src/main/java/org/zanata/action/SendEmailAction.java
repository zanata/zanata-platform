/*
 * Copyright 2010, Red Hat, Inc. and individual contributors
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
package org.zanata.action;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

import javax.faces.application.FacesMessage;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import org.hibernate.validator.constraints.Email;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.international.LocaleSelector;
import org.jboss.seam.security.NotLoggedInException;
import org.jboss.seam.security.management.JpaIdentityStore;
import org.jboss.seam.web.ServletContexts;
import org.zanata.common.LocaleId;
import org.zanata.email.ContactAdminEmailStrategy;
import org.zanata.email.ContactLanguageCoordinatorEmailStrategy;
import org.zanata.email.EmailStrategy;
import org.zanata.email.RequestRoleLanguageEmailStrategy;
import org.zanata.email.RequestToJoinLanguageEmailStrategy;
import org.zanata.email.RequestToJoinVersionGroupEmailStrategy;
import org.zanata.model.HAccount;
import org.zanata.model.HLocale;
import org.zanata.model.HPerson;
import org.zanata.model.HProjectIteration;
import org.zanata.seam.scope.ConversationScopeMessages;
import org.zanata.security.UserRedirectBean;
import org.zanata.service.EmailService;
import org.zanata.service.LocaleService;
import org.zanata.ui.faces.FacesMessages;
import org.zanata.util.UrlUtil;
import org.zanata.webtrans.shared.model.ProjectIterationId;

import com.google.common.collect.Lists;

/**
 * Sends an email to a specified role.
 *
 * @author damason@redhat.com
 *
 */
@AutoCreate
@Name("sendEmail")
@NoArgsConstructor
@Scope(ScopeType.PAGE)
@Slf4j
public class SendEmailAction implements Serializable {
    private static final long serialVersionUID = 1L;

    private static final String EMAIL_TYPE_CONTACT_ADMIN = "contact_admin";
    private static final String EMAIL_TYPE_CONTACT_COORDINATOR =
            "contact_coordinator";
    private static final String EMAIL_TYPE_REQUEST_JOIN =
            "request_join_language";
    private static final String EMAIL_TYPE_REQUEST_ROLE =
            "request_role_language";
    private static final String EMAIL_TYPE_REQUEST_TO_JOIN_GROUP =
            "request_to_join_group";

    @In
    private LanguageJoinUpdateRoleAction languageJoinUpdateRoleAction;

    @In
    private VersionGroupJoinAction versionGroupJoinAction;

    @In
    private EmailService emailServiceImpl;

    @In(value = JpaIdentityStore.AUTHENTICATED_USER, required = false)
    private HAccount authenticatedAccount;

    @In
    private LocaleService localeServiceImpl;

    @In
    private LocaleSelector localeSelector;

    @In("jsfMessages")
    private FacesMessages facesMessages;

    @Getter
    @Setter
    private String fromName;

    @Getter
    @Setter
    private String fromLoginName;

    @Email
    @Getter
    @Setter
    private String replyEmail;

    @Getter
    @Setter
    private String subject;

    @Getter
    @Setter
    private String htmlMessage;

    @Getter
    @Setter
    private String emailType;

    @Getter
    private String language;

    @Getter
    private HLocale locale;

    @In
    private ConversationScopeMessages conversationScopeMessages;

    @In
    private UrlUtil urlUtil;

    @In
    private UserRedirectBean userRedirect;

    private List<HPerson> groupMaintainers;

    public static final String SUCCESS = "success";
    public static final String FAILED = "failure";

    @Create
    public void onCreate() {
        if (authenticatedAccount == null) {
            log.warn("accessing SendEmailAction without authenticated account");
            String encodedLocalUrl = urlUtil.getEncodedLocalUrl(
                    ServletContexts.instance().getRequest());
            userRedirect.setUrl(UrlUtil.decodeString(encodedLocalUrl));
            throw new NotLoggedInException();
        }
        fromName = authenticatedAccount.getPerson().getName();
        fromLoginName = authenticatedAccount.getUsername();
        replyEmail = authenticatedAccount.getPerson().getEmail();

        subject = "";
        htmlMessage = "";
    }

    public void setLanguage(String language) {
        this.language = language;
        locale = localeServiceImpl.getByLocaleId(new LocaleId(language));
    }

    /**
     * Sends the email by rendering an appropriate email template with the
     * values in this bean.
     *
     * @return a view to redirect to. This should be replaced with configuration
     *         in pages.xml
     */
    public String send() {
        Locale pervLocale = localeSelector.getLocale();
        localeSelector.setLocale(new Locale("en"));

        try {
            switch (emailType) {
                case EMAIL_TYPE_CONTACT_ADMIN: {
                    EmailStrategy strategy = new ContactAdminEmailStrategy(
                            fromLoginName, fromName, replyEmail,
                            subject, htmlMessage);

                    String msg = emailServiceImpl.sendToAdmins(strategy, null);

                    facesMessages.addGlobal(msg);
                    conversationScopeMessages.setMessage(
                        FacesMessage.SEVERITY_INFO, msg);
                    return SUCCESS;
                }
                case EMAIL_TYPE_CONTACT_COORDINATOR: {
                    String localeNativeName = locale.retrieveNativeName();

                    EmailStrategy strategy =
                            new ContactLanguageCoordinatorEmailStrategy(
                                    fromLoginName, fromName, replyEmail,
                                    subject,
                                    locale.getLocaleId().getId(),
                                    localeNativeName, htmlMessage);
                    String msg = emailServiceImpl.sendToLanguageCoordinators(
                            locale, strategy);

                    facesMessages.addGlobal(msg);
                    conversationScopeMessages.setMessage(
                            FacesMessage.SEVERITY_INFO, msg);
                    return SUCCESS;
                }
                case EMAIL_TYPE_REQUEST_JOIN: {
                    String localeNativeName = locale.retrieveNativeName();

                    EmailStrategy strategy =
                            new RequestToJoinLanguageEmailStrategy(
                                    fromLoginName, fromName, replyEmail,
                                    locale.getLocaleId().getId(),
                                    localeNativeName, htmlMessage,
                                    languageJoinUpdateRoleAction
                                            .getRequestAsTranslator(),
                                    languageJoinUpdateRoleAction
                                            .getRequestAsReviewer(),
                                    languageJoinUpdateRoleAction
                                            .getRequestAsCoordinator());
                    String msg = emailServiceImpl.sendToLanguageCoordinators(
                            locale, strategy);
                    facesMessages.addGlobal(msg);
                    conversationScopeMessages.setMessage(
                            FacesMessage.SEVERITY_INFO, msg);
                    return SUCCESS;
                }
                case EMAIL_TYPE_REQUEST_ROLE: {
                    String localeNativeName = locale.retrieveNativeName();

                    EmailStrategy strategy =
                            new RequestRoleLanguageEmailStrategy(
                                    fromLoginName, fromName, replyEmail,
                                    locale.getLocaleId().getId(),
                                    localeNativeName, htmlMessage,
                                    languageJoinUpdateRoleAction
                                            .requestingTranslator(),
                                    languageJoinUpdateRoleAction
                                            .requestingReviewer(),
                                    languageJoinUpdateRoleAction
                                            .requestingCoordinator());
                    String msg = emailServiceImpl.sendToLanguageCoordinators(
                            locale, strategy);
                    facesMessages.addGlobal(msg);
                    conversationScopeMessages.setMessage(
                            FacesMessage.SEVERITY_INFO, msg);
                    return SUCCESS;
                }
                case EMAIL_TYPE_REQUEST_TO_JOIN_GROUP: {
                    String groupSlug = versionGroupJoinAction.getSlug();
                    String groupName = versionGroupJoinAction.getGroupName();
                    Collection<ProjectIterationId> projectIterIds =
                            Lists.newArrayList();

                    for (VersionGroupJoinAction.SelectableProject version : versionGroupJoinAction
                            .getProjectVersions()) {
                        if (version.isSelected()) {
                            HProjectIteration projIter =
                                    version.getProjectIteration();
                            projectIterIds.add(new ProjectIterationId(
                                    projIter.getProject().getSlug(),
                                    projIter.getSlug(),
                                    projIter.getProjectType()));
                        }
                    }

                    EmailStrategy strategy =
                            new RequestToJoinVersionGroupEmailStrategy(
                                    fromLoginName, fromName, replyEmail,
                                    groupName, groupSlug,
                                    projectIterIds, htmlMessage);
                    String msg =
                            emailServiceImpl
                                    .sendToVersionGroupMaintainers(
                                            groupMaintainers, strategy);
                    conversationScopeMessages.setMessage(
                            FacesMessage.SEVERITY_INFO, msg);
                    return SUCCESS;
                }
                default:
                    throw new Exception("Invalid email type: " + emailType);
            }
        } catch (Exception e) {
            facesMessages.addGlobal(
                    "There was a problem sending the message: "
                            + e.getMessage());
            log.error(
                    "Failed to send email: fromName '{}', fromLoginName '{}', replyEmail '{}', subject '{}', message '{}'",
                    e, fromName, fromLoginName, replyEmail, subject,
                    htmlMessage);
            return FAILED;
        } finally {
            localeSelector.setLocale(pervLocale);
        }
    }

    public void cancel() {
        log.info(
                "Canceled sending email: fromName '{}', fromLoginName '{}', replyEmail '{}', subject '{}', message '{}'",
                fromName, fromLoginName, replyEmail, subject, htmlMessage);
        facesMessages.addGlobal("Sending message canceled");
        conversationScopeMessages.setMessage(FacesMessage.SEVERITY_INFO,
            "Sending message canceled");
    }

    public String sendToVersionGroupMaintainer(List<HPerson> maintainers) {
        groupMaintainers = maintainers;
        return send();
    }
}
