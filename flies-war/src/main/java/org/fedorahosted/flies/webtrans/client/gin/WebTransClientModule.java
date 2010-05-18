package org.fedorahosted.flies.webtrans.client.gin;


import net.customware.gwt.presenter.client.DefaultEventBus;
import net.customware.gwt.presenter.client.EventBus;
import net.customware.gwt.presenter.client.gin.AbstractPresenterModule;
import net.customware.gwt.presenter.client.place.PlaceManager;

import org.fedorahosted.flies.webtrans.client.AppPresenter;
import org.fedorahosted.flies.webtrans.client.AppView;
import org.fedorahosted.flies.webtrans.client.Application;
import org.fedorahosted.flies.webtrans.client.DocumentListPresenter;
import org.fedorahosted.flies.webtrans.client.DocumentListView;
import org.fedorahosted.flies.webtrans.client.EventProcessor;
import org.fedorahosted.flies.webtrans.client.Resources;
import org.fedorahosted.flies.webtrans.client.SidePanel;
import org.fedorahosted.flies.webtrans.client.SidePanelPresenter;
import org.fedorahosted.flies.webtrans.client.TransMemoryPresenter;
import org.fedorahosted.flies.webtrans.client.TransMemoryView;
import org.fedorahosted.flies.webtrans.client.TransUnitDetailsPresenter;
import org.fedorahosted.flies.webtrans.client.TransUnitDetailsView;
import org.fedorahosted.flies.webtrans.client.TransUnitNavigationPresenter;
import org.fedorahosted.flies.webtrans.client.TransUnitNavigationView;
import org.fedorahosted.flies.webtrans.client.TranslationEditorPresenter;
import org.fedorahosted.flies.webtrans.client.TranslationEditorView;
import org.fedorahosted.flies.webtrans.client.WebTransMessages;
import org.fedorahosted.flies.webtrans.client.WorkspaceUsersPresenter;
import org.fedorahosted.flies.webtrans.client.WorkspaceUsersView;
import org.fedorahosted.flies.webtrans.client.editor.HasPageNavigation;
import org.fedorahosted.flies.webtrans.client.editor.filter.TransFilterPresenter;
import org.fedorahosted.flies.webtrans.client.editor.filter.TransFilterView;
import org.fedorahosted.flies.webtrans.client.editor.table.TableEditorPresenter;
import org.fedorahosted.flies.webtrans.client.editor.table.TableEditorView;
import org.fedorahosted.flies.webtrans.client.rpc.CachingDispatchAsync;
import org.fedorahosted.flies.webtrans.client.rpc.DelegatingDispatchAsync;
import org.fedorahosted.flies.webtrans.shared.auth.Identity;
import org.fedorahosted.flies.webtrans.shared.model.WorkspaceContext;

import com.google.inject.Provider;
import com.google.inject.Singleton;

public class WebTransClientModule extends AbstractPresenterModule {

	/**
	 * The Binding EDSL is described in {@link com.google.inject.Binder}
	 */
	@Override
	protected void configure() {		
		bind(EventBus.class).to(DefaultEventBus.class).in(Singleton.class);
		bind(PlaceManager.class).in(Singleton.class);
		bind(EventProcessor.class).in(Singleton.class);
		bind(Resources.class).in(Singleton.class);
		bind(WebTransMessages.class).in(Singleton.class);
	
		bindPresenter(AppPresenter.class, AppPresenter.Display.class, AppView.class);
		bindPresenter(DocumentListPresenter.class, DocumentListPresenter.Display.class, DocumentListView.class);
		bindPresenter(TransFilterPresenter.class, TransFilterPresenter.Display.class, TransFilterView.class);
		bindPresenter(TableEditorPresenter.class, TableEditorPresenter.Display.class, TableEditorView.class);
		bindPresenter(WorkspaceUsersPresenter.class, WorkspaceUsersPresenter.Display.class, WorkspaceUsersView.class);
		bindPresenter(TransMemoryPresenter.class, TransMemoryPresenter.Display.class, TransMemoryView.class);
		bindPresenter(TransUnitNavigationPresenter.class, TransUnitNavigationPresenter.Display.class, TransUnitNavigationView.class);
		bindPresenter(SidePanelPresenter.class, SidePanelPresenter.Display.class, SidePanel.class);
		bindPresenter(TranslationEditorPresenter.class, TranslationEditorPresenter.Display.class, TranslationEditorView.class);
		bindPresenter(TransUnitDetailsPresenter.class, TransUnitDetailsPresenter.Display.class, TransUnitDetailsView.class);
		
		bind(HasPageNavigation.class).to(TableEditorView.class).in(Singleton.class);

		// NB: if we bind directly to SeamDispatchAsync, we can't use replace-class in
		// the module definition unless the replacement extends SeamDispatchAsync
		bind(CachingDispatchAsync.class).to(DelegatingDispatchAsync.class).in(Singleton.class);
		
		bind(Identity.class).toProvider(IdentityProvider.class).in(Singleton.class);
		bind(WorkspaceContext.class).toProvider(WorkspaceContextProvider.class).in(Singleton.class);
		
	}
	
	static class WorkspaceContextProvider implements Provider<WorkspaceContext> {
		@Override
		public WorkspaceContext get() {
			return Application.getWorkspaceContext();
		}
	}
	
	static class IdentityProvider implements Provider<Identity> {
		@Override
		public Identity get() {
			return Application.getIdentity();
		}
	}

}
