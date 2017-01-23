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

/**
 * Token used for rate limiter queue.
 *
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
public class RateLimiterToken {
    private final String value;
    private final TYPE type;

    public static enum TYPE {
        USERNAME,
        API_KEY,
        IP_ADDRESS,
        TOKEN;

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
     *
     * @param token
     *            the actual token value
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

    @Override
    public boolean equals(final Object o) {
        if (o == this)
            return true;
        if (!(o instanceof RateLimiterToken))
            return false;
        final RateLimiterToken other = (RateLimiterToken) o;
        if (!other.canEqual((Object) this))
            return false;
        final Object this$value = this.getValue();
        final Object other$value = other.getValue();
        if (this$value == null ? other$value != null
                : !this$value.equals(other$value))
            return false;
        final Object this$type = this.getType();
        final Object other$type = other.getType();
        if (this$type == null ? other$type != null
                : !this$type.equals(other$type))
            return false;
        return true;
    }

    protected boolean canEqual(final Object other) {
        return other instanceof RateLimiterToken;
    }

    @Override
    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final Object $value = this.getValue();
        result = result * PRIME + ($value == null ? 43 : $value.hashCode());
        final Object $type = this.getType();
        result = result * PRIME + ($type == null ? 43 : $type.hashCode());
        return result;
    }

    @Override
    public String toString() {
        return "RateLimiterToken(value=" + this.getValue() + ", type="
                + this.getType() + ")";
    }

    public String getValue() {
        return this.value;
    }

    public TYPE getType() {
        return this.type;
    }
}
