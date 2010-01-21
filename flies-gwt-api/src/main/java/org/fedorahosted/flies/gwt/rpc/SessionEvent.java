package org.fedorahosted.flies.gwt.rpc;

import java.io.Serializable;

public class SessionEvent<T extends SessionEventData> implements Serializable, HasSequence {

	private static final long serialVersionUID = 1L;
	private T data;
	private int sequence;
	
	@SuppressWarnings("unused")
	private SessionEvent() {
	}
	
	public SessionEvent(T data, int sequence) {
		this.data = data;
		this.sequence = sequence;
	}

	public T getData() {
		return data;
	}
	
	@Override
	public int getSequence() {
		return sequence;
	}

}
