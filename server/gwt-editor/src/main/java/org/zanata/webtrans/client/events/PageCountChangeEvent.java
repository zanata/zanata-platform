package org.zanata.webtrans.client.events;

import com.google.gwt.event.shared.GwtEvent;

public class PageCountChangeEvent extends GwtEvent<PageCountChangeEventHandler> {
    public static final Type<PageCountChangeEventHandler> TYPE =
            new Type<>();

    private int pageCount;

    public PageCountChangeEvent(int pageCount) {
        this.pageCount = pageCount;
    }

    public Type<PageCountChangeEventHandler> getAssociatedType() {
        return TYPE;
    }

    protected void dispatch(PageCountChangeEventHandler handler) {
        handler.onPageCountChange(this);
    }

    public int getPageCount() {
        return pageCount;
    }
}
