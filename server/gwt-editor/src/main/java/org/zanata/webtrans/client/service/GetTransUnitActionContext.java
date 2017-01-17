/*
 * Copyright 2012, Red Hat, Inc. and individual contributors as indicated by the
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

package org.zanata.webtrans.client.service;

import java.util.List;

import com.google.common.base.MoreObjects;
import org.zanata.webtrans.shared.model.ContentStateGroup;
import org.zanata.webtrans.shared.model.DocumentInfo;
import org.zanata.webtrans.shared.model.TransUnitId;
import org.zanata.webtrans.shared.model.ValidationId;
import org.zanata.webtrans.shared.rpc.EditorFilter;

import com.google.common.base.Objects;

/**
 * This class is immutable and all the mutator methods will return a new
 * instance of it. This is so that it can be shared by multiple objects to keep
 * track of states easily.
 *
 * @author Patrick Huang <a
 *         href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
public class GetTransUnitActionContext {
    private DocumentInfo document;
    private int offset = 0;
    // this should be set to UserConfigHolder.getPageSize()
    private int count = 5;
    private boolean filterTranslated;
    private boolean filterFuzzy;
    private boolean filterUntranslated;
    private boolean filterApproved;
    private boolean filterRejected;
    private boolean filterHasError;
    private TransUnitId targetTransUnitId;
    private List<ValidationId> validationIds;
    private EditorFilter editorFilter = EditorFilter.ALL;

    public GetTransUnitActionContext(DocumentInfo document) {
        this.document = document;
    }

    private GetTransUnitActionContext(GetTransUnitActionContext other) {
        document = other.getDocument();
        offset = other.getOffset();
        count = other.getCount();
        filterTranslated = other.isFilterTranslated();
        filterFuzzy = other.isFilterFuzzy();
        filterUntranslated = other.isFilterUntranslated();
        filterApproved = other.isFilterApproved();
        filterRejected = other.isFilterRejected();
        filterHasError = other.isFilterHasError();
        targetTransUnitId = other.getTargetTransUnitId();
        validationIds = other.getValidationIds();
        editorFilter = other.getEditorFilter();
    }

    public DocumentInfo getDocument() {
        return document;
    }

    public GetTransUnitActionContext changeDocument(DocumentInfo document) {
        GetTransUnitActionContext result = new GetTransUnitActionContext(this);
        result.document = document;
        return result;
    }

    public String getFindMessage() {
        return editorFilter.getTextInContent();
    }

    public GetTransUnitActionContext withFindMessage(String findMessage) {
        GetTransUnitActionContext result = new GetTransUnitActionContext(this);
        result.editorFilter = EditorFilter.fromQuery(findMessage);
        return result;
    }

    public boolean isFilterTranslated() {
        return filterTranslated;
    }

    public GetTransUnitActionContext withFilterTranslated(
            boolean filterTranslated) {
        GetTransUnitActionContext result = new GetTransUnitActionContext(this);
        result.filterTranslated = filterTranslated;
        return result;
    }

    public boolean isFilterFuzzy() {
        return filterFuzzy;
    }

    public GetTransUnitActionContext withFilterFuzzy(boolean filterFuzzy) {
        GetTransUnitActionContext result = new GetTransUnitActionContext(this);
        result.filterFuzzy = filterFuzzy;
        return result;
    }

    public boolean isFilterUntranslated() {
        return filterUntranslated;
    }

    public boolean isFilterApproved() {
        return filterApproved;
    }

    public boolean isFilterRejected() {
        return filterRejected;
    }

    public GetTransUnitActionContext withFilterUntranslated(
            boolean filterUntranslated) {
        GetTransUnitActionContext result = new GetTransUnitActionContext(this);
        result.filterUntranslated = filterUntranslated;
        return result;
    }

    public GetTransUnitActionContext withFilterApproved(boolean filterApproved) {
        GetTransUnitActionContext result = new GetTransUnitActionContext(this);
        result.filterApproved = filterApproved;
        return result;
    }

    public GetTransUnitActionContext withFilterRejected(boolean filterRejected) {
        GetTransUnitActionContext result = new GetTransUnitActionContext(this);
        result.filterRejected = filterRejected;
        return result;
    }

    public boolean isFilterHasError() {
        return filterHasError;
    }

    public GetTransUnitActionContext withFilterHasError(boolean filterHasError) {
        GetTransUnitActionContext result = new GetTransUnitActionContext(this);
        result.filterHasError = filterHasError;
        return result;
    }

    public TransUnitId getTargetTransUnitId() {
        return targetTransUnitId;
    }

    public GetTransUnitActionContext withTargetTransUnitId(
            TransUnitId targetTransUnitId) {
        GetTransUnitActionContext result = new GetTransUnitActionContext(this);
        result.targetTransUnitId = targetTransUnitId;
        return result;
    }

    public int getOffset() {
        return offset;
    }

    public GetTransUnitActionContext withOffset(int offset) {
        GetTransUnitActionContext result = new GetTransUnitActionContext(this);
        result.offset = offset;
        return result;
    }

    public int getCount() {
        return count;
    }

    public GetTransUnitActionContext withCount(int count) {
        GetTransUnitActionContext result = new GetTransUnitActionContext(this);
        result.count = count;
        return result;
    }

    public List<ValidationId> getValidationIds() {
        return validationIds;
    }

    public GetTransUnitActionContext withValidationIds(
            List<ValidationId> validationIds) {
        GetTransUnitActionContext result = new GetTransUnitActionContext(this);
        result.validationIds = validationIds;
        return result;
    }

    public EditorFilter getEditorFilter() {
        return editorFilter;
    }

    public GetTransUnitActionContext withEditorFilter(
            EditorFilter editorFilter) {
        GetTransUnitActionContext result = new GetTransUnitActionContext(this);
        result.editorFilter = editorFilter;
        return result;
    }

    @Override
    public String toString() {
        // @formatter:off
        return MoreObjects.toStringHelper(this).
            add("document", document).
            add("offset", offset).
            add("count", count).
            add("filterTranslated", filterTranslated).
            add("filterFuzzy", filterFuzzy).
            add("filterUntranslated", filterUntranslated).
            add("filterApproved", filterApproved).
            add("filterRejected", filterRejected).
            add("filterHasError", filterHasError).
            add("targetTransUnitId", targetTransUnitId).
            add("editorFilter", editorFilter).
            toString();
        // @formatter:on
    }

    /**
     * Detects whether context has changed so that we need to reload translation
     * unit list
     *
     * @param newContext
     *            new context compare to current
     * @return true if we should reload list
     */
    public boolean needReloadList(GetTransUnitActionContext newContext) {
        return needReloadNavigationIndex(newContext)
                || count != newContext.count;
    }

    /**
     * Detects whether context has changed so that we need to reload navigation
     * indexes
     *
     * @param newContext
     *            new context compare to current
     * @return true if we should reload navigation index
     */
    public boolean needReloadNavigationIndex(
            GetTransUnitActionContext newContext) {
        if (this == newContext) {
            return false;
        }

        // @formatter:off
        return filterFuzzy != newContext.filterFuzzy
            || filterTranslated != newContext.filterTranslated
            || filterUntranslated != newContext.filterUntranslated
            || filterApproved != newContext.filterApproved
            || filterRejected != newContext.filterRejected
            || filterHasError != newContext.filterHasError
            || offset != newContext.offset
            || !document.equals(newContext.document)
            || !Objects.equal(editorFilter, newContext.editorFilter);
        // @formatter:on
    }

    public ContentStateGroup getCurrentFilterStates() {
        return filterStatesFromCheckboxStates(getCheckboxStates());
    }

    private ContentStateGroup getCheckboxStates() {
        ContentStateGroup checkboxStates =
                ContentStateGroup.builder().includeNew(filterUntranslated)
                        .includeFuzzy(filterFuzzy)
                        .includeTranslated(filterTranslated)
                        .includeApproved(filterApproved)
                        .includeRejected(filterRejected).build();
        return checkboxStates;
    }

    private static ContentStateGroup filterStatesFromCheckboxStates(
            ContentStateGroup filterStates) {
        if (filterStates.hasNoStates()) {
            filterStates = ContentStateGroup.builder().addAll().build();
        }
        return filterStates;
    }
}
