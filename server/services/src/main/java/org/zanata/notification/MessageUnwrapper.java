package org.zanata.notification;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import javax.jms.Message;
import javax.jms.ObjectMessage;
import javax.jms.TextMessage;
import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.collect.ImmutableMap;

/**
 * Unwraps a JMS message. Extract payload for ObjectMessage and TextMessage and
 * all properties.
 *
 * @author Patrick Huang
 *         <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
class MessageUnwrapper {
    private final Serializable payload;
    private final Map<String, ?> properties;

    static MessageUnwrapper unwrap(final Message message) {
        Serializable payload = extractPayload(message);
        Map<String, ?> properties = extractProperties(message);
        // we may want to add more interested properties. e.g. reply address
        return new MessageUnwrapper(payload, properties);
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
            Object propertyValue = getPropertyValue(message, propName);
            builder.put(propName, propertyValue == null ? "" : propertyValue);
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
        return MoreObjects.toStringHelper(this).add("payload", payload)
                .add("properties", properties).toString();
    }

    private MessageUnwrapper(final Serializable payload,
            final Map<String, ?> properties) {
        this.payload = payload;
        this.properties = properties;
    }
}
