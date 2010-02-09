package org.fedorahosted.flies.webtrans.client;

import org.fedorahosted.flies.common.ContentState;

import com.google.gwt.event.shared.GwtEvent;

public class TransMemoryCopyEvent extends GwtEvent<TransMemoryCopyHandler> {
	
	/**
	 * Handler type.
	 */
	private static Type<TransMemoryCopyHandler> TYPE;
	
	/**
	 * Gets the type associated with this event.
	 * 
	 * @return returns the handler type
	 */
	public static Type<TransMemoryCopyHandler> getType() {
		return TYPE != null ? TYPE : (TYPE = new Type<TransMemoryCopyHandler>());
	}
	
	private String sourceResult, targetResult;

	/**
	 * ContentState may be New, NeedApproved or null.
	 * stepValue may be -1 or +1.
	 * @param sourceResult
	 * @param targetResult
	 */
	public TransMemoryCopyEvent(String sourceResult, String targetResult) {
		this.sourceResult = sourceResult;
		this.targetResult = targetResult;
	}
	
	@Override
	protected void dispatch(TransMemoryCopyHandler handler) {
		handler.onTransMemoryCopy(this);
	}

	@Override
	public GwtEvent.Type<TransMemoryCopyHandler> getAssociatedType() {
		return getType();
	}

	public String getSourceResult() {
		return sourceResult;
	}

	public String getTargetResult() {
		return targetResult;
	}
}