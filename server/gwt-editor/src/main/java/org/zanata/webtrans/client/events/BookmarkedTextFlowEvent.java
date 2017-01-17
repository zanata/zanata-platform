package org.zanata.webtrans.client.events;

import org.zanata.webtrans.client.service.GetTransUnitActionContext;
import org.zanata.webtrans.client.service.NavigationService;
import org.zanata.webtrans.shared.model.TransUnitId;
import com.google.gwt.event.shared.GwtEvent;

public class BookmarkedTextFlowEvent extends
        GwtEvent<BookmarkedTextFlowEventHandler> implements
        NavigationService.UpdateContextCommand {
    public static final Type<BookmarkedTextFlowEventHandler> TYPE =
            new Type<>();

    private int offset;
    private TransUnitId targetTransUnitId;

    public BookmarkedTextFlowEvent(int offset, TransUnitId textFlowId) {
        this.offset = offset;
        this.targetTransUnitId = textFlowId;
    }

    public Type<BookmarkedTextFlowEventHandler> getAssociatedType() {
        return TYPE;
    }

    protected void dispatch(BookmarkedTextFlowEventHandler handler) {
        handler.onBookmarkableTextFlow(this);
    }

    @Override
    public GetTransUnitActionContext updateContext(
            GetTransUnitActionContext currentContext) {
        return currentContext.withOffset(offset).withTargetTransUnitId(
                targetTransUnitId);
    }

    public int getOffset() {
        return offset;
    }

    public TransUnitId getTargetTransUnitId() {
        return targetTransUnitId;
    }
}
