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
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import javax.jms.TextMessage;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.jboss.seam.annotations.Name;

import com.google.common.base.Objects;
import com.google.common.collect.ImmutableMap;

/**
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
                propertyValue = "jms/queue/DLQ"
        ),
        @ActivationConfigProperty(
                propertyName = "maxSession",
                propertyValue = "1")
})
@Name("deadLetterQueueReceiver")
@Slf4j
public class DeadLetterQueueReceiver implements MessageListener {

    @Override
    public void onMessage(Message message) {
        log.error("dead message: {}", DeadMessageUnwrapper.unwrap(message));
    }

    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    static class DeadMessageUnwrapper {
        private final Serializable payload;
        private final Map<String, ?> properties;

        static DeadMessageUnwrapper unwrap(final Message message) {

            Serializable payload = extractPayload(message);
            Map<String, ?> properties = extractProperties(message);
            // we may want to add more interested properties. e.g. reply address
            return new DeadMessageUnwrapper(payload, properties);
        }

        private static Map<String, ?> extractProperties(final Message message) {
            List<String> propNames =
                    tryDoOrNullOnException(new Callable<List<String>>() {
                        @Override
                        public List<String> call() throws Exception {
                            return Collections.list(message.getPropertyNames());
                        }
                    });

            ImmutableMap.Builder<String, ? super Object> builder =
                    ImmutableMap.builder();
            for (String propName : propNames) {
                builder.put(propName, getPropertyValue(message, propName));
            }
            return builder.build();
        }

        private static Object getPropertyValue(final Message message,
                final String propertyName) {
            return tryDoOrNullOnException(new Callable<Object>() {
                @Override
                public Object call() throws Exception {
                    return message.getObjectProperty(propertyName);
                }
            });
        }

        /**
         * try to extract message payload by message type.
         *
         * @return message payload
         */
        private static Serializable extractPayload(final Message message) {
            // note: this is NOT a complete list of message types
            if (message instanceof ObjectMessage) {
                return tryDoOrNullOnException(new Callable<Serializable>() {
                    @Override
                    public Serializable call() throws Exception {
                        return ((ObjectMessage) message).getObject();
                    }
                });
            } else if (message instanceof TextMessage) {
                return tryDoOrNullOnException(new Callable<Serializable>() {
                    @Override
                    public Serializable call() throws Exception {
                        return ((TextMessage) message).getText();
                    }
                });
            } else {
                // generally this is not going to give us much information
                return message.toString();
            }
        }

        static <T> T tryDoOrNullOnException(Callable<T> callable) {
            try {
                return callable.call();
            } catch (Exception e) {
                return null;
            }
        }

        @Override
        public String toString() {
            return Objects.toStringHelper(this)
                    .add("payload", payload)
                    .add("properties", properties)
                    .toString();
        }
    }
}
