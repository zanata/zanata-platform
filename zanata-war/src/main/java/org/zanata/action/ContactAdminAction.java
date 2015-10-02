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

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;
import org.zanata.security.annotations.CheckLoggedIn;
import org.zanata.security.annotations.CheckPermission;
import org.zanata.security.annotations.CheckRole;
import org.zanata.seam.security.ZanataJpaIdentityStore;
import org.zanata.email.ContactAdminAnonymousEmailStrategy;
import org.zanata.email.ContactAdminEmailStrategy;
import org.zanata.email.EmailStrategy;
import org.zanata.i18n.Messages;
import org.zanata.model.HAccount;
import org.zanata.security.annotations.ZanataSecured;
import org.zanata.service.EmailService;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.zanata.ui.faces.FacesMessages;
import org.zanata.util.HttpUtil;

import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;

/**
 * Handles send email to admin - Contact admin(Registered and non-registered users)
 *
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
@Named("contactAdminAction")
@javax.faces.bean.ViewScoped
@Slf4j
@ZanataSecured
public class ContactAdminAction implements Serializable {

    @Inject /* TODO [CDI] check this: migrated from @In(value = ZanataJpaIdentityStore.AUTHENTICATED_USER, required = false) */
    private HAccount authenticatedAccount;

    @Inject
    private EmailService emailServiceImpl;

    @Inject
    private Messages msgs;

    @Inject
    private FacesMessages facesMessages;

    @Getter
    @Setter
    private String message;

    @Getter
    @Setter
    private String subject;

    /**
     * Send email to admin by registered user.
     */
    @CheckLoggedIn
    public void send() {
        String fromName = authenticatedAccount.getPerson().getName();
        String fromLoginName = authenticatedAccount.getUsername();
        String replyEmail = authenticatedAccount.getPerson().getEmail();
        subject = msgs.get("jsf.message.admin.inquiry.subject");
        try {
            EmailStrategy strategy = new ContactAdminEmailStrategy(
                fromLoginName, fromName, replyEmail, subject, message);

            facesMessages.addGlobal(emailServiceImpl.sendToAdmins(strategy,
                null));
        } catch (Exception e) {
            sendEmailFailedNotification(e, fromLoginName);
        }
    }

    /**
     * Send email to admin by anonymous user.
     */
    public void sendAnonymous() {
        String ipAddress = getClientIp(); //client ip address
        subject = msgs.get("jsf.message.admin.inquiry.subject");

        try {
            EmailStrategy strategy = new ContactAdminAnonymousEmailStrategy(
                ipAddress, subject, message);

            facesMessages.addGlobal(emailServiceImpl.sendToAdmins(strategy,
                null));
        } catch (Exception e) {
            sendEmailFailedNotification(e, ipAddress);
        }
    }

    private void sendEmailFailedNotification(Exception e, String fromName) {
        StringBuilder sb = new StringBuilder()
            .append("Failed to send email with subject '")
            .append(subject)
            .append("' , message '").append(message)
            .append("', from '").append(fromName)
            .append("'");
        log.error("{}. {}", sb.toString(), e);
        facesMessages.addGlobal(sb.toString());
    }

    private String getClientIp() {
        HttpServletRequest request =
                (HttpServletRequest) FacesContext.getCurrentInstance()
                        .getExternalContext().getRequest();
        return HttpUtil.getClientIp(request);
    }
}
