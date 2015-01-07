/*
 * Copyright 2014, Red Hat, Inc. and individual contributors as indicated by the
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
package org.zanata.notification;

import java.io.Serializable;

import javax.mail.internet.InternetAddress;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.zanata.ApplicationConfiguration;
import org.zanata.email.Addresses;
import org.zanata.email.EmailBuilder;
import org.zanata.email.LanguageTeamPermissionChangeEmailStrategy;
import org.zanata.events.LanguageTeamPermissionChangedEvent;
import org.zanata.i18n.Messages;
import lombok.extern.slf4j.Slf4j;

/**
 * Handles language team permissions change JMS message. This will build and
 * send out an email to the person affected.
 * N.B. We can only have application or
 * stateless scope beans in here.
 *
 * @see EmailQueueMessageReceiver
 * @author Patrick Huang <a
 *         href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@Name("languageTeamPermissionChangeJmsMessagePayloadHandler")
@Scope(ScopeType.STATELESS)
@AutoCreate
@Slf4j
public class LanguageTeamPermissionChangeJmsMessagePayloadHandler implements
        EmailQueueMessageReceiver.JmsMessagePayloadHandler {
    @In
    private EmailBuilder emailBuilder;

    @In
    private Messages msgs;

    @In
    private ApplicationConfiguration applicationConfiguration;

    @Override
    public void handle(Serializable data) {
        if (!(data instanceof LanguageTeamPermissionChangedEvent)) {
            log.error("can not handle data other than type {}",
                    LanguageTeamPermissionChangedEvent.class);
            return;
        }
        LanguageTeamPermissionChangedEvent changedEvent =
                LanguageTeamPermissionChangedEvent.class.cast(data);
        log.debug("language team permission change data:{}", changedEvent);

        if (!changedEvent.hasPermissionsChanged()) {
            // permission didn't really changed
            return;
        }

        String receivedReason =
                msgs.format("jsf.email.languageteam.permission.ReceivedReason",
                        changedEvent.getLanguage());
        String contactTeamCoordinatorLink =
                applicationConfiguration.getServerPath() +
                        "/language/contact/" + changedEvent.getLanguage();
        LanguageTeamPermissionChangeEmailStrategy emailStrategy =
                new LanguageTeamPermissionChangeEmailStrategy(
                        changedEvent, msgs, contactTeamCoordinatorLink);
        InternetAddress to =
                Addresses.getAddress(changedEvent.getEmail(),
                        changedEvent.getName());

        emailBuilder.sendMessage(emailStrategy, receivedReason, to);
    }

}
