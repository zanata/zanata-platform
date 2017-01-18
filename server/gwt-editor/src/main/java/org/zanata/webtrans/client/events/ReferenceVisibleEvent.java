package org.zanata.webtrans.client.events;

import com.google.gwt.event.shared.GwtEvent;
import org.zanata.webtrans.shared.model.Locale;

public class ReferenceVisibleEvent extends
        GwtEvent<ReferenceVisibleEventHandler> {

    /**
     * Handler type.
     */
    private static final Type<ReferenceVisibleEventHandler> TYPE = new Type<>();

    /**
     * Gets the type associated with this event.
     *
     * @return returns the handler type
     */
    public static Type<ReferenceVisibleEventHandler> getType() {
        return TYPE;
    }

    private Locale selectedLocale;
    private boolean isVisible;

    public ReferenceVisibleEvent(Locale selectedLocale, boolean isVisible) {
        this.selectedLocale = selectedLocale;
        this.isVisible = isVisible;
    }

    public Locale getSelectedLocale() {
        return selectedLocale;
    }

    public boolean isVisible() {
        return isVisible;
    }

    @Override
    public Type<ReferenceVisibleEventHandler> getAssociatedType() {
        return getType();
    }

    @Override
    protected void dispatch(ReferenceVisibleEventHandler handler) {
        handler.onShowHideReference(this);
    }
}
