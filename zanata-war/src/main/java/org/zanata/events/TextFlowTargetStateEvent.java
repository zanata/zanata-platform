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

import java.io.Serializable;

import com.google.common.collect.ImmutableList;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.zanata.common.ContentState;

import javax.annotation.Nullable;

/**
 * @author Sean Flanigan <a
 *         href="mailto:sflaniga@redhat.com">sflaniga@redhat.com</a>
 *
 */
@AllArgsConstructor
public final class TextFlowTargetStateEvent {
    @Getter
    private final DocumentLocaleKey key;

    @Getter
    private final Long projectIterationId;

    @Getter
    // this may be null in the case of document uploads
    private final @Nullable Long actorId;

    @Getter
    private final ImmutableList<TextFlowTargetState> states;

    public TextFlowTargetStateEvent(DocumentLocaleKey key,
        Long projectIterationId, Long actorId, TextFlowTargetState state) {
        this(key, projectIterationId, actorId, ImmutableList.of(state));
    }

    @Getter
    @EqualsAndHashCode
    @AllArgsConstructor
    public static final class TextFlowTargetState implements Serializable {
        private final Long textFlowId;
        private final Long textFlowTargetId;
        private final ContentState newState;
        private final ContentState previousState;
    }
}
