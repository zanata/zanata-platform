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

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.zanata.security.annotations.CheckLoggedIn;
import org.zanata.security.annotations.CheckPermission;
import org.zanata.security.annotations.CheckRole;
import org.zanata.seam.security.ZanataJpaIdentityStore;
import org.zanata.common.LocaleId;
import org.zanata.dao.LocaleMemberDAO;
import org.zanata.email.EmailStrategy;
import org.zanata.email.RequestToJoinLanguageEmailStrategy;
import org.zanata.i18n.Messages;
import org.zanata.model.HAccount;
import org.zanata.model.HLocale;
import org.zanata.model.HLocaleMember;
import org.zanata.security.annotations.ZanataSecured;
import org.zanata.service.EmailService;
import org.zanata.service.LocaleService;
import org.zanata.ui.faces.FacesMessages;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */

@AutoCreate
@Name("languageJoinAction")
@Scope(ScopeType.PAGE)
@ZanataSecured
@Slf4j
public class LanguageJoinAction implements Serializable {
    private static final long serialVersionUID = 1L;

    @In
    private LocaleMemberDAO localeMemberDAO;

    @In
    private LocaleService localeServiceImpl;

    @In
    private EmailService emailServiceImpl;

    @In("jsfMessages")
    private FacesMessages facesMessages;

    @In
    private Messages msgs;

    @Getter
    @Setter
    private boolean requestAsTranslator;

    @Getter
    @Setter
    private boolean requestAsReviewer;

    @Getter
    @Setter
    private boolean requestAsCoordinator;

    @Setter
    @Getter
    private String language;

    private HLocale locale;

    @Getter
    @Setter
    private String message;

    @In(value = ZanataJpaIdentityStore.AUTHENTICATED_USER, required = false)
    private HAccount authenticatedAccount;

    public boolean hasSelectedRole() {
        return requestAsTranslator || requestAsReviewer || requestAsCoordinator;
    }

    public void bindRole(String role, boolean checked) {
        if (role.equals("translator")) {
            requestAsTranslator = checked;
        } else if (role.equals("reviewer")) {
            requestAsReviewer = checked;
        } else if (role.equals("coordinator")) {
            requestAsCoordinator = checked;
        }
    }

    private void reset() {
        requestAsTranslator = false;
        requestAsReviewer = false;
        requestAsCoordinator = false;
        message = "";
    }

    @CheckLoggedIn
    public void send() {
        String fromName = authenticatedAccount.getPerson().getName();
        String fromLoginName = authenticatedAccount.getUsername();
        String replyEmail = authenticatedAccount.getPerson().getEmail();

        EmailStrategy strategy =
                new RequestToJoinLanguageEmailStrategy(
                        fromLoginName, fromName, replyEmail,
                        locale.getLocaleId().getId(),
                        locale.retrieveNativeName(), message,
                        isRequestAsTranslator(),
                        isRequestAsReviewer(),
                        isRequestAsCoordinator());
        try {
            facesMessages.addGlobal(emailServiceImpl
                .sendToLanguageCoordinators(locale.getLocaleId(), strategy));
        } catch (Exception e) {
            String subject = strategy.getSubject(msgs);

            StringBuilder sb =
                new StringBuilder()
                    .append("Failed to send email with subject '")
                    .append(strategy.getSubject(msgs))
                    .append("' , message '").append(message)
                    .append("'");
            log.error(
                    "Failed to send email: fromName '{}', fromLoginName '{}', replyEmail '{}', subject '{}', message '{}'. {}",
                    fromName, fromLoginName, replyEmail, subject, message, e);
            facesMessages.addGlobal(sb.toString());
        } finally {
            reset();
        }
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
        return localeMemberDAO.findByPersonAndLocale(authenticatedAccount
                .getPerson().getId(), new LocaleId(language));
    }
}
