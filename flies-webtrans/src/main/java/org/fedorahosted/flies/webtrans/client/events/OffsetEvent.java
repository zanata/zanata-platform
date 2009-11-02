package org.fedorahosted.flies.webtrans.client.events;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;

public abstract class OffsetEvent<H extends EventHandler> extends GwtEvent<H>{
	
	private final int offset;
	
	public OffsetEvent(int offset) {
		this.offset = offset;
	}
	
	public final int getOffset() {
		return offset;
	}

}
