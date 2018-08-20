/*
 * Copyright 2014, Red Hat, Inc. and individual contributors
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
package org.zanata.email;

import com.google.common.base.Optional;
import org.zanata.i18n.Messages;
import javax.mail.internet.InternetAddress;

import cyclops.collections.immutable.PersistentMapX;

import static org.zanata.email.Addresses.getReplyTo;
import static org.zanata.util.HtmlUtil.textToSafeHtml;

/**
 * @author Sean Flanigan
 *         <a href="mailto:sflaniga@redhat.com">sflaniga@redhat.com</a>
 */
public class ContactLanguageCoordinatorEmailStrategy extends
        VelocityEmailStrategy {
    private final String receiver;
    private final String fromLoginName;
    private final String fromName;
    private final String replyEmail;
    private final String userSubject;
    private final String localeId;
    private final String localeNativeName;
    private final String userMessage;

    @Override
    public String getBodyResourceName() {
        return "org/zanata/email/templates/email_coordinator.vm";
    }

    @Override
    public Optional<InternetAddress[]> getReplyToAddress() {
        return Optional.of(getReplyTo(replyEmail, fromName));
    }

    @Override
    public String getSubject(Messages msgs) {
        return msgs.format("jsf.email.coordinator.SubjectPrefix",
                localeId, fromLoginName) + " " + userSubject;
    }

    @Override
    public PersistentMapX<String, Object> makeContext(PersistentMapX<String, Object> genericContext,
            InternetAddress[] toAddresses) {
        PersistentMapX<String, Object> context =
                super.makeContext(genericContext, toAddresses);
        return context.plus("receiver", receiver)
                .plus("fromLoginName", fromLoginName)
                .plus("fromName", fromName).plus("replyEmail", replyEmail)
                .plus("localeId", localeId)
                .plus("localeNativeName", localeNativeName)
                .plus("safeHtmlMessage", textToSafeHtml(userMessage));
    }

    @java.beans.ConstructorProperties({ "receiver", "fromLoginName", "fromName",
            "replyEmail", "userSubject", "localeId", "localeNativeName",
            "userMessage" })
    public ContactLanguageCoordinatorEmailStrategy(final String receiver,
            final String fromLoginName,
            final String fromName, final String replyEmail,
            final String userSubject, final String localeId,
            final String localeNativeName, final String userMessage) {
        this.receiver = receiver;
        this.fromLoginName = fromLoginName;
        this.fromName = fromName;
        this.replyEmail = replyEmail;
        this.userSubject = userSubject;
        this.localeId = localeId;
        this.localeNativeName = localeNativeName;
        this.userMessage = userMessage;
    }
}
