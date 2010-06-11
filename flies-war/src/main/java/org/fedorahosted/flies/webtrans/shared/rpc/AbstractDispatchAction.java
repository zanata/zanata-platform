package org.fedorahosted.flies.webtrans.shared.rpc;

import net.customware.gwt.dispatch.shared.Result;

public class AbstractDispatchAction<R extends Result> implements DispatchAction<R> {
	private String csrfToken;
	
	public void setCsrfToken(String csrfSecret) {
		this.csrfToken = csrfSecret;
	}
	
	public String getCsrfToken() {
		return csrfToken;
	}

}
