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

import lombok.Value;

/**
 * Event for post update of Request status.
 * To be integrate with notification.
 *
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
@Value
public class RequestUpdatedEvent {
    public static final String EVENT_NAME =
        "org.zanata.events.RequestUpdatedEvent";

    /**
     * Id for {@link org.zanata.model.Request}
     */
    long id;

    /**
     * Id for request subclass. e.g {@link org.zanata.model.LanguageRequest}
     */
    long requestId;

    /**
     * Id for actor for this request. HAccount id
     */
    long actorId;

    /**
     * New state
     */
    RequestState state;
}
