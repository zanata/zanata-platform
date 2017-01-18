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
package org.zanata.security.openid;

import java.io.Serializable;

/**
 * Contains results of an open Id authentication. It may also include user info
 * returned by the open id provider like email, user name, etc.
 *
 * @author Carlos Munoz <a
 *         href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 */
public class OpenIdAuthenticationResult implements Serializable {
    /**
     * Represents an externally authenticated username (ie a username for the external system).
     */
    private String authenticatedId;

    private String email;

    private String fullName;

    private String username;

    private String returnToUrl;

    public boolean isAuthenticated() {
        return authenticatedId != null;
    }

    public String getAuthenticatedId() {
        return this.authenticatedId;
    }

    public String getEmail() {
        return this.email;
    }

    public String getFullName() {
        return this.fullName;
    }

    public String getUsername() {
        return this.username;
    }

    public String getReturnToUrl() {
        return this.returnToUrl;
    }

    public void setAuthenticatedId(String authenticatedId) {
        this.authenticatedId = authenticatedId;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setReturnToUrl(String returnToUrl) {
        this.returnToUrl = returnToUrl;
    }
}
