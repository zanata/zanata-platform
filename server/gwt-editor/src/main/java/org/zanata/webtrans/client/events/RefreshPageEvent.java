package org.zanata.webtrans.client.events;

import com.google.gwt.event.shared.GwtEvent;

public class RefreshPageEvent extends GwtEvent<RefreshPageEventHandler> {
    public static final Type<RefreshPageEventHandler> TYPE =
            new Type<>();
    /**
     * When firing this event, code mirror instances will be refreshed.
     */
    public static final RefreshPageEvent REFRESH_CODEMIRROR_EVENT =
            new RefreshPageEvent();
    /**
     * When firing this event, we are switching from code mirror to plain
     * textarea.
     */
    public static final RefreshPageEvent REDRAW_PAGE_EVENT =
            new RefreshPageEvent();
    /**
     * When firing this event, we are switching from edit mode to review mode
     */
    public static final RefreshPageEvent REVIEW_MODE_EVENT =
            new RefreshPageEvent();

    private RefreshPageEvent() {
    }

    public Type<RefreshPageEventHandler> getAssociatedType() {
        return TYPE;
    }

    protected void dispatch(RefreshPageEventHandler handler) {
        handler.onRefreshPage(this);
    }
}
