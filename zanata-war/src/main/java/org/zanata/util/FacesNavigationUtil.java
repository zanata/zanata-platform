/*
 * Copyright 2015, Red Hat, Inc. and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.zanata.util;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Collections;
import java.util.Map;
import javax.faces.application.NavigationHandler;
import javax.faces.component.UIViewRoot;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zanata.servlet.HttpRequestAndSessionHolder;

/**
 * @author Patrick Huang <a
 *         href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
public class FacesNavigationUtil {
    private static final Logger log =
            LoggerFactory.getLogger(FacesNavigationUtil.class);

    public static void handlePageNavigation(String fromAction, String outcome) {
        FacesContext context = FacesContext.getCurrentInstance();
        NavigationHandler navigationHandler =
                context.getApplication().getNavigationHandler();
        navigationHandler.handleNavigation(context, fromAction, outcome);
    }

    public static String getCurrentViewId() {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        if (facesContext != null) {
            UIViewRoot viewRoot = facesContext.getViewRoot();
            if (viewRoot != null) {
                return viewRoot.getViewId();
            }
        }
        return null;
    }

    public static String encodeScheme(FacesContext context, String url) {
        String scheme = HttpRequestAndSessionHolder.getScheme();
        if (scheme != null) {
            String requestUrl = getRequestUrl(context);
            if (requestUrl != null) {
                try {
                    URL serverUrl = new URL(requestUrl);

                    StringBuilder sb = new StringBuilder();
                    sb.append(scheme);
                    sb.append("://");
                    sb.append(serverUrl.getHost());

                    int serverPort =
                            HttpRequestAndSessionHolder.getServerPort();
                    if ("http".equals(scheme) && serverPort != 80) {
                        sb.append(":");
                        sb.append(serverPort);
                    } else if ("https".equals(scheme) && serverPort != 80) {
                        sb.append(":");
                        sb.append(serverPort);
                    } else if (serverUrl.getPort() != -1) {
                        sb.append(":");
                        sb.append(serverUrl.getPort());
                    }

                    if (!url.startsWith("/")) sb.append("/");

                    sb.append(url);

                    url = sb.toString();
                } catch (MalformedURLException ex) {
                    throw new RuntimeException(ex);
                }
            }
        }
        return url;
    }

    private static String getRequestUrl(FacesContext facesContext) {
        Object request = facesContext.getExternalContext().getRequest();
        if (request instanceof HttpServletRequest) {
            return ((HttpServletRequest) request).getRequestURL().toString();
        } else {
            return null;
        }
    }

    /**
     * Performs an HTTP redirect to a given url
     * @param context The FacesContext to use.
     * @param url The url where to redirect
     * @throws IOException If there is a problem performing the redirection.
     * @see ExternalContext#redirect(String)
     */
    public static void redirect(FacesContext context, String url)
            throws IOException {
        url = encodeScheme(context, url);
        if (log.isDebugEnabled()) {
            log.debug("redirecting to: " + url);
        }
        ExternalContext externalContext = context.getExternalContext();
//        controllingRedirect = true;
        try {
//            Contexts.getEventContext().set(REDIRECT_FROM_MANAGER, "");
            externalContext.redirect(externalContext.encodeActionURL(url));
        } catch (IOException | IllegalStateException e) {
            log.warn("error redirecting to url:" + url, e);
            throw e;
        } /*finally {
//            Contexts.getEventContext().remove(REDIRECT_FROM_MANAGER);
//            controllingRedirect = false;
        }*/
        context.responseComplete();
    }

    public static void redirectToExternal(String url) throws IOException {
        FacesContext context = FacesContext.getCurrentInstance();
        ExternalContext externalContext = context.getExternalContext();
        try {
            externalContext.redirect(externalContext.encodeActionURL(url));
        } catch (IOException | IllegalStateException e) {
            log.warn("error redirecting to url:" + url, e);
            throw e;
        }
        context.responseComplete();
    }

    public static void redirect(String viewId, Map<String, Object> parameters)
            throws IOException {
        if (viewId == null) {
            return;
        }
        FacesContext context = FacesContext.getCurrentInstance();
        String url = context.getApplication().getViewHandler()
                .getRedirectURL(context, viewId, Collections.emptyMap(), false);
        if (parameters != null) {
            url = encodeParameters(url, parameters);
        }


        redirect(context, url);
    }

    /**
     * Add the parameters to a URL
     */
    private static String encodeParameters(String url,
            Map<String, Object> parameters) {
        if (parameters.isEmpty()) {
            return url;
        }

        StringBuilder builder = new StringBuilder(url);
        for (Map.Entry<String, Object> param : parameters.entrySet()) {
            String parameterName = param.getKey();
            if (!containsParameter(url, parameterName)) {
                Object parameterValue = param.getValue();
                if (parameterValue instanceof Iterable) {
                    for (Object value : (Iterable) parameterValue) {
                        builder.append('&')
                                .append(parameterName)
                                .append('=');
                        if (value != null) {
                            builder.append(encode(value));
                        }
                    }
                } else {
                    builder.append('&')
                            .append(parameterName)
                            .append('=');
                    if (parameterValue != null) {
                        builder.append(encode(parameterValue));
                    }
                }
            }
        }
        if (url.indexOf('?') < 0) {
            builder.setCharAt(url.length(), '?');
        }
        return builder.toString();
    }

    private static boolean containsParameter(String url, String parameterName) {
        return url.indexOf('?' + parameterName + '=') > 0 ||
                url.indexOf('&' + parameterName + '=') > 0;
    }

    private static String encode(Object value) {
        try {
            return URLEncoder.encode(String.valueOf(value), "UTF-8");
        } catch (UnsupportedEncodingException iee) {
            throw new RuntimeException(iee);
        }
    }
}
