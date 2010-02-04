package org.fedorahosted.flies.gwt.common;

import net.customware.gwt.dispatch.shared.Action;
import net.customware.gwt.dispatch.shared.Result;
import com.google.gwt.user.client.rpc.AsyncCallback;

public interface DispatchServiceAsync {

	void execute(Action<?> action, AsyncCallback<Result> callback);
}