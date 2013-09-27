package org.zanata.webtrans.client.events;

import com.google.gwt.event.shared.GwtEvent;

public class ShowSideMenuEvent extends GwtEvent<ShowSideMenuEventHandler> {

    /**
     * Handler type.
     */
    private static Type<ShowSideMenuEventHandler> TYPE;

    /**
     * Gets the type associated with this event.
     *
     * @return returns the handler type
     */
    public static Type<ShowSideMenuEventHandler> getType() {
        if (TYPE == null) {
            TYPE = new Type<ShowSideMenuEventHandler>();
        }
        return TYPE;
    }

    private boolean isShowing;

    public ShowSideMenuEvent(boolean isShowing) {
        this.isShowing = isShowing;
    }

    public boolean isShowing() {
        return isShowing;
    }

    @Override
    public Type<ShowSideMenuEventHandler> getAssociatedType() {
        return getType();
    }

    @Override
    protected void dispatch(ShowSideMenuEventHandler handler) {
        handler.onShowSideMenu(this);
    }

}
