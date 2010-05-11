package org.fedorahosted.flies.webtrans.shared.common;

import net.customware.gwt.dispatch.shared.Action;
import net.customware.gwt.dispatch.shared.Result;
import com.google.gwt.user.client.rpc.RemoteService;

public interface DispatchService extends RemoteService {

	Result execute(Action<?> action) throws Exception;
}