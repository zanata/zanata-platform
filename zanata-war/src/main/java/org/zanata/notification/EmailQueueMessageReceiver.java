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
import java.util.Map;

import javax.annotation.Nullable;
import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.inject.Inject;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;

import com.google.common.collect.ImmutableMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zanata.events.LanguageTeamPermissionChangedEvent;

import com.google.common.base.Throwables;
import org.zanata.util.IServiceLocator;

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
 * @author Sean Flanigan <a href="mailto:sflaniga@redhat.com">sflaniga@redhat.com</a>
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
public class EmailQueueMessageReceiver implements MessageListener {
    private static final Logger log = LoggerFactory.getLogger(EmailQueueMessageReceiver.class);
    private static final Map<String, Class<? extends JmsMessagePayloadHandler>> handlers;

    static {
        handlers =
                ImmutableMap.of(
                        LanguageTeamPermissionChangedEvent.class.getCanonicalName(),
                        LanguageTeamPermissionChangeJmsMessagePayloadHandler.class);
        log.info("email queue payload handlers: {}", handlers);
    }

    private IServiceLocator serviceLocator;

    @SuppressWarnings("unused")
    public EmailQueueMessageReceiver() {
    }

    public @Inject EmailQueueMessageReceiver(
            IServiceLocator serviceLocator) {
        this.serviceLocator = serviceLocator;
    }

    @Override
    public void onMessage(Message message) {
        if (message instanceof ObjectMessage) {
            try {
                ObjectMessage om = (ObjectMessage) message;
                String objectType = nullToEmpty(
                        message.getStringProperty(
                                MessagePropertiesKey.objectType.name()));
                JmsMessagePayloadHandler handler = getHandler(objectType);
                if (handler != null) {
                    log.debug("found handler for message object type [{}]",
                            objectType);
                    handler.handle(om.getObject());
                } else {
                    log.warn("cannot find handler for message: {}", message);
                }
            } catch (Exception e) {
                log.warn("error handling jms message: {}", message);
                Throwables.propagate(e);
            }
        }
    }

    private @Nullable JmsMessagePayloadHandler getHandler(
            String objectType) throws ClassNotFoundException {
        Class<? extends JmsMessagePayloadHandler> handlerClass =
                handlers.get(objectType);
        if (handlerClass != null) {
            return serviceLocator.getInstance(handlerClass);
        } else {
            return null;
        }
    }

    public interface JmsMessagePayloadHandler {
        void handle(Serializable data);
    }
}
