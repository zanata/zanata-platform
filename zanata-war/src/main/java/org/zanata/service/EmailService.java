/*
 * Copyright 2012, Red Hat, Inc. and individual contributors as indicated by the
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
package org.zanata.service;

import java.util.List;

import org.zanata.model.HPerson;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
public interface EmailService {
    public static final String ADMIN_EMAIL_TEMPLATE =
            "/WEB-INF/facelets/email/email_admin.xhtml";
    public static final String COORDINATOR_EMAIL_TEMPLATE =
            "/WEB-INF/facelets/email/email_coordinator.xhtml";
    public static final String REQUEST_TO_JOIN_EMAIL_TEMPLATE =
            "/WEB-INF/facelets/email/email_request_to_join_language.xhtml";
    public static final String REQUEST_ROLE_EMAIL_TEMPLATE =
            "/WEB-INF/facelets/email/email_request_role_language.xhtml";
    public static final String REQUEST_TO_JOIN_GROUP_EMAIL_TEMPLATE =
            "/WEB-INF/facelets/email/email_request_to_join_group.xhtml";
    public static final String ACTIVATION_ACCOUNT_EMAIL_TEMPLATE =
            "/WEB-INF/facelets/email/activation.xhtml";

    /**
     * sends emails to configured admin emails for server, or admin users if no
     * server emails are configured.
     *
     *
     * @param emailTemplate
     * @param fromName
     * @param fromLoginName
     * @param replyEmail
     * @param subject
     * @param message
     * @return
     */
    String sendToAdminEmails(String emailTemplate, String fromName,
            String fromLoginName, String replyEmail, String subject,
            String message);

    /**
     * sends emails to version group maintainers -> admin -> admin users
     *
     * @param emailTemplate
     * @param maintainers
     * @param fromName
     * @param fromLoginName
     * @param replyEmail
     * @param subject
     * @param message
     * @return
     */
    String sendToVersionGroupMaintainer(String emailTemplate,
            List<HPerson> maintainers, String fromName, String fromLoginName,
            String replyEmail, String subject, String message);

    /**
     * sends emails to language coordinators -> admin -> admin users
     *
     * @param emailTemplate
     * @param coordinators
     * @param fromName
     * @param fromLoginName
     * @param replyEmail
     * @param subject
     * @param message
     * @param language
     * @return
     */
    String sendToLanguageCoordinators(String emailTemplate,
            List<HPerson> coordinators, String fromName, String fromLoginName,
            String replyEmail, String subject, String message, String language);

    /**
     * send account activation email to register user
     *
     * @param emailTemplate
     * @param toName
     * @param toEmailAddr
     * @param activationKey
     * @return
     */
    String sendActivationEmail(String emailTemplate, String toName,
            String toEmailAddr, String activationKey);
}
