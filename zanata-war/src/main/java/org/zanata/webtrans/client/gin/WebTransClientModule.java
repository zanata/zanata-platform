/*
 * Copyright 2010, Red Hat, Inc. and individual contributors as indicated by the
 * @author tags. See the copyright.txt file in the distribution for a full
 * listing of individual contributors.
 * 
 * This is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 * 
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this software; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
 * site: http://www.fsf.org.
 */
package org.zanata.webtrans.client.gin;

import com.google.gwt.core.client.Scheduler;
import com.google.inject.Provides;
import net.customware.gwt.presenter.client.DefaultEventBus;
import net.customware.gwt.presenter.client.Display;
import net.customware.gwt.presenter.client.EventBus;
import net.customware.gwt.presenter.client.gin.AbstractPresenterModule;

import org.zanata.webtrans.client.Application;
import org.zanata.webtrans.client.EventProcessor;
import org.zanata.webtrans.client.editor.HasPageNavigation;
import org.zanata.webtrans.client.editor.filter.TransFilterPresenter;
import org.zanata.webtrans.client.editor.filter.TransFilterView;
import org.zanata.webtrans.client.editor.table.SourceContentsDisplay;
import org.zanata.webtrans.client.editor.table.SourceContentsView;
import org.zanata.webtrans.client.editor.table.TableEditorPresenter;
import org.zanata.webtrans.client.editor.table.TableEditorView;
import org.zanata.webtrans.client.editor.table.TargetContentsDisplay;
import org.zanata.webtrans.client.editor.table.TargetContentsView;
import org.zanata.webtrans.client.events.NativeEvent;
import org.zanata.webtrans.client.events.NativeEventImpl;
import org.zanata.webtrans.client.history.History;
import org.zanata.webtrans.client.history.HistoryImpl;
import org.zanata.webtrans.client.history.Window;
import org.zanata.webtrans.client.history.WindowImpl;
import org.zanata.webtrans.client.history.WindowLocationImpl;
import org.zanata.webtrans.client.presenter.AppPresenter;
import org.zanata.webtrans.client.presenter.DocumentListPresenter;
import org.zanata.webtrans.client.presenter.GlossaryDetailsPresenter;
import org.zanata.webtrans.client.presenter.GlossaryPresenter;
import org.zanata.webtrans.client.presenter.OptionsPanelPresenter;
import org.zanata.webtrans.client.presenter.PrefillPresenter;
import org.zanata.webtrans.client.presenter.SearchResultsPresenter;
import org.zanata.webtrans.client.presenter.SourceContentsPresenter;
import org.zanata.webtrans.client.presenter.TransMemoryDetailsPresenter;
import org.zanata.webtrans.client.presenter.TransMemoryPresenter;
import org.zanata.webtrans.client.presenter.TransUnitNavigationPresenter;
import org.zanata.webtrans.client.presenter.TranslationEditorPresenter;
import org.zanata.webtrans.client.presenter.TranslationPresenter;
import org.zanata.webtrans.client.presenter.ValidationOptionsPresenter;
import org.zanata.webtrans.client.presenter.WorkspaceUsersPresenter;
import org.zanata.webtrans.client.resources.Resources;
import org.zanata.webtrans.client.resources.WebTransMessages;
import org.zanata.webtrans.client.rpc.CachingDispatchAsync;
import org.zanata.webtrans.client.rpc.DelegatingDispatchAsync;
import org.zanata.webtrans.client.service.ValidationService;
import org.zanata.webtrans.client.ui.OptionsPanelView;
import org.zanata.webtrans.client.ui.PrefillPopupPanelDisplay;
import org.zanata.webtrans.client.ui.PrefillPopupPanelView;
import org.zanata.webtrans.client.ui.ValidationMessagePanelDisplay;
import org.zanata.webtrans.client.ui.ValidationMessagePanelView;
import org.zanata.webtrans.client.view.AppView;
import org.zanata.webtrans.client.view.DocumentListView;
import org.zanata.webtrans.client.view.GlossaryDetailsView;
import org.zanata.webtrans.client.view.GlossaryView;
import org.zanata.webtrans.client.view.SearchResultsView;
import org.zanata.webtrans.client.view.TransMemoryDetailsView;
import org.zanata.webtrans.client.view.TransMemoryView;
import org.zanata.webtrans.client.view.TransUnitNavigationView;
import org.zanata.webtrans.client.view.TranslationEditorView;
import org.zanata.webtrans.client.view.TranslationView;
import org.zanata.webtrans.client.view.ValidationOptionsView;
import org.zanata.webtrans.client.view.WorkspaceUsersView;
import org.zanata.webtrans.shared.auth.Identity;
import org.zanata.webtrans.shared.model.WorkspaceContext;

import com.google.inject.Provider;
import com.google.inject.Singleton;

public class WebTransClientModule extends AbstractPresenterModule
{

   /**
    * The Binding EDSL is described in {@link com.google.inject.Binder}
    */
   @Override
   protected void configure()
   {
      bind(EventBus.class).to(DefaultEventBus.class).in(Singleton.class);
      bind(EventProcessor.class).in(Singleton.class);
      bind(Resources.class).in(Singleton.class);
      bind(WebTransMessages.class).in(Singleton.class);
      bind(ValidationService.class).in(Singleton.class);

      bindPresenter(AppPresenter.class, AppPresenter.Display.class, AppView.class);
      bindPresenter(DocumentListPresenter.class, DocumentListPresenter.Display.class, DocumentListView.class);
      bindPresenter(SearchResultsPresenter.class, SearchResultsPresenter.Display.class, SearchResultsView.class);
      bindPresenter(TranslationPresenter.class, TranslationPresenter.Display.class, TranslationView.class);
      bindPresenter(TransFilterPresenter.class, TransFilterPresenter.Display.class, TransFilterView.class);
      bindPresenter(TableEditorPresenter.class, TableEditorPresenter.Display.class, TableEditorView.class);
      bindPresenter(WorkspaceUsersPresenter.class, WorkspaceUsersPresenter.Display.class, WorkspaceUsersView.class);
      bindPresenter(TransMemoryPresenter.class, TransMemoryPresenter.Display.class, TransMemoryView.class);
      bindPresenter(GlossaryPresenter.class, GlossaryPresenter.Display.class, GlossaryView.class);
      bindPresenter(GlossaryDetailsPresenter.class, GlossaryDetailsPresenter.Display.class, GlossaryDetailsView.class);
      bindPresenter(TransMemoryDetailsPresenter.class, TransMemoryDetailsPresenter.Display.class, TransMemoryDetailsView.class);
      bindPresenter(TransUnitNavigationPresenter.class, TransUnitNavigationPresenter.Display.class, TransUnitNavigationView.class);
      bindPresenter(OptionsPanelPresenter.class, OptionsPanelPresenter.Display.class, OptionsPanelView.class);
      bindPresenter(TranslationEditorPresenter.class, TranslationEditorPresenter.Display.class, TranslationEditorView.class);
      bindPresenter(ValidationOptionsPresenter.class, ValidationOptionsPresenter.Display.class, ValidationOptionsView.class);

      bind(SourceContentsPresenter.class).in(Singleton.class);
      bind(TargetContentsDisplay.class).to(TargetContentsView.class);
      bind(SourceContentsDisplay.class).to(SourceContentsView.class);
      bind(ValidationMessagePanelDisplay.class).to(ValidationMessagePanelView.class).in(Singleton.class);
      bindPresenter(PrefillPresenter.class, PrefillPopupPanelDisplay.class, PrefillPopupPanelView.class);

      bind(HasPageNavigation.class).to(TableEditorView.class).in(Singleton.class);
      bind(NativeEvent.class).to(NativeEventImpl.class).in(Singleton.class);
      bind(History.class).to(HistoryImpl.class).in(Singleton.class);
      bind(Window.class).to(WindowImpl.class).in(Singleton.class);
      bind(Window.Location.class).to(WindowLocationImpl.class).in(Singleton.class);

      // NB: if we bind directly to SeamDispatchAsync, we can't use
      // replace-class in
      // the module definition unless the replacement extends SeamDispatchAsync
      bind(CachingDispatchAsync.class).to(DelegatingDispatchAsync.class).in(Singleton.class);

      bind(Identity.class).toProvider(IdentityProvider.class).in(Singleton.class);
      bind(WorkspaceContext.class).toProvider(WorkspaceContextProvider.class).in(Singleton.class);
   }

   // default implementation doesn't use singleton display binding, adding here
   // to allow displays to be injected into other displays
   @Override
   protected <D extends Display> void bindDisplay(Class<D> display, Class<? extends D> displayImpl)
   {
      bind(display).to(displayImpl).in(Singleton.class);
   }

   @Provides
   public Scheduler provideScheduler()
   {
      return Scheduler.get();
   }

   static class WorkspaceContextProvider implements Provider<WorkspaceContext>
   {
      @Override
      public WorkspaceContext get()
      {
         return Application.getWorkspaceContext();
      }
   }

   static class IdentityProvider implements Provider<Identity>
   {
      @Override
      public Identity get()
      {
         return Application.getIdentity();
      }
   }

}
