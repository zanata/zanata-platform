/*
 * Copyright 2012, Red Hat, Inc. and individual contributors
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
package org.zanata.webtrans.client.presenter;

import java.util.Comparator;

import org.zanata.webtrans.client.ui.SearchResultsDocumentTable;
import org.zanata.webtrans.shared.model.TransUnit;
import org.zanata.webtrans.shared.model.TransUnitUpdateInfo;
import org.zanata.webtrans.shared.model.TransUnitUpdatePreview;

/**
 * Data model used by {@link SearchResultsDocumentTable}.
 *
 * @author David Mason, <a
 *         href="mailto:damason@redhat.com">damason@redhat.com</a>
 *
 */
public class TransUnitReplaceInfo {
    private static Comparator<TransUnitReplaceInfo> comparator;

    private ReplacementState replacementState;
    private PreviewState previewState;
    private Long docId;
    private TransUnit tu;
    private TransUnitUpdatePreview preview;
    private TransUnitUpdateInfo replaceInfo;

    public TransUnitReplaceInfo(Long containingDocId, TransUnit tu) {
        this.docId = containingDocId;
        this.tu = tu;
        preview = null;
        replaceInfo = null;
        replacementState = ReplacementState.NotReplaced;
        previewState = PreviewState.NotFetched;
    }

    public TransUnit getTransUnit() {
        return tu;
    }

    public void setTransUnit(TransUnit tu) {
        this.tu = tu;
    }

    public TransUnitUpdatePreview getPreview() {
        return preview;
    }

    public void setPreview(TransUnitUpdatePreview preview) {
        this.preview = preview;
    }

    public TransUnitUpdateInfo getReplaceInfo() {
        return replaceInfo;
    }

    public void setReplaceInfo(TransUnitUpdateInfo replaceInfo) {
        this.replaceInfo = replaceInfo;
    }

    public ReplacementState getReplaceState() {
        return replacementState;
    }

    public void setReplaceState(ReplacementState state) {
        this.replacementState = state;
    }

    public PreviewState getPreviewState() {
        return previewState;
    }

    public void setPreviewState(PreviewState previewState) {
        this.previewState = previewState;
    }

    public Long getDocId() {
        return docId;
    }

    public static Comparator<TransUnitReplaceInfo> getRowComparator() {
        if (comparator == null) {
            comparator = (o1, o2) -> {
                if (o1 == o2) {
                    return 0;
                }
                if (o1 != null) {
                    return (o2 != null ? Integer.valueOf(
                            o1.getTransUnit().getRowIndex()).compareTo(
                            o2.getTransUnit().getRowIndex()) : 1);
                }
                return -1;
            };
        }
        return comparator;
    }
}
