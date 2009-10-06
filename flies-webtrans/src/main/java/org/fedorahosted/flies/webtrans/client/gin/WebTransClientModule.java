package org.fedorahosted.flies.webtrans.client.gin;

import net.customware.gwt.presenter.client.DefaultEventBus;
import net.customware.gwt.presenter.client.EventBus;
import net.customware.gwt.presenter.client.gin.AbstractPresenterModule;
import net.customware.gwt.presenter.client.place.PlaceManager;

import org.fedorahosted.flies.webtrans.client.mvp.AppPresenter;
import org.fedorahosted.flies.webtrans.client.mvp.TransUnitListPresenter;
import org.fedorahosted.flies.webtrans.client.mvp.TransUnitListView;
import org.fedorahosted.flies.webtrans.client.mvp.WestNavigationPresenter;
import org.fedorahosted.flies.webtrans.client.mvp.WestNavigationView;

import com.google.inject.Singleton;

public class WebTransClientModule extends AbstractPresenterModule {
	
	@Override
	protected void configure() {		
		bind(EventBus.class).to(DefaultEventBus.class).in(Singleton.class);
		bind(PlaceManager.class).in(Singleton.class);
		
		bindPresenter(TransUnitListPresenter.class, TransUnitListPresenter.Display.class, TransUnitListView.class);
		bindPresenter(WestNavigationPresenter.class, WestNavigationPresenter.Display.class, WestNavigationView.class);
//		bindPresenter(TransUnitListResponsePresenter.class, TransUnitListResponsePresenter.Display.class, TransUnitListResponseView.class);
		
		bind(AppPresenter.class).in(Singleton.class);
		//bind(DispatchAsync.class);
		
	}

}
