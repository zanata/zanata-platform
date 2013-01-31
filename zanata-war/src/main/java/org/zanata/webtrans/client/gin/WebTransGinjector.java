package org.zanata.webtrans.client.gin;

import net.customware.gwt.presenter.client.EventBus;

import org.zanata.webtrans.client.EventProcessor;
import org.zanata.webtrans.client.history.History;
import org.zanata.webtrans.client.history.Window;
import org.zanata.webtrans.client.presenter.AppPresenter;
import org.zanata.webtrans.client.presenter.DocumentListPresenter;
import org.zanata.webtrans.client.presenter.TargetContentsPresenter;
import org.zanata.webtrans.client.presenter.UserConfigHolder;
import org.zanata.webtrans.client.rpc.CachingDispatchAsync;
import org.zanata.webtrans.client.service.HistoryEventHandlerService;
import org.zanata.webtrans.client.service.ValidationService;
import org.zanata.webtrans.shared.model.UserWorkspaceContext;

import com.google.gwt.inject.client.GinModules;
import com.google.gwt.inject.client.Ginjector;

@GinModules(WebTransClientModule.class)
public interface WebTransGinjector extends Ginjector
{

   AppPresenter getAppPresenter();

   CachingDispatchAsync getDispatcher();

   EventBus getEventBus();

   UserWorkspaceContext getUserWorkspaceContext();

   EventProcessor getEventProcessor();

   UserConfigHolder getUserConfig();
   
   Window.Location getLocation();

   DocumentListPresenter getDocumentListPresenter();

   History getHistory();

   HistoryEventHandlerService getHistoryEventHandlerService();

   TargetContentsPresenter getTargetContentsPresenter();

   ValidationService getValidationService();
}