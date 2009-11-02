package org.fedorahosted.flies.webtrans.client.events;

import org.fedorahosted.flies.gwt.rpc.TransUnitUpdated;

import com.google.gwt.event.shared.GwtEvent;

public class TransUnitUpdatedEvent extends GwtEvent<TransUnitUpdatedEventHandler>{
	
	/**
	 * Handler type.
	 */
	private static Type<TransUnitUpdatedEventHandler> TYPE;

	/**
	 * Gets the type associated with this event.
	 * 
	 * @return returns the handler type
	 */
	public static Type<TransUnitUpdatedEventHandler> getType() {
		if (TYPE == null) {
			TYPE = new Type<TransUnitUpdatedEventHandler>();
		}
		return TYPE;
	}
	
	private final TransUnitUpdated data;
	
	public TransUnitUpdatedEvent(TransUnitUpdated data) {
		this.data = data;
	}
	
	public TransUnitUpdated getData() {
		return data;
	}
	
	@Override
	protected void dispatch(TransUnitUpdatedEventHandler handler) {
		handler.onTransUnitUpdated(this);
	}

	@Override
	public Type<TransUnitUpdatedEventHandler> getAssociatedType() {
		return getType();
	}

}
