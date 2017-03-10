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

import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.inject.Named;

/**
 * Consumer for JMS ExpiryQueue.
 *
 * @author Patrick Huang
 *         <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@MessageDriven(activationConfig = {
        @ActivationConfigProperty(propertyName = "destinationType",
                propertyValue = "javax.jms.Queue"),
        @ActivationConfigProperty(propertyName = "destination",
                propertyValue = "jms/queue/ExpiryQueue"),
        @ActivationConfigProperty(propertyName = "maxSession",
                propertyValue = "1") })
@Named("expiryQueueReceiver")
public class ExpiryQueueReceiver implements MessageListener {
    private static final org.slf4j.Logger log =
            org.slf4j.LoggerFactory.getLogger(ExpiryQueueReceiver.class);

    @Override
    public void onMessage(Message message) {
        log.warn("JMS message expired: {}", MessageUnwrapper.unwrap(message));
    }
}
