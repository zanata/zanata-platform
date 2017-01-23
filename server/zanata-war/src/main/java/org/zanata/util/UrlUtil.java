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
import com.google.common.annotations.VisibleForTesting;
import org.apache.commons.lang.StringUtils;
import javax.inject.Inject;
import javax.inject.Named;
import org.apache.deltaspike.core.spi.scope.window.WindowContext;
import org.apache.http.client.utils.URIBuilder;
import org.zanata.common.LocaleId;
import org.zanata.rest.service.GlossaryService;
import org.zanata.servlet.annotations.ContextPath;
import com.google.common.base.Throwables;
import org.zanata.servlet.annotations.ServerPath;

/**
 * Get the URL for the current page in URL encoded format for use in the query
 * string
 *
 * @author David Mason, damason@redhat.com
 */
@RequestScoped
public class UrlUtil implements Serializable {
    private static final org.slf4j.Logger log =
            org.slf4j.LoggerFactory.getLogger(UrlUtil.class);

    private static final long serialVersionUID = 1L;
    private static final String ENCODING = "UTF-8";
    @Inject
    @ServerPath
    private String serverPath;
    @Inject
    @ContextPath
    private String contextPath;
    @Inject
    private WindowContext windowContext;
    @Inject
    @Named("dswidQuery")
    private String dswidQuery;
    @Inject
    @Named("dswidParam")
    private String dswidParam;

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
        String url;
        String queryString;
        if (request.getAttribute("javax.servlet.forward.request_uri") != null) {
            url = (String) request
                    .getAttribute("javax.servlet.forward.context_path");
            url += (String) request
                    .getAttribute("javax.servlet.forward.servlet_path");
            queryString = (String) request
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

    /**
     * Get source files url with dswid parameter
     */
    public String sourceFilesViewUrl(String projectSlug, String versionSlug) {
        return versionUrl(projectSlug, versionSlug, false) + "/documents"
                + dswidQuery;
    }

    /**
     * Get project url with dswid parameter
     */
    public String projectUrl(String projectSlug) {
        return contextPath + "/project/view/" + projectSlug + dswidQuery;
    }

    /**
     * Get add-version url with dswid parameter
     */
    public String createNewVersionUrl(String projectSlug) {
        return contextPath + "/project/add_iteration.xhtml?projectSlug="
                + projectSlug + dswidParam;
    }

    /**
     * Get version url with dswid parameter
     */
    public String versionUrl(String projectSlug, String versionSlug) {
        return versionUrl(projectSlug, versionSlug, true);
    }

    /**
     * Get version url with or without dswid parameter
     */
    private String versionUrl(String projectSlug, String versionSlug,
            boolean addDswid) {
        return contextPath + "/iteration/view/" + projectSlug + "/"
                + versionSlug + (addDswid ? dswidQuery : "");
    }

    /**
     * Get editor url for document with dswid parameter, without or without full
     * server path
     */
    public String editorDocumentListUrl(String projectSlug, String versionSlug,
            LocaleId targetLocaleId, LocaleId sourceLocaleId,
            boolean fullPath) {
        String prefix = fullPath ? serverPath : contextPath;
        return prefix + "/webtrans/translate?project=" + projectSlug
                + "&iteration=" + versionSlug + "&localeId=" + targetLocaleId
                + "&locale=" + sourceLocaleId + dswidParam;
    }

    /**
     * Get editor url for document with dswid parameter but without full server
     * path
     */
    public String editorDocumentUrl(String projectSlug, String versionSlug,
            LocaleId targetLocaleId, LocaleId sourceLocaleId, String docId) {
        return editorDocumentListUrl(projectSlug, versionSlug, targetLocaleId,
                sourceLocaleId, false) + "#view:doc;doc:" + docId;
    }

    /**
     * Get editor url for document, with dswid parameter and full server path
     */
    public String fullEditorDocumentUrl(String projectSlug, String versionSlug,
            LocaleId targetLocaleId, LocaleId sourceLocaleId, String docId) {
        return editorDocumentListUrl(projectSlug, versionSlug, targetLocaleId,
                sourceLocaleId, true) + "#view:doc;doc:" + docId;
    }

    /**
     * Get editor url for textflow, with dswid parameter
     */
    public String editorTransUnitUrl(String projectSlug, String versionSlug,
            LocaleId targetLocaleId, LocaleId sourceLocaleId, String docId,
            Long tuId) {
        return editorDocumentUrl(projectSlug, versionSlug, targetLocaleId,
                sourceLocaleId, docId) + ";textflow:" + tuId;
    }

    /**
     * Get url with dswid parameter
     */
    public String dashboardUrl() {
        return contextPath + "/dashboard/" + dswidQuery;
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

    /**
     * Redirect to a Zanata url, adding dswid parameter if missing. Do not use
     * for external URLs!
     */
    public void redirectToInternal(String url) {
        try {
            String urlWithWindowId = addWindowId(url);
            FacesContext.getCurrentInstance().getExternalContext()
                    .redirect(urlWithWindowId);
        } catch (IOException e) {
            throw Throwables.propagate(e);
        }
    }

    public String addWindowId(String url) {
        try {
            // to fix https://zanata.atlassian.net/browse/ZNTA-887
            String windowId = windowContext.getCurrentWindowId();
            String urlWithWindowId;
            if (windowId == null) {
                urlWithWindowId = url;
            } else {
                URI uri = new URIBuilder(url).setParameter("dswid", windowId)
                        .build();
                urlWithWindowId = uri.toString();
            }
            return urlWithWindowId;
        } catch (URISyntaxException e) {
            throw Throwables.propagate(e);
        }
    }

    /**
     * Get languages url with dswid parameter
     */
    public String languageHome() {
        return contextPath + "/language/list" + dswidQuery;
    }

    /**
     * Get error url with dswid parameter
     */
    public String genericErrorPage() {
        return contextPath + "/error" + dswidQuery;
    }

    /**
     * Get missing entity page url with dswid parameter
     */
    public String missingEntityPage() {
        return contextPath + "/error/missing_entity" + dswidQuery;
    }

    /**
     * Get view expired url with dswid parameter
     */
    public String viewExpiredErrorPage() {
        return contextPath + "/error/viewexpiredexception" + dswidQuery;
    }

    /**
     * Get sign-in url with dswid parameter
     */
    public String signInPage() {
        return contextPath + "/account/sign_in" + dswidQuery;
    }

    public String resetPasswordPage(String resetPasswordKey) {
        return contextPath + "/account/password_reset/" + resetPasswordKey
                + dswidQuery;
    }

    /**
     * Get home url with dswid parameter
     */
    public String home() {
        return contextPath + "/" + dswidQuery;
    }

    public String createUserPage() {
        return contextPath + "/account/create_user" + dswidQuery;
    }

    public String inactiveAccountPage() {
        return contextPath + "/account/inactive" + dswidQuery;
    }

    /**
     * Get glossary url with dswid parameter
     */
    public String glossaryUrl(String qualifiedName, String filter,
            LocaleId localeId) {
        String url = contextPath;
        if (GlossaryService.isProjectGlossary(qualifiedName)) {
            String projectSlug = GlossaryService.getProjectSlug(qualifiedName);
            url = url + "/glossary/project/" + projectSlug;
        } else {
            url = url + "/glossary";
        }
        boolean hasFilter = StringUtils.isNotBlank(filter);
        if (hasFilter) {
            url += "?filter=" + encodeString(filter);
        }
        if (localeId != null) {
            String prefix = hasFilter ? "&" : "?";
            url += prefix + "locale=" + localeId;
        }
        return url;
    }
}
