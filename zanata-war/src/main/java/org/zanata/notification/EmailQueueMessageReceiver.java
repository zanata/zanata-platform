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
import java.util.Collections;
import java.util.Map;

import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.inject.Named;
import org.zanata.events.LanguageTeamPermissionChangedEvent;

import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableMap;

import static com.google.common.base.Strings.nullToEmpty;
import static org.zanata.notification.NotificationManager.MessagePropertiesKey;

/**
 * JMS EmailsQueue consumer. It will base on
 * org.zanata.notification.NotificationManager.MessagePropertiesKey#objectType
 * value to find a message payload handler. The objectType value is normally the
 * canonical name of the event class.
 *
 * @author Patrick Huang <a
 *         href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@MessageDriven(activationConfig = {
        @ActivationConfigProperty(
                propertyName = "destinationType",
                propertyValue = "javax.jms.Queue"
        ),
        @ActivationConfigProperty(
                propertyName = "destination",
                propertyValue = "jms/queue/MailsQueue"
        )
})
@Named("emailQueueMessageReceiver")
@Slf4j
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class EmailQueueMessageReceiver implements MessageListener {

    private static Map<String, JmsMessagePayloadHandler> handlers = Collections
            .emptyMap();

    @Inject /* TODO [CDI] check this: migrated from @In("languageTeamPermissionChangeJmsMessagePayloadHandler") */
    private LanguageTeamPermissionChangeJmsMessagePayloadHandler languageTeamHandler;

    @Override
    public void onMessage(Message message) {
        if (message instanceof ObjectMessage) {
            try {
                String objectType =
                        nullToEmpty(
                        message.getStringProperty(
                                MessagePropertiesKey.objectType.name()));

                JmsMessagePayloadHandler jmsMessagePayloadHandler =
                        getHandlers().get(objectType);
                if (jmsMessagePayloadHandler != null) {
                    log.debug("found handler for message object type [{}]",
                            objectType);
                    jmsMessagePayloadHandler.handle(((ObjectMessage) message)
                            .getObject());
                } else {
                    log.warn("can not find handler for message:{}", message);
                }

            } catch (JMSException e) {
                log.warn("error handling jms message: {}", message);
                Throwables.propagate(e);
            }
        }
    }

    public Map<String, JmsMessagePayloadHandler> getHandlers() {
        if (handlers.isEmpty()) {
            synchronized (this) {
                if (handlers.isEmpty()) {
                    handlers =
                            ImmutableMap
                                    .<String, JmsMessagePayloadHandler> builder()
                                    .put(LanguageTeamPermissionChangedEvent.class
                                            .getCanonicalName(),
                                            languageTeamHandler)
                                    .build();
                }
            }
            log.info("email queue payload handlers: {}", handlers);
        }
        return handlers;
    }

    public static interface JmsMessagePayloadHandler {
        void handle(Serializable data);
    }
}
