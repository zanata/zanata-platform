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

/**
 * Produce JMS related resources for CDI injection.
 *
 * @author Patrick Huang <a
 *         href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@ApplicationScoped
public class JmsResourcesProducer {
    @Resource(name = "JmsXA")
    private QueueConnectionFactory connectionFactory;

    @Resource(name = "jms/queue/MailsQueue")
    private Queue mailQueue;

    @Produces @InVMJMS
    public QueueConnection createOrderConnection() throws JMSException {
        return connectionFactory.createQueueConnection();
    }

    public void closeOrderConnection(
            @Disposes @InVMJMS Connection connection)
            throws JMSException {
        connection.close();
    }

    @Produces @InVMJMS
    public QueueSession createQueueSession(@InVMJMS QueueConnection connection)
            throws JMSException {
        return connection.createQueueSession(true, Session.AUTO_ACKNOWLEDGE);
    }

    public void closeOrderSession(@Disposes @InVMJMS QueueSession session)
            throws JMSException {
        session.close();
    }

    @Produces
    @EmailQueueSender
    public QueueSender createEmailQueueSender(
            @InVMJMS QueueSession session) throws JMSException {
        return session.createSender(mailQueue);
    }

    public void closeEmailQueueSender(
            @Disposes @EmailQueueSender QueueSender queueSender)
            throws JMSException {
        queueSender.close();
    }
}
