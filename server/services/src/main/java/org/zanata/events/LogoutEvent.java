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
// NB: avoid using session-scoped beans, because this event is

// fired during session expiry.

/**
 * @author Sean Flanigan
 *         <a href="mailto:sflaniga@redhat.com">sflaniga@redhat.com</a>
 */
public final class LogoutEvent {
    private final String username;
    private final String sessionId;
    private final String personName;
    private final String personEmail;

    @java.beans.ConstructorProperties({ "username", "sessionId", "personName",
            "personEmail" })
    public LogoutEvent(final String username, final String sessionId,
            final String personName, final String personEmail) {
        this.username = username;
        this.sessionId = sessionId;
        this.personName = personName;
        this.personEmail = personEmail;
    }

    public String getUsername() {
        return this.username;
    }

    public String getSessionId() {
        return this.sessionId;
    }

    public String getPersonName() {
        return this.personName;
    }

    public String getPersonEmail() {
        return this.personEmail;
    }

    @Override
    public boolean equals(final Object o) {
        if (o == this)
            return true;
        if (!(o instanceof LogoutEvent))
            return false;
        final LogoutEvent other = (LogoutEvent) o;
        final Object this$username = this.getUsername();
        final Object other$username = other.getUsername();
        if (this$username == null ? other$username != null
                : !this$username.equals(other$username))
            return false;
        final Object this$sessionId = this.getSessionId();
        final Object other$sessionId = other.getSessionId();
        if (this$sessionId == null ? other$sessionId != null
                : !this$sessionId.equals(other$sessionId))
            return false;
        final Object this$personName = this.getPersonName();
        final Object other$personName = other.getPersonName();
        if (this$personName == null ? other$personName != null
                : !this$personName.equals(other$personName))
            return false;
        final Object this$personEmail = this.getPersonEmail();
        final Object other$personEmail = other.getPersonEmail();
        if (this$personEmail == null ? other$personEmail != null
                : !this$personEmail.equals(other$personEmail))
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
        final Object $sessionId = this.getSessionId();
        result = result * PRIME
                + ($sessionId == null ? 43 : $sessionId.hashCode());
        final Object $personName = this.getPersonName();
        result = result * PRIME
                + ($personName == null ? 43 : $personName.hashCode());
        final Object $personEmail = this.getPersonEmail();
        result = result * PRIME
                + ($personEmail == null ? 43 : $personEmail.hashCode());
        return result;
    }

    @Override
    public String toString() {
        return "LogoutEvent(username=" + this.getUsername() + ", sessionId="
                + this.getSessionId() + ", personName=" + this.getPersonName()
                + ", personEmail=" + this.getPersonEmail() + ")";
    }
}
