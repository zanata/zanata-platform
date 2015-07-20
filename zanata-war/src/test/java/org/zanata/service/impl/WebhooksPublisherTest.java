/*
 * Copyright 2015, Red Hat, Inc. and individual contributors as indicated by the
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

import org.junit.Test;
import org.zanata.util.HmacUtil;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
public class WebhooksPublisherTest {

    @Test
    public void test1() {
        final String key = "secret_key";
        final String json = "{data: 'testing', event, 'org.zanata.events.TestEvent'}";
        final String callbackURL = "http://localhost:8080";

        //Generated sha from http://hash.online-convert.com/sha1-generator
        final String generatedSha = "uX68Kw05xIlQhl5PNQbuIRQdeyo=";

        String sha = WebHooksPublisher.signWebhookHeader(json, key, callbackURL);

        assertThat(sha).isEqualTo(generatedSha);
    }

    @Test
    public void testGeneratedShaForHeader() {
        final String json = "{data: 'testing', event, 'test event'}";
        final String key= "secret_key";
        final String callbackURL = "http://localhost:8080/listener";

        String expectedSha = getExpectedShaForHeader(json, key, callbackURL);

        String sha = WebHooksPublisher.signWebhookHeader(json, key, callbackURL);

        assertThat(sha).isEqualTo(expectedSha);
    }

    public String getExpectedShaForHeader(String json, String key,
        String callbackURL) {
        String valueToDigest = json + callbackURL;
        return HmacUtil
            .hmacSha1(key, HmacUtil.hmacSha1(key, valueToDigest));
    }
}
