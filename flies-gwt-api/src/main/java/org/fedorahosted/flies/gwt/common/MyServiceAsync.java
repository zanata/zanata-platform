package org.fedorahosted.flies.gwt.common;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.RemoteService;

public interface MyServiceAsync extends RemoteService {

   public void askIt(String question, AsyncCallback<String> callback);

}