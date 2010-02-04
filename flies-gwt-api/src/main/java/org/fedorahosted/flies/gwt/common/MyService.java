package org.fedorahosted.flies.gwt.common;

import com.google.gwt.user.client.rpc.RemoteService;

public interface MyService extends RemoteService {

    public String askIt(String question);      

 }