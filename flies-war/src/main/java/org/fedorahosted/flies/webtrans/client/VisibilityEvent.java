package org.fedorahosted.flies.webtrans.client;

import com.google.gwt.event.shared.GwtEvent;

public class VisibilityEvent<I> extends GwtEvent<VisibilityHandler> {
	
	/**
	 * Handler type.
	 */
	private static Type<VisibilityHandler> TYPE;
	private final I value;
	
	/**
	 * Gets the type associated with this event.
	 * 
	 * @return returns the handler type
	 */
	public static Type<VisibilityHandler> getType() {
		return TYPE != null ? TYPE : (TYPE = new Type<VisibilityHandler>());
	}

	public VisibilityEvent(I value) {
		this.value = value;
	}
	
	@Override
	protected void dispatch(VisibilityHandler handler) {
		handler.onTransMemorySelected(this);
	}

	@Override
	public GwtEvent.Type<VisibilityHandler> getAssociatedType() {
		return getType();
	}

	public I getValue() {
	    return value;
	}
	
	public static <I> void fire(HasVisibilityEventHandlers source, I value) {
		if (TYPE != null) {
			VisibilityEvent<I> event = new VisibilityEvent<I>(value);
			source.fireEvent(event);
		}
	}
	
}
