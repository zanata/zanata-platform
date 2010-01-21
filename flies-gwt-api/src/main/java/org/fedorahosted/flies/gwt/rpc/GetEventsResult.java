package org.fedorahosted.flies.gwt.rpc;

import java.util.ArrayList;

import com.google.gwt.user.client.rpc.IsSerializable;

import net.customware.gwt.dispatch.shared.Result;

public class GetEventsResult implements Result, IsSerializable {
	
	private static final long serialVersionUID = 1L;
	private ArrayList<SessionEvent<?>> events;
	
	@SuppressWarnings("unused")
	private GetEventsResult() {
	}
	
	public GetEventsResult(ArrayList<SessionEvent<?>> events) {
		this.events = events;
	}
	
	public ArrayList<SessionEvent<?>> getEvents() {
		return events;
	}

}
