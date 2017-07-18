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

import org.zanata.common.LocaleId;

/**
 * @author Sean Flanigan
 *         <a href="mailto:sflaniga@redhat.com">sflaniga@redhat.com</a>
 */
public final class JoinedLanguageTeam {
    private final String username;
    private final LocaleId localeId;

    @java.beans.ConstructorProperties({ "username", "localeId" })
    public JoinedLanguageTeam(final String username, final LocaleId localeId) {
        this.username = username;
        this.localeId = localeId;
    }

    public String getUsername() {
        return this.username;
    }

    public LocaleId getLocaleId() {
        return this.localeId;
    }

    @Override
    public boolean equals(final Object o) {
        if (o == this)
            return true;
        if (!(o instanceof JoinedLanguageTeam))
            return false;
        final JoinedLanguageTeam other = (JoinedLanguageTeam) o;
        final Object this$username = this.getUsername();
        final Object other$username = other.getUsername();
        if (this$username == null ? other$username != null
                : !this$username.equals(other$username))
            return false;
        final Object this$localeId = this.getLocaleId();
        final Object other$localeId = other.getLocaleId();
        if (this$localeId == null ? other$localeId != null
                : !this$localeId.equals(other$localeId))
            return false;
        return true;
    }

    @Override
    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final Object $username = this.getUsername();
        result = result * PRIME
                + ($username == null ? 43 : $username.hashCode());
        final Object $localeId = this.getLocaleId();
        result = result * PRIME
                + ($localeId == null ? 43 : $localeId.hashCode());
        return result;
    }

    @Override
    public String toString() {
        return "JoinedLanguageTeam(username=" + this.getUsername()
                + ", localeId=" + this.getLocaleId() + ")";
    }
}
