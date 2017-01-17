package org.zanata.webtrans.client.events;

import com.google.gwt.event.shared.GwtEvent;

public class PageChangeEvent extends GwtEvent<PageChangeEventHandler> {
    public static final Type<PageChangeEventHandler> TYPE =
            new Type<>();

    int page;

    public PageChangeEvent(int page) {
        this.page = page;
    }

    public Type<PageChangeEventHandler> getAssociatedType() {
        return TYPE;
    }

    protected void dispatch(PageChangeEventHandler handler) {
        handler.onPageChange(this);
    }

    public int getPageNumber() {
        return page + 1;
    }

}
