/*
 * Copyright 2010, Red Hat, Inc. and individual contributors as indicated by the
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
package org.zanata.log4j;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.naming.NamingException;

import org.apache.log4j.helpers.LogLog;
import org.apache.log4j.spi.ErrorCode;
import org.apache.log4j.spi.LoggingEvent;
import org.apache.log4j.spi.TriggeringEventEvaluator;
import org.zanata.util.ServiceLocator;
import it.openutils.log4j.AlternateSMTPAppender;

/**
 * Extension of the {@link AlternateSMTPAppender} class that allows Zanata to
 * use email configuration.
 *
 * @author Carlos Munoz <a
 *         href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 */
public class ZanataSMTPAppender extends AlternateSMTPAppender {
    public ZanataSMTPAppender() {
        this.setEvaluator(new LevelBasedEventEvaluator());
    }

    @Override
    public void activateOptions() {
        try {
            // for some reason this has to use the FQ jndi address
            Session session = ServiceLocator.instance()
                    .getJndiComponent("java:jboss/mail/Default",
                            Session.class);
            // session.setDebug(true);
            msg = new MimeMessage(session);

            if (getFrom() != null) {
                msg.setFrom(getAddress(getFrom()));
            } else {
                msg.setFrom();
            }

            if (getTo() == null) {
                throw new MessagingException();
            }

            msg.setRecipients(Message.RecipientType.TO, parseAddress(getTo()));
        } catch (MessagingException | NamingException e) {
            LogLog.error("Could not activate SMTPAppender options.", e);
        }
    }

    InternetAddress getAddress(String addressStr) {
        try {
            return new InternetAddress(addressStr);
        } catch (AddressException e) {
            errorHandler.error("Could not parse address [" + addressStr + "].",
                    e, ErrorCode.ADDRESS_PARSE_FAILURE);
            return null;
        }
    }

    InternetAddress[] parseAddress(String addressStr) {
        try {
            return InternetAddress.parse(addressStr, true);
        } catch (AddressException | NullPointerException e) {
            errorHandler.error("Could not parse address [" + addressStr + "].",
                    e, ErrorCode.ADDRESS_PARSE_FAILURE);
            return null;
        }
    }

    /**
     * Evaluates events based on a predetermined event level threshold.
     */
    private class LevelBasedEventEvaluator implements TriggeringEventEvaluator {
        @Override
        public boolean isTriggeringEvent(LoggingEvent event) {
            return event.getLevel().isGreaterOrEqual(getThreshold());
        }
    }
}
