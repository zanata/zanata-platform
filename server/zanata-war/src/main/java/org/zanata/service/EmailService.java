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

import javax.annotation.Nullable;

import org.zanata.common.LocaleId;
import org.zanata.email.EmailStrategy;
import org.zanata.model.HLocaleMember;
import org.zanata.model.HPerson;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
public interface EmailService {

    /**
     * send account activation email to register user
     *
     * @param toName
     * @param toEmailAddr
     * @param activationKey
     * @return
     */
    String sendActivationEmail(String toName,
            String toEmailAddr, String activationKey);

    String sendActivationAndResetPasswordEmail(String toName,
            String toEmailAddr, String activationKey, String resetPasswordKey);

    String sendEmailValidationEmail(String toName,
            String toEmailAddr, String activationKey);

    String sendPasswordResetEmail(HPerson person, String key);

    /**
     * sends emails to configured admin emails for server, or admin users if no
     * server emails are configured.
     *
     * @param strategy - Email template
     * @param receivedReasons
     *            - Reasons of why users are receiving email as admin can
     *            received via: 1) Direct email from logged in users. 2) User
     *            request to join language team when there's no coordinator. 3)
     *            User request to update their role in language team when
     *            there's no coordinator. 4) User request to join a version group
     *            when there's no maintainer.
     */
    String sendToAdmins(EmailStrategy strategy,
            @Nullable List<String> receivedReasons);

    /**
     * sends emails to version group maintainers -> admin -> admin users
     */
    String sendToVersionGroupMaintainers(List<HPerson> maintainers,
            EmailStrategy strategy);

    /**
     * sends emails to language coordinators -> admin -> admin users
     *
     */
    String sendToLanguageCoordinators(LocaleId localeId,
            EmailStrategy strategy);

    String sendUsernameChangedEmail(String email, String newUsername);

    /**
     * sends email to requester of the language request
     */
    void sendToLanguageRequester(EmailStrategy strategy, HPerson person);

    /**
     * sends email to language team members.
     */
    String sendToLanguageTeamMembers(LocaleId localeId, EmailStrategy strategy,
        List<HLocaleMember> members);
}
