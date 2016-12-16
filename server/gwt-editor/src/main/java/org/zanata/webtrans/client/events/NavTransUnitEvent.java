package org.zanata.webtrans.client.events;

import com.google.gwt.event.shared.GwtEvent;

public class NavTransUnitEvent extends GwtEvent<NavTransUnitHandler> {
    public static final NavTransUnitEvent PREV_ENTRY_EVENT =
            new NavTransUnitEvent(NavigationType.PrevEntry);
    public static final NavTransUnitEvent NEXT_ENTRY_EVENT =
            new NavTransUnitEvent(NavigationType.NextEntry);
    public static final NavTransUnitEvent PREV_STATE_EVENT =
            new NavTransUnitEvent(NavigationType.PrevState);
    public static final NavTransUnitEvent NEXT_STATE_EVENT =
            new NavTransUnitEvent(NavigationType.NextState);
    public static final NavTransUnitEvent FIRST_ENTRY_EVENT =
            new NavTransUnitEvent(NavigationType.FirstEntry);
    public static final NavTransUnitEvent LAST_ENTRY_EVENT =
            new NavTransUnitEvent(NavigationType.LastEntry);

    public enum NavigationType {
        PrevEntry, NextEntry, PrevState, NextState, FirstEntry, LastEntry
    }

    private NavigationType rowType;

    /**
     * Handler type.
     */
    private static final Type<NavTransUnitHandler> TYPE = new Type<>();

    /**
     * Gets the type associated with this event.
     *
     * @return returns the handler type
     */
    public static Type<NavTransUnitHandler> getType() {
        return TYPE;
    }

    public NavTransUnitEvent(NavigationType typeValue) {
        this.rowType = typeValue;
    }

    @Override
    protected void dispatch(NavTransUnitHandler handler) {
        handler.onNavTransUnit(this);
    }

    @Override
    public GwtEvent.Type<NavTransUnitHandler> getAssociatedType() {
        return getType();
    }

    public NavigationType getRowType() {
        return rowType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        NavTransUnitEvent that = (NavTransUnitEvent) o;

        return rowType == that.rowType;

    }

    @Override
    public int hashCode() {
        return rowType.hashCode();
    }
}
