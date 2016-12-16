package org.zanata.webtrans.client.events;

import com.google.gwt.event.shared.GwtEvent;

public class InitEditorEvent extends GwtEvent<InitEditorEventHandler> {
    public static final Type<InitEditorEventHandler> TYPE = new Type<>();

    public Type<InitEditorEventHandler> getAssociatedType() {
        return TYPE;
    }

    protected void dispatch(InitEditorEventHandler handler) {
        handler.onInitEditor(this);
    }
}
