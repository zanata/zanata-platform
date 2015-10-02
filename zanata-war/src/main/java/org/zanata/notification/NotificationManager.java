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
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.jms.JMSException;
import javax.jms.ObjectMessage;
import javax.jms.QueueSender;
import javax.jms.QueueSession;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;
import org.jboss.seam.annotations.Observer;
import org.jboss.seam.annotations.Startup;
import org.zanata.events.LanguageTeamPermissionChangedEvent;

import com.google.common.base.Throwables;

/**
 * Centralized place to handle all events that needs to send out notifications.
 *
 * @author Patrick Huang <a
 *         href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@Named("notificationManager")
@javax.enterprise.context.ApplicationScoped
// see below Observer method comment
/* TODO [CDI] Remove @PostConstruct from startup method and make it accept (@Observes @Initialized ServletContext context) */
@Slf4j
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class NotificationManager implements Serializable {
    private static final long serialVersionUID = -1L;

    // JMS EmailQueue Producer.
    @Inject
    @EmailQueueSender
    private QueueSender mailQueueSender;

    @Inject
    @InVMJMS
    private QueueSession queueSession;


    @PostConstruct
    public void onCreate() {
        try {
            mailQueueSender.getQueue();
        } catch (JMSException e) {
            // it will never reach this block. As long as you call getQueue()
            // and if the queue is not defined, seam will terminate:
            // org.jboss.seam.jms.ManagedQueueSender.getQueue(ManagedQueueSender.java:45
            Throwables.propagate(e);
        }
    }

    // Once migrated to CDI events, CDI will instantiate NotificationManager bean for us. This will remove the need of Seam /* TODO [CDI] Remove @PostConstruct from startup method and make it accept (@Observes @Initialized ServletContext context) */
    @Observer(LanguageTeamPermissionChangedEvent.EVENT_NAME)
    public
            void onLanguageTeamPermissionChanged(
                    final @Observes LanguageTeamPermissionChangedEvent event) {
        try {
            ObjectMessage message =
                    queueSession.createObjectMessage(event);
            message.setObjectProperty(MessagePropertiesKey.objectType.name(),
                    event.getClass().getCanonicalName());
            mailQueueSender.send(message);
        } catch (JMSException e) {
            throw Throwables.propagate(e);
        }
    }

    /*
     * we use this as property key in the JMS message to denote what type of
     * message/event this is and the queue consumer can base on this value to
     * find appropriate handler to handle the message payload.
     *
     * @seeorg.zanata.notification.EmailQueueMessageReceiver
     */
    static enum MessagePropertiesKey {
        objectType
    }
}
