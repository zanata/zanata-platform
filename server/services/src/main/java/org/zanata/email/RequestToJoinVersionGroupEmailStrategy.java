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
import org.zanata.webtrans.shared.model.ProjectIterationId;
import javax.mail.internet.InternetAddress;
import java.util.Collection;

import cyclops.collections.immutable.PersistentMapX;

import static org.zanata.email.Addresses.getReplyTo;
import static org.zanata.util.HtmlUtil.textToSafeHtml;

/**
 * @author Sean Flanigan
 *         <a href="mailto:sflaniga@redhat.com">sflaniga@redhat.com</a>
 */
public class RequestToJoinVersionGroupEmailStrategy extends
        VelocityEmailStrategy {
    private final String fromLoginName;
    private final String fromName;
    private final String replyEmail;
    private final String groupName;
    private final String groupSlug;
    private final Collection<ProjectIterationId> projectIterationIds;
    private final String userMessage;

    @Override
    public String getBodyResourceName() {
        return "org/zanata/email/templates/email_request_to_join_group.vm";
    }

    @Override
    public Optional<InternetAddress[]> getReplyToAddress() {
        return Optional.of(getReplyTo(replyEmail, fromName));
    }

    @Override
    public String getSubject(Messages msgs) {
        return msgs.format("jsf.email.JoinGroupRequest.Subject", groupName);
    }

    @Override
    public PersistentMapX<String, Object> makeContext(PersistentMapX<String, Object> genericContext,
            InternetAddress[] toAddresses) {
        PersistentMapX<String, Object> context =
                super.makeContext(genericContext, toAddresses);
        return context.plus("fromLoginName", fromLoginName)
                .plus("fromName", fromName).plus("replyEmail", replyEmail)
                .plus("groupName", groupName).plus("versionGroupSlug", groupSlug)
                .plus("projectIterationIds", projectIterationIds)
                .plus("safeHtmlMessage", textToSafeHtml(userMessage));
    }

    @java.beans.ConstructorProperties({ "fromLoginName", "fromName",
            "replyEmail", "groupName", "groupSlug", "projectIterationIds",
            "userMessage" })
    public RequestToJoinVersionGroupEmailStrategy(final String fromLoginName,
            final String fromName, final String replyEmail,
            final String groupName, final String groupSlug,
            final Collection<ProjectIterationId> projectIterationIds,
            final String userMessage) {
        this.fromLoginName = fromLoginName;
        this.fromName = fromName;
        this.replyEmail = replyEmail;
        this.groupName = groupName;
        this.groupSlug = groupSlug;
        this.projectIterationIds = projectIterationIds;
        this.userMessage = userMessage;
    }
}
