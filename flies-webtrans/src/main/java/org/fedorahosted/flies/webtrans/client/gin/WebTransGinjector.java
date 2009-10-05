package org.fedorahosted.flies.webtrans.client.gin;


import org.fedorahosted.flies.webtrans.client.mvp.AppPresenter;

import net.customware.gwt.dispatch.client.gin.ClientDispatchModule;
import net.customware.gwt.presenter.client.place.PlaceManager;
import com.google.gwt.inject.client.GinModules;
import com.google.gwt.inject.client.Ginjector;

@GinModules({ ClientDispatchModule.class, WebTransClientModule.class })
public interface WebTransGinjector extends Ginjector {

	AppPresenter getAppPresenter();

	PlaceManager getPlaceManager();

}