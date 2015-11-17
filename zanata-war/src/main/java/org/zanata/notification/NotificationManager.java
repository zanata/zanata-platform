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

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Dependent;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.jms.JMSException;
import javax.jms.ObjectMessage;
import javax.jms.QueueConnection;
import javax.jms.QueueSender;
import javax.jms.QueueSession;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;
import javax.jms.Session;

import org.zanata.events.LanguageTeamPermissionChangedEvent;

import com.google.common.base.Throwables;

/**
 * Centralized place to handle all events that needs to send out notifications.
 *
 * @author Patrick Huang <a
 *         href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@Named("notificationManager")
@Dependent
@Slf4j
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class NotificationManager {

    @Inject
    @InVMJMS
    private QueueConnection connection;

    @Inject
    private JmsResourcesProducer resourcesProducer;

    private transient QueueSender mailQueueSender;

    private transient QueueSession queueSession;


    @PostConstruct
    public void onCreate() {
        try {
            queueSession = resourcesProducer.createQueueSession(connection);
            mailQueueSender =
                    resourcesProducer.createEmailQueueSender(queueSession);
            // force initialisation:
            mailQueueSender.getQueue();
        } catch (JMSException e) {
            // it will never reach this block. As long as you call getQueue()
            // and if the queue is not defined, seam will terminate:
            // org.jboss.seam.jms.ManagedQueueSender.getQueue(ManagedQueueSender.java:45
            Throwables.propagate(e);
        }
    }

    public void onLanguageTeamPermissionChanged(
            final @Observes LanguageTeamPermissionChangedEvent event) {
        try {
            ObjectMessage message =
                    queueSession.createObjectMessage(event);
            message.setObjectProperty(MessagePropertiesKey.objectType.name(),
                    event.getClass().getCanonicalName());
            mailQueueSender.send(message);
        } catch (JMSException e) {
            throw Throwables.propagate(e);
        } finally {
            // We can not use JmsResourceProducer to produce session and
            // mailQueueSender. If we do that, it seems to only dispose queueSender
            // and session but not connection. Server will log a INFO with long
            // stacktrace:
            // 14:48:35,653 INFO  [org.jboss.jca.core.api.connectionmanager.ccm.CachedConnectionManager] (http-/0.0.0.0:8180-3) IJ000100: Closing a connection for you. Please close them yourself: org.hornetq.ra.HornetQRASession@74dd3df2: java.lang.Throwable: STACKTRACE
            try {
                mailQueueSender.close();
                queueSession.close();
            } catch (JMSException e) {
                log.error("error closing JMS resources", e);
            }
        }
    }

    /*
     * we use this as property key in the JMS message to denote what type of
     * message/event this is and the queue consumer can base on this value to
     * find appropriate handler to handle the message payload.
     *
     * @see org.zanata.notification.EmailQueueMessageReceiver
     */
    enum MessagePropertiesKey {
        objectType
    }
}
