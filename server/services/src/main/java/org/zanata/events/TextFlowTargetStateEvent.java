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
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import org.zanata.common.ContentState;
import javax.annotation.Nullable;

/**
 * @author Sean Flanigan
 *         <a href="mailto:sflaniga@redhat.com">sflaniga@redhat.com</a>
 */
public final class TextFlowTargetStateEvent {
    private final DocumentLocaleKey key;
    private final Long projectIterationId;
    // this may be null in the case of document uploads
    @Nullable
    private final Long actorId;
    private final ImmutableList<TextFlowTargetStateChange> states;

    public TextFlowTargetStateEvent(DocumentLocaleKey key,
            Long projectIterationId, Long actorId,
            ImmutableList<TextFlowTargetStateChange> states) {
        this.key = key;
        this.projectIterationId = projectIterationId;
        this.actorId = actorId;
        this.states = states;
        Preconditions.checkArgument(!states.isEmpty(), "states is empty");
    }

    public TextFlowTargetStateEvent(DocumentLocaleKey key,
            Long projectIterationId, Long actorId,
            TextFlowTargetStateChange state) {
        this(key, projectIterationId, actorId, ImmutableList.of(state));
    }

    public static final class TextFlowTargetStateChange
            implements Serializable {
        private final Long textFlowId;
        private final Long textFlowTargetId;
        private final ContentState newState;
        private final ContentState previousState;

        public Long getTextFlowId() {
            return this.textFlowId;
        }

        public Long getTextFlowTargetId() {
            return this.textFlowTargetId;
        }

        public ContentState getNewState() {
            return this.newState;
        }

        public ContentState getPreviousState() {
            return this.previousState;
        }

        @Override
        public boolean equals(final Object o) {
            if (o == this)
                return true;
            if (!(o instanceof TextFlowTargetStateEvent.TextFlowTargetStateChange))
                return false;
            final TextFlowTargetStateChange other =
                    (TextFlowTargetStateChange) o;
            final Object this$textFlowId = this.getTextFlowId();
            final Object other$textFlowId = other.getTextFlowId();
            if (this$textFlowId == null ? other$textFlowId != null
                    : !this$textFlowId.equals(other$textFlowId))
                return false;
            final Object this$textFlowTargetId = this.getTextFlowTargetId();
            final Object other$textFlowTargetId = other.getTextFlowTargetId();
            if (this$textFlowTargetId == null ? other$textFlowTargetId != null
                    : !this$textFlowTargetId.equals(other$textFlowTargetId))
                return false;
            final Object this$newState = this.getNewState();
            final Object other$newState = other.getNewState();
            if (this$newState == null ? other$newState != null
                    : !this$newState.equals(other$newState))
                return false;
            final Object this$previousState = this.getPreviousState();
            final Object other$previousState = other.getPreviousState();
            if (this$previousState == null ? other$previousState != null
                    : !this$previousState.equals(other$previousState))
                return false;
            return true;
        }

        @Override
        public int hashCode() {
            final int PRIME = 59;
            int result = 1;
            final Object $textFlowId = this.getTextFlowId();
            result = result * PRIME
                    + ($textFlowId == null ? 43 : $textFlowId.hashCode());
            final Object $textFlowTargetId = this.getTextFlowTargetId();
            result = result * PRIME + ($textFlowTargetId == null ? 43
                    : $textFlowTargetId.hashCode());
            final Object $newState = this.getNewState();
            result = result * PRIME
                    + ($newState == null ? 43 : $newState.hashCode());
            final Object $previousState = this.getPreviousState();
            result = result * PRIME
                    + ($previousState == null ? 43 : $previousState.hashCode());
            return result;
        }

        @java.beans.ConstructorProperties({ "textFlowId", "textFlowTargetId",
                "newState", "previousState" })
        public TextFlowTargetStateChange(final Long textFlowId,
                final Long textFlowTargetId, final ContentState newState,
                final ContentState previousState) {
            this.textFlowId = textFlowId;
            this.textFlowTargetId = textFlowTargetId;
            this.newState = newState;
            this.previousState = previousState;
        }
    }

    public DocumentLocaleKey getKey() {
        return this.key;
    }

    public Long getProjectIterationId() {
        return this.projectIterationId;
    }

    @Nullable
    public Long getActorId() {
        return this.actorId;
    }

    public ImmutableList<TextFlowTargetStateChange> getStates() {
        return this.states;
    }
}
