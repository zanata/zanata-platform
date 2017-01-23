package org.zanata.webtrans.shared.model;

import java.util.List;
import org.zanata.common.ContentState;
import com.google.common.collect.Lists;
import com.google.gwt.user.client.rpc.IsSerializable;

public class ContentStateGroup implements IsSerializable {
    private boolean hasNew;
    private boolean hasFuzzy;
    private boolean hasTranslated;
    private boolean hasApproved;
    private boolean hasRejected;

    private ContentStateGroup() {
        // This exists to allow GWT to serialize
    }

    private ContentStateGroup(boolean includeNew, boolean includeFuzzy,
            boolean includeTranslated, boolean includeApproved,
            boolean includeRejected) {
        hasNew = includeNew;
        hasFuzzy = includeFuzzy;
        hasTranslated = includeTranslated;
        hasApproved = includeApproved;
        hasRejected = includeRejected;
    }

    /**
     * @return a Builder with all states on by default
     */
    public static Builder builder() {
        return new Builder();
    }

    public boolean hasNew() {
        return hasNew;
    }

    public boolean hasFuzzy() {
        return hasFuzzy;
    }

    public boolean hasTranslated() {
        return hasTranslated;
    }

    public boolean hasApproved() {
        return hasApproved;
    }

    public boolean hasRejected() {
        return hasRejected;
    }

    public boolean hasAllStates() {
        return hasNew && hasFuzzy && hasTranslated && hasApproved
                && hasRejected;
    }

    public boolean hasNoStates() {
        return !(hasNew || hasFuzzy || hasTranslated || hasApproved
                || hasRejected);
    }

    public List<ContentState> asList() {
        List<ContentState> result = Lists.newArrayList();
        if (hasNew) {
            result.add(ContentState.New);
        }
        if (hasFuzzy) {
            result.add(ContentState.NeedReview);
        }
        if (hasTranslated) {
            result.add(ContentState.Translated);
        }
        if (hasApproved) {
            result.add(ContentState.Approved);
        }
        if (hasRejected) {
            result.add(ContentState.Rejected);
        }
        return result;
    }

    public static class Builder {
        private boolean hasNew;
        private boolean hasFuzzy;
        private boolean hasTranslated;
        private boolean hasApproved;
        private boolean hasRejected;

        public Builder() {
            addAll();
        }

        public ContentStateGroup build() {
            return new ContentStateGroup(hasNew, hasFuzzy, hasTranslated,
                    hasApproved, hasRejected);
        }

        public Builder addAll() {
            this.hasNew = true;
            this.hasFuzzy = true;
            this.hasTranslated = true;
            this.hasApproved = true;
            this.hasRejected = true;
            return this;
        }

        public Builder removeAll() {
            this.hasNew = false;
            this.hasFuzzy = false;
            this.hasTranslated = false;
            this.hasApproved = false;
            this.hasRejected = false;
            return this;
        }

        public Builder fromStates(ContentStateGroup states) {
            this.hasNew = states.hasNew;
            this.hasFuzzy = states.hasFuzzy;
            this.hasTranslated = states.hasTranslated;
            this.hasApproved = states.hasApproved;
            this.hasRejected = states.hasRejected;
            return this;
        }

        public Builder includeNew(boolean on) {
            hasNew = on;
            return this;
        }

        public Builder includeFuzzy(boolean on) {
            hasFuzzy = on;
            return this;
        }

        public Builder includeTranslated(boolean on) {
            hasTranslated = on;
            return this;
        }

        public Builder includeApproved(boolean on) {
            hasApproved = on;
            return this;
        }

        public Builder includeRejected(boolean on) {
            hasRejected = on;
            return this;
        }
    }

    @Override
    public String toString() {
        return "ContentStateGroup(hasNew=" + this.hasNew + ", hasFuzzy="
                + this.hasFuzzy + ", hasTranslated=" + this.hasTranslated
                + ", hasApproved=" + this.hasApproved + ", hasRejected="
                + this.hasRejected + ")";
    }
}
