package org.zanata.webtrans.client.gin;

import net.customware.gwt.presenter.client.EventBus;

import org.zanata.webtrans.client.EventProcessor;
import org.zanata.webtrans.client.presenter.AppPresenter;
import org.zanata.webtrans.client.rpc.CachingDispatchAsync;
import org.zanata.webtrans.shared.model.WorkspaceContext;

import com.google.gwt.inject.client.GinModules;
import com.google.gwt.inject.client.Ginjector;

@GinModules(WebTransClientModule.class)
public interface WebTransGinjector extends Ginjector
{

   AppPresenter getAppPresenter();

   CachingDispatchAsync getDispatcher();

   EventBus getEventBus();

   WorkspaceContext getWorkspaceContext();

   EventProcessor getEventProcessor();

}