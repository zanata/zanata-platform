package org.zanata.webtrans.client.events;

import org.zanata.webtrans.client.service.GetTransUnitActionContext;
import org.zanata.webtrans.client.service.NavigationService;
import com.google.common.base.Preconditions;
import com.google.gwt.event.shared.GwtEvent;

public class EditorPageSizeChangeEvent extends
        GwtEvent<EditorPageSizeChangeEventHandler> implements
        NavigationService.UpdateContextCommand {
    public static final Type<EditorPageSizeChangeEventHandler> TYPE =
            new Type<>();

    private int pageSize;

    public EditorPageSizeChangeEvent(int pageSize) {
        this.pageSize = pageSize;
    }

    public Type<EditorPageSizeChangeEventHandler> getAssociatedType() {
        return TYPE;
    }

    protected void dispatch(EditorPageSizeChangeEventHandler handler) {
        handler.onPageSizeChange(this);
    }

    @Override
    public GetTransUnitActionContext updateContext(
            GetTransUnitActionContext currentContext) {
        Preconditions.checkNotNull(currentContext,
                "current context can not be null");
        return currentContext.withCount(pageSize);
    }

    public int getPageSize() {
        return pageSize;
    }
}
