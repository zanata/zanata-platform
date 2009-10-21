package org.fedorahosted.flies.webtrans.client.gin;

import net.customware.gwt.dispatch.client.DispatchAsync;
import net.customware.gwt.presenter.client.DefaultEventBus;
import net.customware.gwt.presenter.client.EventBus;
import net.customware.gwt.presenter.client.gin.AbstractPresenterModule;
import net.customware.gwt.presenter.client.place.PlaceManager;

import org.fedorahosted.flies.webtrans.client.AppPresenter;
import org.fedorahosted.flies.webtrans.client.DocumentListPresenter;
import org.fedorahosted.flies.webtrans.client.DocumentListView;
import org.fedorahosted.flies.webtrans.client.FlatFolderDocNameMapper;
import org.fedorahosted.flies.webtrans.client.LoginPanel;
import org.fedorahosted.flies.webtrans.client.LoginPresenter;
import org.fedorahosted.flies.webtrans.client.TransUnitInfoPresenter;
import org.fedorahosted.flies.webtrans.client.TransUnitInfoView;
import org.fedorahosted.flies.webtrans.client.WestNavigationPresenter;
import org.fedorahosted.flies.webtrans.client.WestNavigationView;
import org.fedorahosted.flies.webtrans.client.WorkspaceUsersPresenter;
import org.fedorahosted.flies.webtrans.client.WorkspaceUsersView;
import org.fedorahosted.flies.webtrans.client.rpc.DelegatingDispatchAsync;
import org.fedorahosted.flies.webtrans.client.ui.Pager;
import org.fedorahosted.flies.webtrans.client.ui.TreeNodeMapper;
import org.fedorahosted.flies.webtrans.editor.CachedWebTransTableModel;
import org.fedorahosted.flies.webtrans.editor.HasPageNavigation;
import org.fedorahosted.flies.webtrans.editor.TransUnitListPresenter;
import org.fedorahosted.flies.webtrans.editor.TransUnitTableDefinition;
import org.fedorahosted.flies.webtrans.editor.WebTransEditorFooter;
import org.fedorahosted.flies.webtrans.editor.WebTransEditorHeader;
import org.fedorahosted.flies.webtrans.editor.WebTransEditorPresenter;
import org.fedorahosted.flies.webtrans.editor.WebTransEditorView;
import org.fedorahosted.flies.webtrans.editor.WebTransScrollTable;
import org.fedorahosted.flies.webtrans.editor.WebTransTableModel;

import com.google.inject.Singleton;

public class WebTransClientModule extends AbstractPresenterModule {

	/**
	 * The Binding EDSL is described in {@link com.google.inject.Binder}
	 */
	@Override
	protected void configure() {		
		bind(EventBus.class).to(DefaultEventBus.class).in(Singleton.class);
		bind(PlaceManager.class).in(Singleton.class);
		
		bind(WebTransTableModel.class).in(Singleton.class);
		bind(CachedWebTransTableModel.class).in(Singleton.class);
		bind(TransUnitTableDefinition.class).in(Singleton.class);
		bind(WebTransScrollTable.class).in(Singleton.class);
		bind(WebTransEditorHeader.class).in(Singleton.class);
		bind(WebTransEditorFooter.class).in(Singleton.class);
		bind(Pager.class).in(Singleton.class);
		
		bindPresenter(DocumentListPresenter.class, DocumentListPresenter.Display.class, DocumentListView.class);
		bindPresenter(TransUnitInfoPresenter.class, TransUnitInfoPresenter.Display.class, TransUnitInfoView.class);
		bindPresenter(TransUnitListPresenter.class, TransUnitListPresenter.Display.class, WebTransScrollTable.class);
		bindPresenter(WestNavigationPresenter.class, WestNavigationPresenter.Display.class, WestNavigationView.class);
		bindPresenter(WorkspaceUsersPresenter.class, WorkspaceUsersPresenter.Display.class, WorkspaceUsersView.class);
		bindPresenter(WebTransEditorPresenter.class, WebTransEditorPresenter.Display.class, WebTransEditorView.class);
		bindPresenter(LoginPresenter.class, LoginPresenter.Display.class, LoginPanel.class);

		
		bind(HasPageNavigation.class).to(WebTransScrollTable.class).in(Singleton.class);
		bind(AppPresenter.class).in(Singleton.class);

		// NB: if we bind directly to SeamDispatchAsync, we can't use replace-class in
		// the module definition unless the replacement extends SeamDispatchAsync
		bind(DispatchAsync.class).to(DelegatingDispatchAsync.class).in(Singleton.class);
	}

}
