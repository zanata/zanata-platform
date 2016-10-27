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

package org.zanata.limits;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

/**
 * Token used for rate limiter queue.
 *
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
@EqualsAndHashCode
@ToString
public class RateLimiterToken {

    @Getter
    private final String value;

    @Getter
    private final TYPE type;

    public static enum TYPE {
        USERNAME, API_KEY, IP_ADDRESS, TOKEN;
    }

    public RateLimiterToken(TYPE type, String value) {
        this.type = type;
        this.value = value;
    }

    /**
     * Generate key from username
     */
    public static RateLimiterToken fromUsername(String username) {
        return new RateLimiterToken(TYPE.USERNAME, username);
    }

    /**
     * Generate key from api key
     */
    public static RateLimiterToken fromApiKey(String apiKey) {
        return new RateLimiterToken(TYPE.API_KEY, apiKey);
    }

    /**
     * Generate key from either api key, OAuth authorizationCode or accessToken
     * @param token the actual token value
     */
    public static RateLimiterToken fromToken(String token) {
        return new RateLimiterToken(TYPE.TOKEN, token);
    }

    /**
     * Generate key from ip address
     */
    public static RateLimiterToken fromIPAddress(String ipAddress) {
        return new RateLimiterToken(TYPE.IP_ADDRESS, ipAddress);
    }
}
