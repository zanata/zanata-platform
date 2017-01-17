package org.zanata.webtrans.client.events;

import com.google.gwt.event.shared.GwtEvent;

public class TransMemoryShortcutCopyEvent extends
        GwtEvent<TransMemoryShorcutCopyHandler> {

    /**
     * Handler type.
     */
    private static final Type<TransMemoryShorcutCopyHandler> TYPE = new Type<>();

    /**
     * Gets the type associated with this event.
     *
     * @return returns the handler type
     */
    public static Type<TransMemoryShorcutCopyHandler> getType() {
        return TYPE;
    }

    private int index;

    public TransMemoryShortcutCopyEvent(int index) {
        this.index = index;
    }

    @Override
    protected void dispatch(TransMemoryShorcutCopyHandler handler) {
        handler.onTransMemoryCopy(this);
    }

    @Override
    public GwtEvent.Type<TransMemoryShorcutCopyHandler> getAssociatedType() {
        return getType();
    }

    public int getIndex() {
        return index;
    }
}
