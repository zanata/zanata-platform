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
package org.zanata.util;

import java.io.IOException;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;

import javax.enterprise.context.RequestScoped;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang.StringUtils;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.deltaspike.core.spi.scope.window.WindowContext;
import org.apache.http.client.utils.URIBuilder;
import org.zanata.common.LocaleId;
import org.zanata.servlet.annotations.ContextPath;
import com.google.common.base.Throwables;
import org.zanata.servlet.annotations.ServerPath;

/**
 * Get the URL for the current page in URL encoded format for use in the query
 * string
 *
 * @author David Mason, damason@redhat.com
 */

@Named("urlUtil")
@RequestScoped
@Slf4j
public class UrlUtil implements Serializable {
    private static final long serialVersionUID = 1L;
    private final static String ENCODING = "UTF-8";

    @Inject @ServerPath
    private String serverPath;

    @Inject
    @ContextPath
    private String contextPath;

    @Inject
    private WindowContext windowContext;

    /**
     * Get the local url part, including context path, for the given page
     * request.
     *
     * Current implementation only works for forwarded requests
     *
     * @param request
     *            the current request
     * @return local part of url from original request
     */
    public String getLocalUrl(HttpServletRequest request) {
        String url, queryString;
        if (request.getAttribute("javax.servlet.forward.request_uri") != null) {
            url =
                    (String) request
                            .getAttribute("javax.servlet.forward.context_path");
            url +=
                    (String) request
                            .getAttribute("javax.servlet.forward.servlet_path");
            queryString =
                    (String) request
                            .getAttribute("javax.servlet.forward.query_string");
        } else {
            url = request.getRequestURI();
            queryString = request.getQueryString();
            log.warn("encountered non-rewritten url {} with query string {}",
                    url, queryString);
        }

        if (queryString != null && queryString.length() > 0) {
            url += "?" + queryString;
        }
        return url;
    }

    /**
     * Get the local url part, including context path, for the given page
     * request, encoded for use in query string.
     *
     * Current implementation only works for forwarded requests
     *
     * @param request
     *            the current request
     * @return local part of url from original request, url encoded
     */
    public String getEncodedLocalUrl(HttpServletRequest request) {
        String url = getLocalUrl(request);
        try {
            return URLEncoder.encode(url, ENCODING);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    public String sourceFilesViewUrl(String projectSlug, String versionSlug) {
        return versionUrl(projectSlug, versionSlug) + "#documents";
    }

    public String projectUrl(String projectSlug) {
        return contextPath + "/project/view/" + projectSlug;
    }

    public String createNewVersionUrl(String projectSlug) {
        return contextPath + "/project/add_iteration.xhtml?projectSlug="
                + projectSlug;
    }

    public String versionUrl(String projectSlug, String versionSlug) {
        return contextPath + "/iteration/view/" + projectSlug + "/"
                + versionSlug;
    }

    public String editorDocumentListUrl(String projectSlug, String versionSlug,
            LocaleId targetLocaleId, LocaleId sourceLocaleId, boolean fullPath) {
        String prefix = fullPath ? serverPath : contextPath;

        return prefix + "/webtrans/translate?project=" + projectSlug
                + "&iteration=" + versionSlug + "&localeId=" + targetLocaleId
                + "&locale=" + sourceLocaleId;
    }

    public String editorDocumentUrl(String projectSlug, String versionSlug,
            LocaleId targetLocaleId, LocaleId sourceLocaleId, String docId) {
        return editorDocumentListUrl(projectSlug, versionSlug, targetLocaleId,
                sourceLocaleId, false) + "#view:doc;doc:" + docId;
    }

    public String fullEditorDocumentUrl(String projectSlug, String versionSlug,
        LocaleId targetLocaleId, LocaleId sourceLocaleId, String docId) {
        return editorDocumentListUrl(projectSlug, versionSlug, targetLocaleId,
                sourceLocaleId, true) + "#view:doc;doc:" + docId;
    }

    public String editorTransUnitUrl(String projectSlug, String versionSlug,
            LocaleId targetLocaleId, LocaleId sourceLocaleId, String docId,
            Long tuId) {
        return editorDocumentUrl(projectSlug, versionSlug, targetLocaleId,
                sourceLocaleId, docId) + ";textflow:" + tuId;
    }

    public String dashboardUrl() {
        return contextPath + "/dashboard/";
    }

    /**
     * UrlEncoder replace whitespace with '+'. Need manual replacement of '+' to
     * '%20'
     *
     * @param var
     */
    public static String encodeString(String var) {
        try {
            return URLEncoder.encode(var, ENCODING).replace("+", "%20");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    public static String decodeString(String var) {
        try {
            return URLDecoder.decode(var, ENCODING);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean isValidUrl(String url) {
        if (StringUtils.isEmpty(url)) {
            return false;
        }
        try {
            new URL(url);
            return true;
        } catch (MalformedURLException e) {
            return false;
        }
    }

    public void redirectTo(String url) {
        try {
            // to fix https://zanata.atlassian.net/browse/ZNTA-887
            String windowId = windowContext.getCurrentWindowId();
            String urlWithWindowId;
            if (windowId == null) {
                urlWithWindowId = url;
            } else {
                URI uri = new URIBuilder(url).addParameter("dswid", windowId).build();
                urlWithWindowId = uri.toString();
            }
            FacesContext.getCurrentInstance().getExternalContext().redirect(urlWithWindowId);
        } catch (Exception e) {
            log.error("failed to redirect to {}", url, e);
            throw Throwables.propagate(e);
        }
    }

    public String languageHome() {
        return contextPath + "/language/list";
    }

    public String genericErrorPage() {
        return contextPath + "/error";
    }

    public String signInPage() {
        return contextPath + "/account/sign_in";
    }

    public String home() {
        return contextPath + "/";
    }
}
