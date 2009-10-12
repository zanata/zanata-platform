package org.fedorahosted.flies.webtrans.client.gin;

import net.customware.gwt.presenter.client.DefaultEventBus;
import net.customware.gwt.presenter.client.EventBus;
import net.customware.gwt.presenter.client.gin.AbstractPresenterModule;
import net.customware.gwt.presenter.client.place.PlaceManager;

import org.fedorahosted.flies.webtrans.client.AppPresenter;
import org.fedorahosted.flies.webtrans.client.CachedTransUnitTableModel;
import org.fedorahosted.flies.webtrans.client.DocumentListPresenter;
import org.fedorahosted.flies.webtrans.client.DocumentListView;
import org.fedorahosted.flies.webtrans.client.HasPageNavigation;
import org.fedorahosted.flies.webtrans.client.Pager;
import org.fedorahosted.flies.webtrans.client.TransUnitListPresenter;
import org.fedorahosted.flies.webtrans.client.TransUnitListView;
import org.fedorahosted.flies.webtrans.client.TransUnitTableDefinition;
import org.fedorahosted.flies.webtrans.client.TransUnitTableModel;
import org.fedorahosted.flies.webtrans.client.WestNavigationPresenter;
import org.fedorahosted.flies.webtrans.client.WestNavigationView;
import org.fedorahosted.flies.webtrans.client.WorkspaceUsersPresenter;
import org.fedorahosted.flies.webtrans.client.WorkspaceUsersView;

import com.google.gwt.user.client.ui.HasValue;
import com.google.inject.Singleton;

public class WebTransClientModule extends AbstractPresenterModule {

	/**
	 * The Binding EDSL is described in {@link com.google.inject.Binder}
	 */
	@Override
	protected void configure() {		
		bind(EventBus.class).to(DefaultEventBus.class).in(Singleton.class);
		bind(PlaceManager.class).in(Singleton.class);
		
		bind(TransUnitTableModel.class).in(Singleton.class);
		bind(CachedTransUnitTableModel.class).in(Singleton.class);
		bind(TransUnitTableDefinition.class).in(Singleton.class);
		bind(TransUnitListView.class).in(Singleton.class);
		
		bindPresenter(DocumentListPresenter.class, DocumentListPresenter.Display.class, DocumentListView.class);
		bindPresenter(TransUnitListPresenter.class, TransUnitListPresenter.Display.class, TransUnitListView.class);
		bindPresenter(WestNavigationPresenter.class, WestNavigationPresenter.Display.class, WestNavigationView.class);
		bindPresenter(WorkspaceUsersPresenter.class, WorkspaceUsersPresenter.Display.class, WorkspaceUsersView.class);
		bind(HasPageNavigation.class).to(TransUnitListView.class).in(Singleton.class);
		bind(AppPresenter.class).in(Singleton.class);
		//bind(DispatchAsync.class);
		
	}

}
