package org.zanata.webtrans.client.events;

import org.zanata.webtrans.shared.model.DocumentInfo;
import org.zanata.webtrans.shared.model.TransUnitId;

import com.google.gwt.event.shared.GwtEvent;

public class RequestSelectTableRowEvent extends
        GwtEvent<RequestSelectTableRowEventHandler> {
    public static final Type<RequestSelectTableRowEventHandler> TYPE =
            new Type<>();

    private TransUnitId selectedId;
    private DocumentInfo docInfo;
    private boolean suppressSavePending = false;

    public RequestSelectTableRowEvent(DocumentInfo docInfo,
            TransUnitId transUnitId) {
        this.selectedId = transUnitId;
        this.docInfo = docInfo;
    }

    public Type<RequestSelectTableRowEventHandler> getAssociatedType() {
        return TYPE;
    }

    protected void dispatch(RequestSelectTableRowEventHandler handler) {
        handler.onRequestSelectTableRow(this);
    }

    public TransUnitId getSelectedId() {
        return selectedId;
    }

    public DocumentInfo getDocInfo() {
        return docInfo;
    }

    public RequestSelectTableRowEvent setSuppressSavePending(
            boolean suppressSavePending) {
        this.suppressSavePending = suppressSavePending;
        return this;
    }

    public boolean isSuppressSavePending() {
        return suppressSavePending;
    }
}
