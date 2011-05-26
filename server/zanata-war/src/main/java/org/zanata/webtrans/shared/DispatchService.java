package org.zanata.webtrans.shared;

import net.customware.gwt.dispatch.shared.Action;
import net.customware.gwt.dispatch.shared.Result;

import com.google.gwt.user.client.rpc.RemoteService;

public interface DispatchService extends RemoteService
{

   Result execute(Action<?> action) throws Exception;

   void rollback(Action<Result> action, Result result) throws Exception;
}