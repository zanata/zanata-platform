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
import java.util.Map;
import javax.annotation.Nonnull;
import org.zanata.common.LocaleId;
import org.zanata.model.ITextFlow;
import org.zanata.model.ITextFlowTarget;
import com.google.common.collect.ImmutableList;

/**
 * @author Sean Flanigan
 *         <a href="mailto:sflaniga@redhat.com">sflaniga@redhat.com</a>
 */
class SimpleTextFlow implements ITextFlow {
    private final String qualifiedId;
    private final Map<LocaleId, ITextFlowTarget> targets;
    @Nonnull
    private final LocaleId locale;
    private List<String> contents;

    public SimpleTextFlow(String qualifiedId,
            Map<LocaleId, ITextFlowTarget> targets, @Nonnull LocaleId locale,
            List<String> contents) {
        this.qualifiedId = qualifiedId;
        this.targets = targets;
        this.locale = locale;
        this.contents = contents;
    }

    public SimpleTextFlow(String qualifiedId, @Nonnull LocaleId locale,
            @Nonnull String content0, Map<LocaleId, ITextFlowTarget> targets) {
        this(qualifiedId, targets, locale, ImmutableList.of(content0));
    }

    public SimpleTextFlow(String qualifiedId,
            Map<LocaleId, ITextFlowTarget> targets, @Nonnull LocaleId locale,
            @Nonnull String... contents) {
        this(qualifiedId, targets, locale, ImmutableList.copyOf(contents));
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

    @Override
    public ITextFlowTarget getTargetContents(LocaleId localeId) {
        return targets.get(localeId);
    }

    @Override
    public Iterable<ITextFlowTarget> getAllTargetContents() {
        return ImmutableList.copyOf(getTargets().values());
    }

    public String getQualifiedId() {
        return this.qualifiedId;
    }

    public Map<LocaleId, ITextFlowTarget> getTargets() {
        return this.targets;
    }

    @Nonnull
    public LocaleId getLocale() {
        return this.locale;
    }

    public List<String> getContents() {
        return this.contents;
    }

    @Override
    public boolean equals(final Object o) {
        if (o == this)
            return true;
        if (!(o instanceof SimpleTextFlow))
            return false;
        final SimpleTextFlow other = (SimpleTextFlow) o;
        if (!other.canEqual((Object) this))
            return false;
        final Object this$qualifiedId = this.getQualifiedId();
        final Object other$qualifiedId = other.getQualifiedId();
        if (this$qualifiedId == null ? other$qualifiedId != null
                : !this$qualifiedId.equals(other$qualifiedId))
            return false;
        final Object this$targets = this.getTargets();
        final Object other$targets = other.getTargets();
        if (this$targets == null ? other$targets != null
                : !this$targets.equals(other$targets))
            return false;
        final Object this$locale = this.getLocale();
        final Object other$locale = other.getLocale();
        if (this$locale == null ? other$locale != null
                : !this$locale.equals(other$locale))
            return false;
        final Object this$contents = this.getContents();
        final Object other$contents = other.getContents();
        if (this$contents == null ? other$contents != null
                : !this$contents.equals(other$contents))
            return false;
        return true;
    }

    protected boolean canEqual(final Object other) {
        return other instanceof SimpleTextFlow;
    }

    @Override
    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final Object $qualifiedId = this.getQualifiedId();
        result = result * PRIME
                + ($qualifiedId == null ? 43 : $qualifiedId.hashCode());
        final Object $targets = this.getTargets();
        result = result * PRIME + ($targets == null ? 43 : $targets.hashCode());
        final Object $locale = this.getLocale();
        result = result * PRIME + ($locale == null ? 43 : $locale.hashCode());
        final Object $contents = this.getContents();
        result = result * PRIME
                + ($contents == null ? 43 : $contents.hashCode());
        return result;
    }

    @Override
    public String toString() {
        return "SimpleTextFlow(qualifiedId=" + this.getQualifiedId()
                + ", targets=" + this.getTargets() + ", locale="
                + this.getLocale() + ", contents=" + this.getContents() + ")";
    }
}
