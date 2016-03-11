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
package org.zanata.security;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;

import javax.enterprise.event.Observes;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zanata.events.NotLoggedInEvent;
import org.zanata.servlet.annotations.ContextPath;
import org.zanata.util.UrlUtil;

/**
 * This bean is used store a url from the query string for use with redirects.
 *
 * Add {@code continue=URL} to the query string, and have seam capture the
 * parameter by adding child element
 * <code>&lt;param name="continue" value="#{userRedirect.encodedUrl}" /></code>
 * to the login page's {@code <page>} element in pages.xml
 *
 * TODO Use {@link org.jboss.seam.faces.Redirect} instead of this class (by
 * extension or otherwise).
 */
@Named("userRedirect")
// TODO verify that SESSION scope will not persist this too long
@javax.enterprise.context.SessionScoped

public class UserRedirectBean implements Serializable {
    private static final Logger log =
            LoggerFactory.getLogger(UserRedirectBean.class);
    private static final String HOME_PATH = "/";
    private static final String REGISTER_PATH = "/register";
    private static final String ERROR_PATH = "/error/";
    private static final String LOGIN_PATH = "/sign_in";

    /**
    *
    */
    private static final long serialVersionUID = 1L;
    private final static String ENCODING = "UTF-8";

    @Inject @ContextPath
    private String contextPath;
    @Inject
    private UrlUtil urlUtil;

    private String url;

    /**
     * Sets the redirect url to a context local url.
     *
     * @param url
     *            The context local url to redirect to.
     * @see UserRedirectBean#setUrl(String)
     */
    public void setLocalUrl(String url) {
        setUrl(contextPath + url);
    }

    /**
     * Stores the url for later use after applying any required modifications.
     *
     * @param url
     *            a non-encoded url
     * @see {@link #applyRedirectModifications(String)}
     */
    public void setUrl(String url) {
        this.url = url;
    }

    public String getUrl() {
        return url;
    }

    public String getEncodedUrl() {
        if (url == null)
            return null;
        try {
            return URLEncoder.encode(url, ENCODING);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Decodes an encoded url, then stores with modifications.
     *
     * @param encodedUrl
     *            an encoded url to store
     * @see {@link #setUrl(String)}
     */
    public void setEncodedUrl(String encodedUrl) {
        if (encodedUrl == null || encodedUrl.isEmpty()) {
            this.url = encodedUrl;
            return;
        }

        try {
            setUrl(URLDecoder.decode(encodedUrl, ENCODING));
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean isRedirect() {
        return url != null && !url.isEmpty();
    }

    public boolean isRedirectToHome() {
        return isRedirectTo(HOME_PATH);
    }

    public boolean isRedirectToError() {
        return isRedirectTo(ERROR_PATH);
    }

    public boolean isRedirectToRegister() {
        return isRedirectTo(REGISTER_PATH);
    }

    public boolean isRedirectToLoginPage() {
        return isRedirectTo(LOGIN_PATH);
    }

    // provided user is logged in, they should be redirect to dashboard
    public boolean shouldRedirectToDashboard() {
        return !isRedirect() || isRedirectToHome() || isRedirectToRegister()
                || isRedirectToError() || isRedirectToLoginPage();
    }

    /**
     * Removes context path and any query params from a local url
     * @param localUrl
     * @return
     */
    private String getPath(String localUrl) {
        assert localUrl.startsWith(contextPath);
        String localPath;
        // strip off any query params
        int qsIndex = localUrl.indexOf('?');
        if (qsIndex >= 0) {
            localPath = localUrl.substring(0, qsIndex);
        } else {
            localPath = localUrl;
        }
        String path = localPath.substring(contextPath.length());
        return path;
    }

    private boolean isRedirectTo(String pathWithoutContext) {
        if (isRedirect()) {
            String redirectingUrl = getUrl();
            String redirectPath = getPath(redirectingUrl);
            if (redirectPath.equals(pathWithoutContext)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Capture the view id, request parameters from the current request and
     * squirrel them away so we can return here later.
     *
     * @see UserRedirectBean#returnToCapturedView
     */
    public void captureCurrentView(@Observes NotLoggedInEvent event) {
        FacesContext context = FacesContext.getCurrentInstance();

        // If this isn't a faces request then just return
        if (context == null) {
            return;
        }

        HttpServletRequest request = (HttpServletRequest) context.getExternalContext().getRequest();
        setUrl(urlUtil.getLocalUrl(request));
    }

}
