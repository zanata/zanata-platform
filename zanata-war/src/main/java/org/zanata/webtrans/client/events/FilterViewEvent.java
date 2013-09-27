package org.zanata.webtrans.client.events;

import org.zanata.webtrans.client.service.GetTransUnitActionContext;
import org.zanata.webtrans.client.service.NavigationService;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.gwt.event.shared.GwtEvent;

public class FilterViewEvent extends GwtEvent<FilterViewEventHandler> implements
        NavigationService.UpdateContextCommand {
    /**
     * Handler type.
     */
    private static Type<FilterViewEventHandler> TYPE;
    public static final FilterViewEvent DEFAULT = new FilterViewEvent(false,
            false, false, false, false, false, false);

    /**
     * Gets the type associated with this event.
     *
     * @return returns the handler type
     */
    public static Type<FilterViewEventHandler> getType() {
        if (TYPE == null) {
            TYPE = new Type<FilterViewEventHandler>();
        }
        return TYPE;
    }

    private boolean filterTranslated, filterFuzzy, filterUntranslated,
            filterApproved, filterRejected, filterHasError;
    private boolean cancelFilter;

    public FilterViewEvent(boolean filterTranslated, boolean filterFuzzy,
            boolean filterUntranslated, boolean filterApproved,
            boolean filterRejected, boolean filterHasError, boolean cancelFilter) {
        this.filterTranslated = filterTranslated;
        this.filterFuzzy = filterFuzzy;
        this.filterUntranslated = filterUntranslated;
        this.filterApproved = filterApproved;
        this.filterRejected = filterRejected;
        this.filterHasError = filterHasError;
        this.cancelFilter = cancelFilter;
    }

    @Override
    protected void dispatch(FilterViewEventHandler handler) {
        handler.onFilterView(this);
    }

    @Override
    public Type<FilterViewEventHandler> getAssociatedType() {
        return getType();
    }

    public boolean isFilterTranslated() {
        return filterTranslated;
    }

    public boolean isFilterFuzzy() {
        return filterFuzzy;
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

    public boolean isCancelFilter() {
        return cancelFilter;
    }

    public boolean isFilterHasError() {
        return filterHasError;
    }

    @Override
    public GetTransUnitActionContext updateContext(
            GetTransUnitActionContext currentContext) {
        Preconditions.checkNotNull(currentContext,
                "current context can not be null");
        return currentContext.changeFilterFuzzy(filterFuzzy)
                .changeFilterTranslated(filterTranslated)
                .changeFilterUntranslated(filterUntranslated)
                .changeFilterApproved(filterApproved)
                .changeFilterRejected(filterRejected)
                .changeFilterHasError(filterHasError);
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("filterTranslated", filterTranslated)
                .add("filterFuzzy", filterFuzzy)
                .add("filterUntranslated", filterUntranslated)
                .add("filterApproved", filterApproved)
                .add("filterRejected", filterRejected)
                .add("filterHasError", filterHasError)
                .add("cancelFilter", cancelFilter).toString();
    }
}
