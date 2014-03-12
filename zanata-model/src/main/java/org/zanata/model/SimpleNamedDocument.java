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

package org.zanata.model;

import java.util.Iterator;
import java.util.List;

import javax.annotation.Nonnull;

import lombok.Data;

import org.zanata.common.LocaleId;

import com.google.common.collect.ImmutableList;

/**
 * @author Sean Flanigan <a
 *         href="mailto:sflaniga@redhat.com">sflaniga@redhat.com</a>
 *
 */
@Data
public class SimpleNamedDocument implements DocumentWithId {
    private final LocaleId sourceLocaleId;
    private final String qualifiedDocId;
    private final List<ITextFlow> sourceContentsList;

    public SimpleNamedDocument(LocaleId sourceLocaleId, String qualifiedDocId,
            List<ITextFlow> sourceContentsList) {
        this.sourceLocaleId = sourceLocaleId;
        this.qualifiedDocId = qualifiedDocId;
        this.sourceContentsList = sourceContentsList;
    }

    public SimpleNamedDocument(LocaleId sourceLocaleId, String qualifiedDocId,
            @Nonnull ITextFlow... sourceContentsList) {
        this.sourceLocaleId = sourceLocaleId;
        this.qualifiedDocId = qualifiedDocId;
        this.sourceContentsList = ImmutableList.copyOf(sourceContentsList);
    }

    @Override
    public Iterator<ITextFlow> iterator() {
        return sourceContentsList.iterator();
    }

}
