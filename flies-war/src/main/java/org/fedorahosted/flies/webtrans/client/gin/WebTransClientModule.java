package org.fedorahosted.flies.webtrans.client.gin;

import net.customware.gwt.presenter.client.DefaultEventBus;
import net.customware.gwt.presenter.client.EventBus;
import net.customware.gwt.presenter.client.gin.AbstractPresenterModule;
import net.customware.gwt.presenter.client.place.PlaceManager;

import org.fedorahosted.flies.webtrans.client.AppPresenter;
import org.fedorahosted.flies.webtrans.client.AppView;
import org.fedorahosted.flies.webtrans.client.DocumentListPresenter;
import org.fedorahosted.flies.webtrans.client.DocumentListView;
import org.fedorahosted.flies.webtrans.client.EventProcessor;
import org.fedorahosted.flies.webtrans.client.GlossaryPresenter;
import org.fedorahosted.flies.webtrans.client.GlossaryView;
import org.fedorahosted.flies.webtrans.client.LoginView;
import org.fedorahosted.flies.webtrans.client.LoginPresenter;
import org.fedorahosted.flies.webtrans.client.SouthPresenter;
import org.fedorahosted.flies.webtrans.client.SouthView;
import org.fedorahosted.flies.webtrans.client.TransMemoryPresenter;
import org.fedorahosted.flies.webtrans.client.TransMemoryView;
import org.fedorahosted.flies.webtrans.client.TransUnitNavigationPresenter;
import org.fedorahosted.flies.webtrans.client.TransUnitNavigationView;
import org.fedorahosted.flies.webtrans.client.WorkspaceUsersPresenter;
import org.fedorahosted.flies.webtrans.client.WorkspaceUsersView;
import org.fedorahosted.flies.webtrans.client.auth.Identity;
import org.fedorahosted.flies.webtrans.client.auth.IdentityImpl;
import org.fedorahosted.flies.webtrans.client.rpc.CachingDispatchAsync;
import org.fedorahosted.flies.webtrans.client.rpc.DelegatingDispatchAsync;
import org.fedorahosted.flies.webtrans.editor.DocumentStatusPresenter;
import org.fedorahosted.flies.webtrans.editor.HasPageNavigation;
import org.fedorahosted.flies.webtrans.editor.ProjectStatusPresenter;
import org.fedorahosted.flies.webtrans.editor.TranslationStatsBar;
import org.fedorahosted.flies.webtrans.editor.TranslationStatsBarPresenter;
import org.fedorahosted.flies.webtrans.editor.WebTransEditorPresenter;
import org.fedorahosted.flies.webtrans.editor.WebTransEditorView;
import org.fedorahosted.flies.webtrans.editor.filter.OperatorFilterPresenter;
import org.fedorahosted.flies.webtrans.editor.filter.OperatorFilterView;
import org.fedorahosted.flies.webtrans.editor.filter.PhraseFilterPresenter;
import org.fedorahosted.flies.webtrans.editor.filter.PhraseFilterView;
import org.fedorahosted.flies.webtrans.editor.filter.TransFilterPresenter;
import org.fedorahosted.flies.webtrans.editor.filter.TransFilterView;
import org.fedorahosted.flies.webtrans.editor.table.TableEditorPresenter;
import org.fedorahosted.flies.webtrans.editor.table.TableEditorView;

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
		
		bindPresenter(AppPresenter.class, AppPresenter.Display.class, AppView.class);
		bindPresenter(DocumentListPresenter.class, DocumentListPresenter.Display.class, DocumentListView.class);
		bindPresenter(PhraseFilterPresenter.class, PhraseFilterPresenter.Display.class, PhraseFilterView.class);
		bindPresenter(OperatorFilterPresenter.class, OperatorFilterPresenter.Display.class, OperatorFilterView.class);
		bindPresenter(TransFilterPresenter.class, TransFilterPresenter.Display.class, TransFilterView.class);
		bindPresenter(TableEditorPresenter.class, TableEditorPresenter.Display.class, TableEditorView.class);
		bindPresenter(SouthPresenter.class, SouthPresenter.Display.class, SouthView.class);
		bindPresenter(WorkspaceUsersPresenter.class, WorkspaceUsersPresenter.Display.class, WorkspaceUsersView.class);
		bindPresenter(WebTransEditorPresenter.class, WebTransEditorPresenter.Display.class, WebTransEditorView.class);
		bindPresenter(LoginPresenter.class, LoginPresenter.Display.class, LoginView.class);
		bindPresenter(TransMemoryPresenter.class, TransMemoryPresenter.Display.class, TransMemoryView.class);
		bindPresenter(GlossaryPresenter.class, GlossaryPresenter.Display.class, GlossaryView.class);
		bindPresenter(TransUnitNavigationPresenter.class, TransUnitNavigationPresenter.Display.class, TransUnitNavigationView.class);
		bind(DocumentStatusPresenter.class);
		
		bind(ProjectStatusPresenter.class);
		bindDisplay(TranslationStatsBarPresenter.Display.class, TranslationStatsBar.class);
		
		bind(HasPageNavigation.class).to(TableEditorView.class).in(Singleton.class);

		// NB: if we bind directly to SeamDispatchAsync, we can't use replace-class in
		// the module definition unless the replacement extends SeamDispatchAsync
		bind(CachingDispatchAsync.class).to(DelegatingDispatchAsync.class).in(Singleton.class);
		
		bind(Identity.class).to(IdentityImpl.class).in(Singleton.class);
	}

}
