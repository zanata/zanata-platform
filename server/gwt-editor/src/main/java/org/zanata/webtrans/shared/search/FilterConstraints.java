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
package org.zanata.webtrans.shared.search;

//TODO May want to add document(someDocument) to these constraints
//so that only one search method is needed on the interface.
import com.google.common.base.MoreObjects;
import org.joda.time.DateTime;
import org.zanata.webtrans.shared.model.ContentStateGroup;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;

/**
 * Specifies a set of constraints to be applied by a filter.
 *
 * @author David Mason, damason@redhat.com
 */
public class FilterConstraints {
    private String searchString;
    private boolean isCaseSensitive;
    private boolean searchInSource;
    private boolean searchInTarget;
    private ContentStateGroup includedStates;
    private String resId;
    private DateTime changedBefore;
    private DateTime changedAfter;
    private String lastModifiedByUser;
    private String sourceComment;
    private String transComment;
    private String msgContext;

    private FilterConstraints(Builder builder) {
        searchInSource = builder.searchInSource;
        searchInTarget = builder.searchInTarget;
        searchString = builder.searchString;
        isCaseSensitive = builder.caseSensitive;
        includedStates = builder.states.build();
        resId = builder.resId;
        changedBefore = builder.changedBefore;
        changedAfter = builder.changedAfter;
        if (changedAfter != null && changedBefore != null) {
            Preconditions.checkArgument(changedBefore.isAfter(changedAfter),
                    "change before date [%s] must be after change after date [%s]",
                    changedBefore, changedAfter);
        }
        lastModifiedByUser = builder.lastModifiedByUser;
        sourceComment = builder.sourceComment;
        transComment = builder.transComment;
        msgContext = builder.msgContext;
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    public String toString() {
        // @formatter:off
        return MoreObjects.toStringHelper(this).add("searchString", searchString).add("isCaseSensitive", isCaseSensitive).add("searchInSource", searchInSource).add("searchInTarget", searchInTarget).add("includedStates", includedStates).add("resId", resId).add("changedBefore", changedBefore).add("changedAfter", changedAfter).add("lastModifiedByUser", lastModifiedByUser).add("sourceComment", sourceComment).add("transComment", transComment).add("msgContext", msgContext).toString();
        // @formatter:on
    }

    public static class Builder {
        private String searchString;
        private boolean caseSensitive;
        private boolean searchInSource;
        private boolean searchInTarget;
        private ContentStateGroup.Builder states;
        private String resId;
        private DateTime changedBefore;
        private DateTime changedAfter;
        private String lastModifiedByUser;
        private String sourceComment;
        private String transComment;
        private String msgContext;

        public Builder() {
            states = ContentStateGroup.builder();
            setKeepAll();
        }

        public FilterConstraints build() {
            return new FilterConstraints(this);
        }

        public Builder keepAll() {
            setKeepAll();
            return this;
        }

        private void setKeepAll() {
            searchString = "";
            caseSensitive = false;
            searchInSource = true;
            searchInTarget = true;
            states.addAll();
            resId = "";
            changedAfter = null;
            changedBefore = null;
            lastModifiedByUser = "";
            sourceComment = "";
            transComment = "";
            msgContext = "";
        }

        public Builder keepNone() {
            searchString = "";
            caseSensitive = false;
            searchInSource = false;
            searchInTarget = false;
            states.removeAll();
            resId = "";
            changedAfter = null;
            changedBefore = null;
            lastModifiedByUser = "";
            sourceComment = "";
            transComment = "";
            msgContext = "";
            return this;
        }

        public Builder filterBy(String searchString) {
            this.searchString = searchString;
            return this;
        }

        public Builder caseSensitive(boolean caseSensitive) {
            this.caseSensitive = caseSensitive;
            return this;
        }

        public Builder checkInSource(boolean check) {
            searchInSource = check;
            return this;
        }

        public Builder checkInTarget(boolean check) {
            searchInTarget = check;
            return this;
        }

        public Builder includeStates(ContentStateGroup states) {
            this.states.fromStates(states);
            return this;
        }

        public Builder includeNew() {
            states.includeNew(true);
            return this;
        }

        public Builder excludeNew() {
            states.includeNew(false);
            return this;
        }

        public Builder includeFuzzy() {
            states.includeFuzzy(true);
            return this;
        }

        public Builder excludeFuzzy() {
            states.includeFuzzy(false);
            return this;
        }

        public Builder includeTranslated() {
            states.includeTranslated(true);
            return this;
        }

        public Builder excludeTranslated() {
            states.includeTranslated(false);
            return this;
        }

        public Builder includeApproved() {
            states.includeApproved(true);
            return this;
        }

        public Builder excludeApproved() {
            states.includeApproved(false);
            return this;
        }

        public Builder includeRejected() {
            states.includeRejected(true);
            return this;
        }

        public Builder excludeRejected() {
            states.includeRejected(false);
            return this;
        }

        public Builder resourceIdIs(String resId) {
            this.resId = resId;
            return this;
        }

        public Builder lastModifiedBy(String username) {
            this.lastModifiedByUser = username;
            return this;
        }

        public Builder targetChangedBefore(DateTime date) {
            this.changedBefore = date;
            return this;
        }

        public Builder targetChangedAfter(DateTime date) {
            this.changedAfter = date;
            return this;
        }

        public Builder clearChangedDate() {
            this.changedAfter = null;
            this.changedBefore = null;
            return this;
        }

        public Builder msgContext(String msgContext) {
            this.msgContext = msgContext;
            return this;
        }

        public Builder sourceCommentContains(String content) {
            this.sourceComment = content;
            return this;
        }

        public Builder targetCommentContains(String content) {
            this.transComment = content;
            return this;
        }
    }

    public String getSearchString() {
        return this.searchString;
    }

    public boolean isCaseSensitive() {
        return this.isCaseSensitive;
    }

    public boolean isSearchInSource() {
        return this.searchInSource;
    }

    public boolean isSearchInTarget() {
        return this.searchInTarget;
    }

    public ContentStateGroup getIncludedStates() {
        return this.includedStates;
    }

    public String getResId() {
        return this.resId;
    }

    public DateTime getChangedBefore() {
        return this.changedBefore;
    }

    public DateTime getChangedAfter() {
        return this.changedAfter;
    }

    public String getLastModifiedByUser() {
        return this.lastModifiedByUser;
    }

    public String getSourceComment() {
        return this.sourceComment;
    }

    public String getTransComment() {
        return this.transComment;
    }

    public String getMsgContext() {
        return this.msgContext;
    }
}
