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
package org.zanata.rest.service;

import java.util.Arrays;
import java.util.List;
import org.zanata.common.ContentState;
import org.zanata.common.LocaleId;
import org.zanata.model.ITextFlowTarget;
import com.google.common.collect.ImmutableList;

/**
 * @author Sean Flanigan
 *         <a href="mailto:sflaniga@redhat.com">sflaniga@redhat.com</a>
 */
class SimpleTextFlowTarget implements ITextFlowTarget {
    private final ContentState state;
    private List<String> contents;
    private final LocaleId localeId;

    public SimpleTextFlowTarget(LocaleId localeId, ContentState state,
            List<String> contents) {
        this.localeId = localeId;
        this.state = state;
        this.contents = contents;
    }

    public SimpleTextFlowTarget(LocaleId localeId, ContentState state,
            String... contents) {
        this(localeId, state, ImmutableList.copyOf(contents));
    }
    // Lombok won't generate this because of the other setContents method

    @Override
    public void setContents(List<String> contents) {
        this.contents = contents;
    }

    @Override
    public void setContents(String... contents) {
        setContents(Arrays.asList(contents));
    }

    public ContentState getState() {
        return this.state;
    }

    public List<String> getContents() {
        return this.contents;
    }

    public LocaleId getLocaleId() {
        return this.localeId;
    }

    @Override
    public boolean equals(final Object o) {
        if (o == this)
            return true;
        if (!(o instanceof SimpleTextFlowTarget))
            return false;
        final SimpleTextFlowTarget other = (SimpleTextFlowTarget) o;
        if (!other.canEqual((Object) this))
            return false;
        final Object this$state = this.getState();
        final Object other$state = other.getState();
        if (this$state == null ? other$state != null
                : !this$state.equals(other$state))
            return false;
        final Object this$contents = this.getContents();
        final Object other$contents = other.getContents();
        if (this$contents == null ? other$contents != null
                : !this$contents.equals(other$contents))
            return false;
        final Object this$localeId = this.getLocaleId();
        final Object other$localeId = other.getLocaleId();
        if (this$localeId == null ? other$localeId != null
                : !this$localeId.equals(other$localeId))
            return false;
        return true;
    }

    protected boolean canEqual(final Object other) {
        return other instanceof SimpleTextFlowTarget;
    }

    @Override
    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final Object $state = this.getState();
        result = result * PRIME + ($state == null ? 43 : $state.hashCode());
        final Object $contents = this.getContents();
        result = result * PRIME
                + ($contents == null ? 43 : $contents.hashCode());
        final Object $localeId = this.getLocaleId();
        result = result * PRIME
                + ($localeId == null ? 43 : $localeId.hashCode());
        return result;
    }

    @Override
    public String toString() {
        return "SimpleTextFlowTarget(state=" + this.getState() + ", contents="
                + this.getContents() + ", localeId=" + this.getLocaleId() + ")";
    }
}
