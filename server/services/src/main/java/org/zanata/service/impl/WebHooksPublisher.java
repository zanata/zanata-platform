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
package org.zanata.service.impl;

import java.util.Optional;
import javax.annotation.Nonnull;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.MediaType;
import org.apache.commons.lang.StringUtils;
import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;
import org.zanata.events.WebhookEventType;
import org.zanata.util.HmacUtil;

/**
 * Do http post for webhook event
 *
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
public class WebHooksPublisher {
    private static final org.slf4j.Logger log =
            org.slf4j.LoggerFactory.getLogger(WebHooksPublisher.class);

    public static final String WEBHOOK_HEADER = "X-Zanata-Webhook";

    public static void publish(@Nonnull String callbackURL,
            @Nonnull WebhookEventType event, Optional<String> secretKey) {
        publish(callbackURL, event.getJSON(), MediaType.APPLICATION_JSON_TYPE,
                MediaType.APPLICATION_JSON_TYPE, secretKey);
    }

    protected static void publish(@Nonnull String callbackURL,
            @Nonnull String data, @Nonnull MediaType acceptType,
            @Nonnull MediaType mediaType, Optional<String> secretKey) {
        try {
            ResteasyClient client = new ResteasyClientBuilder().build();
            ResteasyWebTarget target = client.target(callbackURL);
            Invocation.Builder postBuilder =
                    target.request().accept(acceptType);
            if (secretKey.isPresent()
                    && StringUtils.isNotBlank(secretKey.get())) {
                String sha =
                        signWebhookHeader(data, secretKey.get(), callbackURL);
                postBuilder.header(WEBHOOK_HEADER, sha);
            }
            log.debug("firing async webhook: {}:{}", callbackURL, data);
            postBuilder.async().post(Entity.entity(data, mediaType));
        } catch (Exception e) {
            log.error("Error on webhooks post {}, {}", callbackURL, e);
        }
    }

    protected static String signWebhookHeader(String data, String key,
            String callbackURL) {
        String valueToDigest = data + callbackURL;
        try {
            return HmacUtil.hmacSha1(key,
                    HmacUtil.hmacSha1(key, valueToDigest));
        } catch (IllegalArgumentException e) {
            log.error("Unable to generate hmac sha1 for {} {}", key,
                    valueToDigest);
            throw new IllegalArgumentException(e);
        }
    }
}
