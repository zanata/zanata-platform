/*
 * Copyright 2016, Red Hat, Inc. and individual contributors as indicated by the
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
package org.zanata.email;

import org.zanata.i18n.Messages;

import javax.mail.internet.InternetAddress;

import cyclops.collections.immutable.PersistentMapX;

import static org.zanata.util.HtmlUtil.textToSafeHtml;

/**
 * @author Alex Eng <a href="aeng@redhat.com">aeng@redhat.com</a>
 */
public class ContactLanguageTeamMembersEmailStrategy extends
        VelocityEmailStrategy {
    private final String fromLoginName;
    private final String userSubject;
    private final String localeId;
    private final String localeNativeName;
    private final String userMessage;
    private final String contactCoordinatorLink;

    @Override
    public String getBodyResourceName() {
        return "org/zanata/email/templates/email_language_team_members.vm";
    }

    @Override
    public String getSubject(Messages msgs) {
        return msgs.format("jsf.email.language.members.SubjectPrefix", localeId,
                fromLoginName) + " " + userSubject;
    }

    @Override
    public PersistentMapX<String, Object> makeContext(
            PersistentMapX<String, Object> genericContext,
            InternetAddress[] toAddresses) {
        PersistentMapX<String, Object> context =
                super.makeContext(genericContext, toAddresses);
        return context.plus("contactCoordinatorLink", contactCoordinatorLink)
                .plus("localeNativeName", localeNativeName)
                .plus("safeHtmlMessage", textToSafeHtml(userMessage));
    }

    @java.beans.ConstructorProperties({ "fromLoginName", "userSubject",
            "localeId", "localeNativeName", "userMessage",
            "contactCoordinatorLink" })
    public ContactLanguageTeamMembersEmailStrategy(final String fromLoginName,
            final String userSubject, final String localeId,
            final String localeNativeName, final String userMessage,
            final String contactCoordinatorLink) {
        this.fromLoginName = fromLoginName;
        this.userSubject = userSubject;
        this.localeId = localeId;
        this.localeNativeName = localeNativeName;
        this.userMessage = userMessage;
        this.contactCoordinatorLink = contactCoordinatorLink;
    }
}
