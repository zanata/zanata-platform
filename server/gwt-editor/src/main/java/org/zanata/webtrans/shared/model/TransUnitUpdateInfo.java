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

package org.zanata.webtrans.shared.model;

import org.zanata.common.*;

import com.google.common.base.*;
import com.google.gwt.user.client.rpc.*;

/**
 * Represents information about an attempted update of a {@link TransUnit}.
 *
 * @author David Mason, damason@redhat.com
 *
 */
public class TransUnitUpdateInfo implements IsSerializable {
    private boolean success;
    private boolean targetChanged;
    private DocumentId documentId;
    private TransUnit transUnit;
    private int sourceWordCount;
    private int previousVersionNum;
    private ContentState previousState;
    private String errorMessage;

    // required for GWT rpc serialization
    @SuppressWarnings("unused")
    private TransUnitUpdateInfo() {
    }

    public TransUnitUpdateInfo(boolean success, boolean targetChanged,
            DocumentId documentId, TransUnit transUnit, int sourceWordCount,
            int previousVersionNum, ContentState previousState,
            String errorMessage) {
        this.success = success;
        this.targetChanged = targetChanged;
        this.documentId = documentId;
        this.transUnit = transUnit;
        this.sourceWordCount = sourceWordCount;
        this.previousVersionNum = previousVersionNum;
        this.previousState = previousState;
        this.errorMessage = errorMessage;
    }

    public TransUnitUpdateInfo(boolean success, boolean targetChanged,
            DocumentId documentId, TransUnit transUnit, int sourceWordCount,
            int previousVersionNum, ContentState previousState) {
        this(success, targetChanged, documentId, transUnit, sourceWordCount,
                previousVersionNum, previousState, null);
    }

    public boolean isSuccess() {
        // TODO could do this
        // return transUnit.getVerNum() > previousVersionNum;
        return success;
    }

    public boolean isTargetChanged() {
        return targetChanged;
    }

    public DocumentId getDocumentId() {
        return documentId;
    }

    public TransUnit getTransUnit() {
        return transUnit;
    }

    public int getPreviousVersionNum() {
        return previousVersionNum;
    }

    public ContentState getPreviousState() {
        return previousState;
    }

    public int getSourceWordCount() {
        return sourceWordCount;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    @Override
    public String toString() {
        // @formatter:off
      return MoreObjects.toStringHelper(this).
            add("success", success).
            add("documentId", documentId).
            add("transUnit", transUnit).
            add("sourceWordCount", sourceWordCount).
            add("previousVersionNum", previousVersionNum).
            add("previousState", previousState).
            add("errorMessage", errorMessage).
            toString();
      // @formatter:on
    }
}
