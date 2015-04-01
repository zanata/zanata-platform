/*
 * Copyright 2013, Red Hat, Inc. and individual contributors
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

import javax.annotation.Nullable;

import lombok.Value;
import org.zanata.common.ContentState;
import org.zanata.common.LocaleId;

/**
 * @author Sean Flanigan <a
 *         href="mailto:sflaniga@redhat.com">sflaniga@redhat.com</a>
 *
 */
@Value
public final class TextFlowTargetStateEvent {
    public static final String EVENT_NAME =
            "org.zanata.events.TextFlowTargetStateEvent";
    // this may be null in the case of document uploads
    private final @Nullable
    Long actorId;
    private final Long projectIterationId;
    private final Long documentId;
    private final Long textFlowId;
    private final LocaleId localeId;
    private final Long textFlowTargetId;
    private final ContentState newState;
    private final ContentState previousState;
}
