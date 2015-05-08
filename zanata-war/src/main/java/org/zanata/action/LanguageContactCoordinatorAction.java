/*
 * Copyright 2015, Red Hat, Inc. and individual contributors as indicated by the
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

import javax.faces.application.FacesMessage;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.security.Restrict;
import org.jboss.seam.security.management.JpaIdentityStore;
import org.zanata.common.LocaleId;
import org.zanata.email.ContactLanguageCoordinatorEmailStrategy;
import org.zanata.email.EmailStrategy;
import org.zanata.i18n.Messages;
import org.zanata.model.HAccount;
import org.zanata.model.HLocale;
import org.zanata.service.EmailService;
import org.zanata.service.LocaleService;
import org.zanata.ui.faces.FacesMessages;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
@AutoCreate
@Name("languageContactCoordinatorAction")
@Scope(ScopeType.PAGE)
@Slf4j
public class LanguageContactCoordinatorAction implements Serializable {

    @In(value = JpaIdentityStore.AUTHENTICATED_USER, required = false)
    private HAccount authenticatedAccount;

    @In("jsfMessages")
    private FacesMessages facesMessages;

    @In
    private EmailService emailServiceImpl;

    @In
    private LocaleService localeServiceImpl;

    @In
    private Messages msgs;

    @Getter
    @Setter
    private String message;

    @Getter
    @Setter
    private String localeId;

    private HLocale locale;

    @Restrict("#{identity.loggedIn}")
    public void send() {
        String fromName = authenticatedAccount.getPerson().getName();
        String fromLoginName = authenticatedAccount.getUsername();
        String replyEmail = authenticatedAccount.getPerson().getEmail();

        String localeNativeName = getLocale().retrieveNativeName();

        EmailStrategy strategy =
                new ContactLanguageCoordinatorEmailStrategy(
                        fromLoginName, fromName, replyEmail,
                        getSubject(),
                        getLocale().getLocaleId().getId(),
                        localeNativeName, message);

        try {
            String msg = emailServiceImpl.sendToLanguageCoordinators(
                    getLocale().getLocaleId(), strategy);

            facesMessages.addGlobal(msg);
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
        }
    }

    private String getSubject() {
        return msgs.format("jsf.message.coordinator.inquiry.subject", localeId,
                getLocale().retrieveDisplayName());
    }

    public HLocale getLocale() {
        if (locale == null) {
            locale = localeServiceImpl.getByLocaleId(new LocaleId(localeId));
        }
        return locale;
    }
}
