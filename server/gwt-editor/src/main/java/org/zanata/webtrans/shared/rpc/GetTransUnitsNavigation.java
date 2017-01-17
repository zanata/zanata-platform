/*
 * Copyright 2010, Red Hat, Inc. and individual contributors as indicated by the
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
package org.zanata.webtrans.shared.rpc;

import com.google.common.base.MoreObjects;
import org.zanata.webtrans.shared.model.ContentStateGroup;
import org.zanata.webtrans.shared.model.DocumentId;
import org.zanata.webtrans.shared.search.FilterConstraints;

import com.google.common.base.Objects;

public class GetTransUnitsNavigation {
    private DocumentId documentId;
    private ContentStateGroup activeStates;
    private EditorFilter editorFilter;
    private FilterConstraints constraints;

    @SuppressWarnings("unused")
    private GetTransUnitsNavigation() {
    }

    public GetTransUnitsNavigation(DocumentId documentId,
            ContentStateGroup activeStates, EditorFilter editorFilter,
            FilterConstraints constraints) {
        this.documentId = documentId;
        this.activeStates = activeStates;
        this.editorFilter = editorFilter;
        this.constraints = constraints;
    }

    public DocumentId getDocumentId() {
        return documentId;
    }

    public FilterConstraints getConstraints() {
        return constraints;
    }

    @Override
    public String toString() {
        // @formatter:off
      return MoreObjects.toStringHelper(this).
            add("documentId", documentId).
            add("activeStates", activeStates).
            add("editorFilter", editorFilter).
            toString();
      // @formatter:on
    }
}
