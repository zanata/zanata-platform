package org.fedorahosted.flies.gwt.common;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface MyServiceAsync {

	void askIt(String question, AsyncCallback<String> callback);

}
