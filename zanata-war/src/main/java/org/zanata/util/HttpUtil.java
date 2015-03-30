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
package org.zanata.util;

import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.HttpMethod;

import org.apache.commons.lang.StringUtils;
import org.jboss.resteasy.spi.HttpRequest;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;

/**
 * Utility class for HTTP related methods.
 *
 * @author Patrick Huang
 *         <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
public final class HttpUtil {

    private final static List<String> HTTP_REQUEST_READ_METHODS = Lists.newArrayList(
        HttpMethod.GET, HttpMethod.HEAD, HttpMethod.OPTIONS);

    public static final String X_AUTH_TOKEN_HEADER = "X-Auth-Token";
    public static final String X_AUTH_USER_HEADER = "X-Auth-User";

    /**
     * This should be set by admin.
     * Example header names might be "X-Forwarded-For", "Proxy-Client-IP",
     * "WL-Proxy-Client-IP", "HTTP_CLIENT_IP", "HTTP_X_FORWARDED_FOR"
     */
    public static String PROXY_HEADER = System
            .getProperty("ZANATA_PROXY_HEADER");

    public static String getApiKey(HttpRequest request) {
        return request.getHttpHeaders().getRequestHeaders()
                .getFirst(X_AUTH_TOKEN_HEADER);
    }

    @VisibleForTesting
    static void refreshProxyHeader() {
        PROXY_HEADER = System.getProperty("ZANATA_PROXY_HEADER");
    }

    public static String getUsername(HttpRequest request) {
        return request.getHttpHeaders().getRequestHeaders()
                .getFirst(X_AUTH_USER_HEADER);
    }

    /**
     * Return client ip address according to HttpServletRequest.
     *
     * This will also check for the possibility of client behind proxy
     * before returning default remote address in request.
     *
     * NOTE: Not all proxy server include client ip information in http header
     * and different proxy MIGHT use different http header for such information.
     * Default remote address in request will be returned if client information
     * is not found in header.
     *
     * see http://stackoverflow.com/questions/4678797/how-do-i-get-the-remote-address-of-a-client-in-servlet
     * @param request
     */
    public static String getClientIp(HttpServletRequest request) {
        String ip;

        if(StringUtils.isEmpty(PROXY_HEADER)) {
            return request.getRemoteAddr();
        }

        // PROXY_HEADER can be list of ip address
        String[] ipList =
                StringUtils.split(request.getHeader(PROXY_HEADER), ",");

        if(ipList.length == 1) {
            return ipList[0];
        }

        //return last ip address from list if found
        ip = ipList[ipList.length-1];
        if(!isIpUnknown(ip)) {
            return ip;
        }

        return request.getRemoteAddr();
    }

    private static boolean isIpUnknown(String ip) {
        return StringUtils.isEmpty(ip) || StringUtils.equalsIgnoreCase(ip,
                "unknown") || StringUtils.equalsIgnoreCase(ip, "localhost") ||
                StringUtils.equals(ip, "127.0.0.1");
    }

    public static boolean isReadMethod(String httpMethod) {
        for(String readMethod: HTTP_REQUEST_READ_METHODS) {
            if(readMethod.equalsIgnoreCase(httpMethod)) {
                return true;
            }
        }
        return false;
    }
}
