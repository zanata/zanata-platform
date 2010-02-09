package org.fedorahosted.flies.webtrans.client;

import com.google.gwt.event.shared.GwtEvent;

public class TMTabSelectionEvent extends GwtEvent<TMTabSelectionHandler> {
	
	/**
	 * Handler type.
	 */
	private static Type<TMTabSelectionHandler> TYPE;
	private boolean TransMemoryTabVisible;
	
	/**
	 * Gets the type associated with this event.
	 * 
	 * @return returns the handler type
	 */
	public static Type<TMTabSelectionHandler> getType() {
		return TYPE != null ? TYPE : (TYPE = new Type<TMTabSelectionHandler>());
	}

	public TMTabSelectionEvent(boolean value) {
		TransMemoryTabVisible = value;
	}
	
	@Override
	protected void dispatch(TMTabSelectionHandler handler) {
		handler.onTMTabSelected(this);
	}

	@Override
	public GwtEvent.Type<TMTabSelectionHandler> getAssociatedType() {
		return getType();
	}
	
	public boolean getTransMemoryTabStatus() {
		return TransMemoryTabVisible;
	}

}
