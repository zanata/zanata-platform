package org.zanata.webtrans.client.events;

import com.google.gwt.event.shared.GwtEvent;

public class LoadingEvent extends GwtEvent<LoadingEventHandler> {
    public static final Type<LoadingEventHandler> TYPE =
            new Type<>();

    public static final LoadingEvent START_EVENT = new LoadingEvent();
    public static final LoadingEvent FINISH_EVENT = new LoadingEvent();

    private LoadingEvent() {
    }

    public Type<LoadingEventHandler> getAssociatedType() {
        return TYPE;
    }

    protected void dispatch(LoadingEventHandler handler) {
        handler.onLoading(this);
    }
}
