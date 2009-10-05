package org.fedorahosted.flies.webtrans.client.gin;

import org.fedorahosted.flies.webtrans.client.CachingDispatchAsync;
import org.fedorahosted.flies.webtrans.client.mvp.AppPresenter;
import org.fedorahosted.flies.webtrans.client.mvp.TransUnitListPresenter;
import org.fedorahosted.flies.webtrans.client.mvp.TransUnitListView;

import com.google.inject.Singleton;

import net.customware.gwt.dispatch.client.DefaultDispatchAsync;
import net.customware.gwt.presenter.client.DefaultEventBus;
import net.customware.gwt.presenter.client.EventBus;
import net.customware.gwt.presenter.client.gin.AbstractPresenterModule;
import net.customware.gwt.presenter.client.place.PlaceManager;

public class WebTransClientModule extends AbstractPresenterModule {
	
	@Override
	protected void configure() {		
		bind(EventBus.class).to(DefaultEventBus.class).in(Singleton.class);
		bind(PlaceManager.class).in(Singleton.class);
		
		bindPresenter(TransUnitListPresenter.class, TransUnitListPresenter.Display.class, TransUnitListView.class);
//		bindPresenter(TransUnitListResponsePresenter.class, TransUnitListResponsePresenter.Display.class, TransUnitListResponseView.class);
		
		bind(AppPresenter.class).in(Singleton.class);
		bind(CachingDispatchAsync.class);
		
	}

}
