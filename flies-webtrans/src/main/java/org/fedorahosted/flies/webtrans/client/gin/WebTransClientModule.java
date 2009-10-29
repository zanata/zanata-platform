package org.fedorahosted.flies.webtrans.client.gin;

import net.customware.gwt.dispatch.client.DispatchAsync;
import net.customware.gwt.presenter.client.DefaultEventBus;
import net.customware.gwt.presenter.client.EventBus;
import net.customware.gwt.presenter.client.gin.AbstractPresenterModule;
import net.customware.gwt.presenter.client.place.PlaceManager;

import org.fedorahosted.flies.webtrans.client.AppPresenter;
import org.fedorahosted.flies.webtrans.client.AppView;
import org.fedorahosted.flies.webtrans.client.DocumentListPresenter;
import org.fedorahosted.flies.webtrans.client.DocumentListView;
import org.fedorahosted.flies.webtrans.client.FlatFolderDocNameMapper;
import org.fedorahosted.flies.webtrans.client.LoginPanel;
import org.fedorahosted.flies.webtrans.client.LoginPresenter;
import org.fedorahosted.flies.webtrans.client.WestNavigationPresenter;
import org.fedorahosted.flies.webtrans.client.WestNavigationView;
import org.fedorahosted.flies.webtrans.client.WorkspaceUsersPresenter;
import org.fedorahosted.flies.webtrans.client.WorkspaceUsersView;
import org.fedorahosted.flies.webtrans.client.auth.Identity;
import org.fedorahosted.flies.webtrans.client.auth.IdentityImpl;
import org.fedorahosted.flies.webtrans.client.rpc.DelegatingDispatchAsync;
import org.fedorahosted.flies.webtrans.client.ui.Pager;
import org.fedorahosted.flies.webtrans.client.ui.TreeNodeMapper;
import org.fedorahosted.flies.webtrans.editor.CachedTransUnitListTableModel;
import org.fedorahosted.flies.webtrans.editor.HasPageNavigation;
import org.fedorahosted.flies.webtrans.editor.StatusBar;
import org.fedorahosted.flies.webtrans.editor.StatusBarPresenter;
import org.fedorahosted.flies.webtrans.editor.TransUnitListEditorView;
import org.fedorahosted.flies.webtrans.editor.TransUnitListEditorPresenter;
import org.fedorahosted.flies.webtrans.editor.TransUnitListEditorTableDefinition;
import org.fedorahosted.flies.webtrans.editor.WebTransEditorPresenter;
import org.fedorahosted.flies.webtrans.editor.WebTransEditorView;
import org.fedorahosted.flies.webtrans.editor.TransUnitListEditorTableModel;
import org.fedorahosted.flies.webtrans.editor.filter.FilterPresenter;
import org.fedorahosted.flies.webtrans.editor.filter.FilterView;
import org.fedorahosted.flies.webtrans.editor.filter.PhraseFilterPresenter;
import org.fedorahosted.flies.webtrans.editor.filter.PhraseFilterWidget;

import com.google.inject.Singleton;

public class WebTransClientModule extends AbstractPresenterModule {

	/**
	 * The Binding EDSL is described in {@link com.google.inject.Binder}
	 */
	@Override
	protected void configure() {		
		bind(EventBus.class).to(DefaultEventBus.class).in(Singleton.class);
		bind(PlaceManager.class).in(Singleton.class);
		
		bindPresenter(AppPresenter.class, AppPresenter.Display.class, AppView.class);
		bindPresenter(DocumentListPresenter.class, DocumentListPresenter.Display.class, DocumentListView.class);
		bindPresenter(FilterPresenter.class, FilterPresenter.Display.class, FilterView.class);
		bindPresenter(PhraseFilterPresenter.class, PhraseFilterPresenter.Display.class, PhraseFilterWidget.class);
		bindPresenter(TransUnitListEditorPresenter.class, TransUnitListEditorPresenter.Display.class, TransUnitListEditorView.class);
		bindPresenter(WestNavigationPresenter.class, WestNavigationPresenter.Display.class, WestNavigationView.class);
		bindPresenter(WorkspaceUsersPresenter.class, WorkspaceUsersPresenter.Display.class, WorkspaceUsersView.class);
		bindPresenter(WebTransEditorPresenter.class, WebTransEditorPresenter.Display.class, WebTransEditorView.class);
		bindPresenter(LoginPresenter.class, LoginPresenter.Display.class, LoginPanel.class);
		bindPresenter(StatusBarPresenter.class, StatusBarPresenter.Display.class, StatusBar.class);
		
		bind(HasPageNavigation.class).to(TransUnitListEditorView.class).in(Singleton.class);

		// NB: if we bind directly to SeamDispatchAsync, we can't use replace-class in
		// the module definition unless the replacement extends SeamDispatchAsync
		bind(DispatchAsync.class).to(DelegatingDispatchAsync.class).in(Singleton.class);
		
		bind(Identity.class).to(IdentityImpl.class).in(Singleton.class);
	}

}
