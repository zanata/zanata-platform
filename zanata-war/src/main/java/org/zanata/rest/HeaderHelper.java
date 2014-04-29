package org.zanata.rest;

import org.jboss.resteasy.spi.HttpRequest;

/**
 * @author Patrick Huang
 *         <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
public final class HeaderHelper {
    private HeaderHelper() {
    }

    public static final String X_AUTH_TOKEN_HEADER = "X-Auth-Token";
    public static final String X_AUTH_USER_HEADER = "X-Auth-User";

    protected static String getApiKey(HttpRequest request) {
        return request.getHttpHeaders().getRequestHeaders()
                .getFirst(X_AUTH_TOKEN_HEADER);
    }

    protected static String getUserName(HttpRequest request) {
        return request.getHttpHeaders().getRequestHeaders()
                .getFirst(X_AUTH_USER_HEADER);
    }
}
