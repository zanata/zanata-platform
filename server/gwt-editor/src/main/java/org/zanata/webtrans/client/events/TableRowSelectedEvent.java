package org.zanata.webtrans.client.events;

import org.zanata.webtrans.shared.model.TransUnitId;
import com.google.gwt.event.shared.GwtEvent;

public class TableRowSelectedEvent extends
        GwtEvent<TableRowSelectedEventHandler> {
    public static final Type<TableRowSelectedEventHandler> TYPE =
            new Type<>();

    private TransUnitId selectedId;
    private boolean suppressSavePending = false;

    public TableRowSelectedEvent(TransUnitId transUnitId) {
        this.selectedId = transUnitId;
    }

    public Type<TableRowSelectedEventHandler> getAssociatedType() {
        return TYPE;
    }

    protected void dispatch(TableRowSelectedEventHandler handler) {
        handler.onTableRowSelected(this);
    }

    public TransUnitId getSelectedId() {
        return selectedId;
    }

    public TableRowSelectedEvent setSuppressSavePending(
            boolean suppressSavePending) {
        this.suppressSavePending = suppressSavePending;
        return this;
    }

    public boolean isSuppressSavePending() {
        return suppressSavePending;
    }
}
