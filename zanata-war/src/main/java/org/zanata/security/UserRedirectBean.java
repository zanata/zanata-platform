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

import javax.inject.Named;
import org.jboss.seam.web.ServletContexts;

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
    private static final String HOME_URL = "/";
    private static final String REGISTER_URL = "/register";
    private static final String ERROR_URL = "/error";

    /**
    *
    */
    private static final long serialVersionUID = 1L;
    private final static String ENCODING = "UTF-8";
    private String url;

    /**
     * Modifies the redirect url to apply extra rules about redirects that
     * cannot be expressed in pages.xml.
     *
     * Currently replaces the error url with the home url to prevent redirect to
     * error page after signing in from the error page.
     *
     * @param originalUrl
     *            the url of the page redirected from
     * @return the adjusted url if any adjustment is required, originalUrl
     *         otherwise
     */
    private String applyRedirectModifications(String originalUrl) {
        if (originalUrl == null) {
            return originalUrl;
        }

        String newUrl, queryString;

        int qsIndex = originalUrl.indexOf('?');
        if (qsIndex < 0) {
            newUrl = originalUrl;
            queryString = "";
        } else {
            newUrl = originalUrl.substring(0, qsIndex);
            queryString = originalUrl.substring(qsIndex);
        }

        if (newUrl.endsWith(ERROR_URL)) {
            newUrl = newUrl.substring(0, newUrl.length() - ERROR_URL.length());
            newUrl = newUrl.concat(HOME_URL);
            return newUrl.concat(queryString);
        } else {
            return originalUrl;
        }

    }

    /**
     * Sets the redirect url to a context local url.
     *
     * @param url
     *            The context local url to redirect to.
     * @see UserRedirectBean#setUrl(String)
     */
    public void setLocalUrl(String url) {
        String ctxPath =
                ServletContexts.instance().getRequest().getContextPath();
        setUrl(ctxPath + url);
    }

    /**
     * Stores the url for later use after applying any required modifications.
     *
     * @param url
     *            a non-encoded url
     * @see {@link #applyRedirectModifications(String)}
     */
    public void setUrl(String url) {
        this.url = applyRedirectModifications(url);
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
        return isRedirectTo(HOME_URL);
    }

    public boolean isRedirectToRegister() {
        return isRedirectTo(REGISTER_URL);
    }

    // provided user is logged in, they should be redirect to dashboard
    public boolean shouldRedirectToDashboard() {
        return isRedirectToHome() || isRedirectToRegister();
    }

    private boolean isRedirectTo(String url) {
        if (isRedirect()) {
            String redirectingUrl = getUrl();
            int qsIndex = redirectingUrl.indexOf('?');
            if (qsIndex > 0) {
                redirectingUrl = redirectingUrl.substring(0, qsIndex);
            }

            if (redirectingUrl.endsWith(url)) {
                return true;
            }

        }
        return false;
    }
}
