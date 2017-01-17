package org.zanata.webtrans.client.events;

import java.util.List;

import org.zanata.webtrans.client.keys.KeyShortcut;

import com.google.gwt.event.shared.GwtEvent;

public class AttentionModeActivationEvent extends
        GwtEvent<AttentionModeActivationEventHandler> {
    /**
     * Handler type.
     */
    public static final Type<AttentionModeActivationEventHandler> TYPE =
            new Type<>();

    /**
     * Gets the type associated with this event.
     *
     * @return returns the handler type
     */
    public static Type<AttentionModeActivationEventHandler> getType() {
        return TYPE;
    }

    private final boolean active;
    private List<KeyShortcut> shortcuts;

    public AttentionModeActivationEvent(boolean isActive) {
        this.active = isActive;
    }

    public boolean isActive() {
        return active;
    }

    public List<KeyShortcut> getShortcuts() {
        return shortcuts;
    }

    @Override
    protected void dispatch(AttentionModeActivationEventHandler handler) {
        handler.onAttentionModeActivationChanged(this);
    }

    @Override
    public Type<AttentionModeActivationEventHandler> getAssociatedType() {
        return getType();
    }

    public void setShortcuts(List<KeyShortcut> shortcuts) {
        this.shortcuts = shortcuts;
    }

}
