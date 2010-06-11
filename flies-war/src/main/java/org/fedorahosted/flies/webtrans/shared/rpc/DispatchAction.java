package org.fedorahosted.flies.webtrans.shared.rpc;

import net.customware.gwt.dispatch.shared.Result;

import com.google.gwt.user.client.rpc.IsSerializable;


public interface DispatchAction<R extends Result> extends IsSerializable, net.customware.gwt.dispatch.shared.Action<R> {
	public String getCsrfToken();
}
