package org.fedorahosted.flies.webtrans.client;

import com.google.gwt.event.shared.GwtEvent;

public class VisibilityEvent extends GwtEvent<VisibilityHandler> {
	
	/**
	 * Handler type.
	 */
	private static Type<VisibilityHandler> TYPE;
	private final boolean visible; 
	
	/**
	 * Gets the type associated with this event.
	 * 
	 * @return returns the handler type
	 */
	public static Type<VisibilityHandler> getType() {
		return TYPE != null ? TYPE : (TYPE = new Type<VisibilityHandler>());
	}

	public VisibilityEvent(boolean transMemTabVisible) {
		this.visible = transMemTabVisible;
	}
	
	@Override
	protected void dispatch(VisibilityHandler handler) {
		handler.onVisibilityChange(this);
	}

	@Override
	public GwtEvent.Type<VisibilityHandler> getAssociatedType() {
		return getType();
	}

	public boolean isVisible() {
	    return this.visible;
	}
	
	public static <I> void fire(HasVisibilityEventHandlers source, boolean value) {
		if (TYPE != null) {
			VisibilityEvent event = new VisibilityEvent(value);
			source.fireEvent(event);
		}
	}
	
}
