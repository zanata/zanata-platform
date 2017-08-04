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
package org.zanata.events;

import org.zanata.model.type.RequestState;

/**
 * Event for post update of Request status. To be integrate with notification.
 *
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
public final class RequestUpdatedEvent {
    public static final String EVENT_NAME =
            "org.zanata.events.RequestUpdatedEvent";

    /**
     * Id for {@link org.zanata.model.Request}
     */
    private final long id;

    /**
     * Id for request subclass. e.g {@link org.zanata.model.LanguageRequest}
     */
    private final long requestId;

    /**
     * Id for actor for this request. HAccount id
     */
    private final long actorId;

    /**
     * New state
     */
    private final RequestState state;

    @java.beans.ConstructorProperties({ "id", "requestId", "actorId", "state" })
    public RequestUpdatedEvent(final long id, final long requestId,
            final long actorId, final RequestState state) {
        this.id = id;
        this.requestId = requestId;
        this.actorId = actorId;
        this.state = state;
    }

    /**
     * Id for {@link org.zanata.model.Request}
     */
    public long getId() {
        return this.id;
    }

    /**
     * Id for request subclass. e.g {@link org.zanata.model.LanguageRequest}
     */
    public long getRequestId() {
        return this.requestId;
    }

    /**
     * Id for actor for this request. HAccount id
     */
    public long getActorId() {
        return this.actorId;
    }

    /**
     * New state
     */
    public RequestState getState() {
        return this.state;
    }

    @Override
    public boolean equals(final Object o) {
        if (o == this)
            return true;
        if (!(o instanceof RequestUpdatedEvent))
            return false;
        final RequestUpdatedEvent other = (RequestUpdatedEvent) o;
        if (this.getId() != other.getId())
            return false;
        if (this.getRequestId() != other.getRequestId())
            return false;
        if (this.getActorId() != other.getActorId())
            return false;
        final Object this$state = this.getState();
        final Object other$state = other.getState();
        if (this$state == null ? other$state != null
                : !this$state.equals(other$state))
            return false;
        return true;
    }

    @Override
    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final long $id = this.getId();
        result = result * PRIME + (int) ($id >>> 32 ^ $id);
        final long $requestId = this.getRequestId();
        result = result * PRIME + (int) ($requestId >>> 32 ^ $requestId);
        final long $actorId = this.getActorId();
        result = result * PRIME + (int) ($actorId >>> 32 ^ $actorId);
        final Object $state = this.getState();
        result = result * PRIME + ($state == null ? 43 : $state.hashCode());
        return result;
    }

    @Override
    public String toString() {
        return "RequestUpdatedEvent(id=" + this.getId() + ", requestId="
                + this.getRequestId() + ", actorId=" + this.getActorId()
                + ", state=" + this.getState() + ")";
    }
}
