/*
 * Copyright 2015, Red Hat, Inc. and individual contributors
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
package org.zanata.notification;

import javax.annotation.Resource;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.Disposes;
import javax.enterprise.inject.Produces;
import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.Queue;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;
import javax.jms.QueueSender;
import javax.jms.QueueSession;
import javax.jms.Session;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.common.base.Throwables;

/**
 * Produce JMS connection for CDI injection.
 *
 * @author Patrick Huang <a
 *         href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 * @author Sean Flanigan <a href="mailto:sflaniga@redhat.com">sflaniga@redhat.com</a>
 */
@ApplicationScoped
public class JmsResourcesProducer {
    private static final Logger log =
            LoggerFactory.getLogger(JmsResourcesProducer.class);

    // == JMS thread safety note ==
    // The following JMS objects are threadsafe:
    // Destination ConnectionFactory Connection
    // These objects are not threadsafe and must not be shared:
    // Session MessageProducer MessageConsumer
    // The threadsafe objects can be shared, but for better JMS performance
    // it may be better to create a Connection per Session (or per thread,
    // eg ThreadScoped), instead of multiplexing.
    // See http://stackoverflow.com/a/18630122/14379

    @Resource(lookup = "JmsXA")
    private QueueConnectionFactory connectionFactory;

    @Resource(lookup = "jms/queue/MailsQueue")
    private Queue mailQueue;

    @Produces @RequestScoped @InVMJMS
    public QueueConnection createJMSConnection() throws JMSException {
        QueueConnection queueConnection =
                connectionFactory.createQueueConnection();
        queueConnection.start();
        return queueConnection;
    }

    public void closeJMSConnection(
            @Disposes @InVMJMS Connection connection)
            throws JMSException {
        log.debug("________ closing JMS connection");
        connection.close();
    }

    @Produces @RequestScoped @InVMJMS
    public QueueSession createQueueSession(@InVMJMS QueueConnection connection)
            throws JMSException {
        return connection.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
    }

    public void closeQueueSession(@Disposes @InVMJMS QueueSession session) {
        try {
            log.debug("________ closing JMS session");
            session.close();
        } catch (JMSException e) {
            throw Throwables.propagate(e);
        }
    }

    @Produces
    @EmailQueueSender
    public QueueSender createEmailQueueSender(@InVMJMS
            QueueSession session) throws JMSException {
        return session.createSender(mailQueue);
    }

    public void closeQueueSender(@Disposes @EmailQueueSender QueueSender queueSender) {
        try {
            log.debug("________ closing email queue sender");
            queueSender.close();
        } catch (JMSException e) {
            throw Throwables.propagate(e);
        }
    }


}
