package org.fedorahosted.flies.webtrans.shared.common;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface MyServiceAsync {

	void askIt(String question, AsyncCallback<String> callback);

}
