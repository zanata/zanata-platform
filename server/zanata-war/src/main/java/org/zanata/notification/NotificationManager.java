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

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.jms.JMSException;
import javax.jms.ObjectMessage;
import javax.jms.QueueSender;
import javax.jms.QueueSession;
import org.zanata.events.LanguageTeamPermissionChangedEvent;
import com.google.common.base.Throwables;

/**
 * Centralized place to handle all events that needs to send out notifications.
 *
 * @author Patrick Huang
 *         <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@ApplicationScoped
public class NotificationManager {
    private static final org.slf4j.Logger log =
            org.slf4j.LoggerFactory.getLogger(NotificationManager.class);

    public void onLanguageTeamPermissionChanged(
            @Observes final LanguageTeamPermissionChangedEvent event,
            @InVMJMS final QueueSession queueSession,
            @EmailQueueSender final QueueSender mailQueueSender) {
        try {
            ObjectMessage message = queueSession.createObjectMessage(event);
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
     * @see org.zanata.notification.EmailQueueMessageReceiver
     */

    enum MessagePropertiesKey {
        objectType;

    }
}
