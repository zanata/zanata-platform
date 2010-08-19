package net.openl10n.flies.webtrans.client.gin;

import net.customware.gwt.presenter.client.EventBus;
import net.customware.gwt.presenter.client.place.PlaceManager;
import net.openl10n.flies.webtrans.client.AppPresenter;
import net.openl10n.flies.webtrans.client.EventProcessor;
import net.openl10n.flies.webtrans.client.rpc.CachingDispatchAsync;
import net.openl10n.flies.webtrans.shared.model.WorkspaceContext;


import com.google.gwt.inject.client.GinModules;
import com.google.gwt.inject.client.Ginjector;

@GinModules( { WebTransClientModule.class })
public interface WebTransGinjector extends Ginjector
{

   AppPresenter getAppPresenter();

   CachingDispatchAsync getDispatcher();

   PlaceManager getPlaceManager();

   EventBus getEventBus();

   WorkspaceContext getWorkspaceContext();

   EventProcessor getEventProcessor();

}