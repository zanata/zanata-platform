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

import lombok.Data;

/**
 * @author Sean Flanigan <a
 *         href="mailto:sflaniga@redhat.com">sflaniga@redhat.com</a>
 *
 */
@Data
class SimpleTextFlow implements ITextFlow {
    private final String qualifiedId;
    private final Map<LocaleId, ITextFlowTarget> targets;
    private final @Nonnull
    LocaleId locale;
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
        return ImmutableList.<ITextFlowTarget> copyOf(getTargets().values());
    }
}
