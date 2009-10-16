package org.fedorahosted.flies.gwt.rpc;

import com.google.gwt.user.client.rpc.IsSerializable;

import net.customware.gwt.dispatch.shared.Result;

public class UpdateTransUnitResult implements Result, IsSerializable {


	private static final long serialVersionUID = -5110777165188058021L;

	private boolean success;
	
	@SuppressWarnings("unused")
	private UpdateTransUnitResult() {
	}
	
	public UpdateTransUnitResult(boolean success) {
		this.success = success;
	}
	
	public boolean isSuccess() {
		return success;
	}
}
