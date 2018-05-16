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
import javaslang.collection.Map;
import org.zanata.i18n.Messages;
import javax.mail.internet.InternetAddress;
import static org.zanata.email.Addresses.getReplyTo;
import static org.zanata.util.HtmlUtil.textToSafeHtml;

/**
 * @author Sean Flanigan
 *         <a href="mailto:sflaniga@redhat.com">sflaniga@redhat.com</a>
 */
public class ContactAdminEmailStrategy extends VelocityEmailStrategy {
    private final String fromLoginName;
    private final String fromName;
    private final String replyEmail;
    private final String userSubject;
    private final String userMessage;

    @Override
    public String getBodyResourceName() {
        return "org/zanata/email/templates/email_admin.vm";
    }

    @Override
    public Optional<InternetAddress[]> getReplyToAddress() {
        return Optional.of(getReplyTo(replyEmail, fromName));
    }

    @Override
    public String getSubject(Messages msgs) {
        return msgs.format("jsf.email.admin.SubjectPrefix", fromLoginName) + " "
                + userSubject;
    }

    @Override
    public Map<String, Object> makeContext(Map<String, Object> genericContext,
            InternetAddress[] toAddresses) {
        Map<String, Object> context =
                super.makeContext(genericContext, toAddresses);
        return context.put("fromLoginName", fromLoginName)
                .put("fromName", fromName).put("replyEmail", replyEmail)
                .put("safeHtmlMessage", textToSafeHtml(userMessage));
    }

    @java.beans.ConstructorProperties({ "fromLoginName", "fromName",
            "replyEmail", "userSubject", "userMessage" })
    public ContactAdminEmailStrategy(final String fromLoginName,
            final String fromName, final String replyEmail,
            final String userSubject, final String userMessage) {
        this.fromLoginName = fromLoginName;
        this.fromName = fromName;
        this.replyEmail = replyEmail;
        this.userSubject = userSubject;
        this.userMessage = userMessage;
    }
}
