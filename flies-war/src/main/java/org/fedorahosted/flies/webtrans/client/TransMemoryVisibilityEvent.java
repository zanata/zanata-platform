package org.fedorahosted.flies.webtrans.client;

import com.google.gwt.event.shared.GwtEvent;

public class TransMemoryVisibilityEvent extends GwtEvent<TransMemoryVisibilityHandler> {
	
	/**
	 * Handler type.
	 */
	private static Type<TransMemoryVisibilityHandler> TYPE;
	private final boolean visible; 
	
	/**
	 * Gets the type associated with this event.
	 * 
	 * @return returns the handler type
	 */
	public static Type<TransMemoryVisibilityHandler> getType() {
		return TYPE != null ? TYPE : (TYPE = new Type<TransMemoryVisibilityHandler>());
	}

	public TransMemoryVisibilityEvent(boolean transMemTabVisible) {
		this.visible = transMemTabVisible;
	}
	
	@Override
	protected void dispatch(TransMemoryVisibilityHandler handler) {
		handler.onVisibilityChange(this);
	}

	@Override
	public GwtEvent.Type<TransMemoryVisibilityHandler> getAssociatedType() {
		return getType();
	}

	public boolean isVisible() {
	    return this.visible;
	}
	
	public static <I> void fire(HasTransMemoryVisibilityHandlers source, boolean value) {
		if (TYPE != null) {
			TransMemoryVisibilityEvent event = new TransMemoryVisibilityEvent(value);
			source.fireEvent(event);
		}
	}
	
}
