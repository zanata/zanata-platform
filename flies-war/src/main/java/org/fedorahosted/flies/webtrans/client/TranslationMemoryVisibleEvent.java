package org.fedorahosted.flies.webtrans.client;

import com.google.gwt.event.shared.GwtEvent;

public class TranslationMemoryVisibleEvent extends GwtEvent<TranslationMemoryVisibleHandler> {
	
	/**
	 * Handler type.
	 */
	private static Type<TranslationMemoryVisibleHandler> TYPE;
	private boolean transMemoryTabVisible;
	
	/**
	 * Gets the type associated with this event.
	 * 
	 * @return returns the handler type
	 */
	public static Type<TranslationMemoryVisibleHandler> getType() {
		return TYPE != null ? TYPE : (TYPE = new Type<TranslationMemoryVisibleHandler>());
	}

	public TranslationMemoryVisibleEvent(boolean transMemTabStatus) {
		transMemoryTabVisible = transMemTabStatus;
	}
	
	@Override
	protected void dispatch(TranslationMemoryVisibleHandler handler) {
		handler.onTransMemorySelected(this);
	}

	@Override
	public GwtEvent.Type<TranslationMemoryVisibleHandler> getAssociatedType() {
		return getType();
	}
	
	public boolean isTranslationMemoryVisible() {
		return transMemoryTabVisible;
	}

}
