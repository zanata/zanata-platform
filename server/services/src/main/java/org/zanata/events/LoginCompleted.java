/*
 * Copyright 2014, Red Hat, Inc. and individual contributors
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
package org.zanata.events;

import org.zanata.security.AuthenticationType;

/**
 * Event used to signal a successful login using the authentication manager. It
 * is a complement to the events in Seam's Identity class.
 *
 * @author Sean Flanigan
 *         <a href="mailto:sflaniga@redhat.com">sflaniga@redhat.com</a>
 */
public final class LoginCompleted {
    private final AuthenticationType authType;

    @java.beans.ConstructorProperties({ "authType" })
    public LoginCompleted(final AuthenticationType authType) {
        this.authType = authType;
    }

    public AuthenticationType getAuthType() {
        return this.authType;
    }

    @Override
    public boolean equals(final Object o) {
        if (o == this)
            return true;
        if (!(o instanceof LoginCompleted))
            return false;
        final LoginCompleted other = (LoginCompleted) o;
        final Object this$authType = this.getAuthType();
        final Object other$authType = other.getAuthType();
        if (this$authType == null ? other$authType != null
                : !this$authType.equals(other$authType))
            return false;
        return true;
    }

    @Override
    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final Object $authType = this.getAuthType();
        result = result * PRIME
                + ($authType == null ? 43 : $authType.hashCode());
        return result;
    }

    @Override
    public String toString() {
        return "LoginCompleted(authType=" + this.getAuthType() + ")";
    }
    // String username;
}
