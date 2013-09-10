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

import java.util.List;

import net.customware.gwt.presenter.client.DefaultEventBus;
import net.customware.gwt.presenter.client.Display;
import net.customware.gwt.presenter.client.EventBus;
import net.customware.gwt.presenter.client.gin.AbstractPresenterModule;

import org.zanata.webtrans.client.Application;
import org.zanata.webtrans.client.EventProcessor;
import org.zanata.webtrans.client.events.NativeEvent;
import org.zanata.webtrans.client.events.NativeEventImpl;
import org.zanata.webtrans.client.history.History;
import org.zanata.webtrans.client.history.HistoryImpl;
import org.zanata.webtrans.client.history.Window;
import org.zanata.webtrans.client.history.WindowImpl;
import org.zanata.webtrans.client.history.WindowLocationImpl;
import org.zanata.webtrans.client.keys.EventWrapper;
import org.zanata.webtrans.client.keys.EventWrapperImpl;
import org.zanata.webtrans.client.keys.TimerFactory;
import org.zanata.webtrans.client.keys.TimerFactoryImpl;
import org.zanata.webtrans.client.presenter.AppPresenter;
import org.zanata.webtrans.client.presenter.AttentionKeyShortcutPresenter;
import org.zanata.webtrans.client.presenter.DocumentListOptionsPresenter;
import org.zanata.webtrans.client.presenter.DocumentListPresenter;
import org.zanata.webtrans.client.presenter.EditorOptionsPresenter;
import org.zanata.webtrans.client.presenter.ForceReviewCommentPresenter;
import org.zanata.webtrans.client.presenter.GlossaryDetailsPresenter;
import org.zanata.webtrans.client.presenter.GlossaryPresenter;
import org.zanata.webtrans.client.presenter.KeyShortcutPresenter;
import org.zanata.webtrans.client.presenter.NotificationPresenter;
import org.zanata.webtrans.client.presenter.OptionsPresenter;
import org.zanata.webtrans.client.presenter.SearchResultsPresenter;
import org.zanata.webtrans.client.presenter.SideMenuPresenter;
import org.zanata.webtrans.client.presenter.SourceContentsPresenter;
import org.zanata.webtrans.client.presenter.TransFilterPresenter;
import org.zanata.webtrans.client.presenter.TransMemoryDetailsPresenter;
import org.zanata.webtrans.client.presenter.TransMemoryMergePresenter;
import org.zanata.webtrans.client.presenter.TransMemoryPresenter;
import org.zanata.webtrans.client.presenter.TransUnitNavigationPresenter;
import org.zanata.webtrans.client.presenter.TransUnitsTablePresenter;
import org.zanata.webtrans.client.presenter.TranslationEditorPresenter;
import org.zanata.webtrans.client.presenter.TranslationPresenter;
import org.zanata.webtrans.client.presenter.ValidationOptionsPresenter;
import org.zanata.webtrans.client.presenter.WorkspaceUsersPresenter;
import org.zanata.webtrans.client.resources.Resources;
import org.zanata.webtrans.client.resources.WebTransMessages;
import org.zanata.webtrans.client.rpc.CachingDispatchAsync;
import org.zanata.webtrans.client.rpc.DelegatingDispatchAsync;
import org.zanata.webtrans.client.service.ValidationService;
import org.zanata.webtrans.client.ui.TransMemoryMergePopupPanelDisplay;
import org.zanata.webtrans.client.ui.TransMemoryMergePopupPanelView;
import org.zanata.webtrans.client.view.AppDisplay;
import org.zanata.webtrans.client.view.AppView;
import org.zanata.webtrans.client.view.AttentionKeyShortcutDisplay;
import org.zanata.webtrans.client.view.AttentionKeyShortcutView;
import org.zanata.webtrans.client.view.DocumentListDisplay;
import org.zanata.webtrans.client.view.DocumentListOptionsDisplay;
import org.zanata.webtrans.client.view.DocumentListOptionsView;
import org.zanata.webtrans.client.view.DocumentListView;
import org.zanata.webtrans.client.view.EditorOptionsDisplay;
import org.zanata.webtrans.client.view.EditorOptionsView;
import org.zanata.webtrans.client.view.GlossaryDetailsDisplay;
import org.zanata.webtrans.client.view.GlossaryDetailsView;
import org.zanata.webtrans.client.view.GlossaryDisplay;
import org.zanata.webtrans.client.view.GlossaryView;
import org.zanata.webtrans.client.view.KeyShortcutDisplay;
import org.zanata.webtrans.client.view.KeyShortcutView;
import org.zanata.webtrans.client.view.NotificationDisplay;
import org.zanata.webtrans.client.view.NotificationView;
import org.zanata.webtrans.client.view.OptionsDisplay;
import org.zanata.webtrans.client.view.OptionsView;
import org.zanata.webtrans.client.view.SearchResultsView;
import org.zanata.webtrans.client.view.SideMenuDisplay;
import org.zanata.webtrans.client.view.SideMenuView;
import org.zanata.webtrans.client.view.SourceContentsDisplay;
import org.zanata.webtrans.client.view.SourceContentsView;
import org.zanata.webtrans.client.view.TargetContentsDisplay;
import org.zanata.webtrans.client.view.TargetContentsView;
import org.zanata.webtrans.client.view.TransFilterDisplay;
import org.zanata.webtrans.client.view.TransFilterView;
import org.zanata.webtrans.client.view.TransMemoryDetailsDisplay;
import org.zanata.webtrans.client.view.TransMemoryDetailsView;
import org.zanata.webtrans.client.view.TransMemoryView;
import org.zanata.webtrans.client.view.TransUnitNavigationDisplay;
import org.zanata.webtrans.client.view.TransUnitNavigationView;
import org.zanata.webtrans.client.view.TransUnitsTableDisplay;
import org.zanata.webtrans.client.view.TransUnitsTableView;
import org.zanata.webtrans.client.view.TranslationEditorDisplay;
import org.zanata.webtrans.client.view.TranslationEditorView;
import org.zanata.webtrans.client.view.TranslationMemoryDisplay;
import org.zanata.webtrans.client.view.TranslationView;
import org.zanata.webtrans.client.view.ValidationOptionsDisplay;
import org.zanata.webtrans.client.view.ValidationOptionsView;
import org.zanata.webtrans.client.view.WorkspaceUsersDisplay;
import org.zanata.webtrans.client.view.WorkspaceUsersView;
import org.zanata.webtrans.shared.auth.Identity;
import org.zanata.webtrans.shared.model.UserWorkspaceContext;

import com.google.common.collect.ImmutableList;
import com.google.gwt.canvas.dom.client.CssColor;
import com.google.gwt.core.client.Scheduler;
import com.google.inject.Provider;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

import de.novanic.eventservice.client.event.RemoteEventService;
import de.novanic.eventservice.client.event.RemoteEventServiceFactory;
import org.zanata.webtrans.client.presenter.TransUnitChangeSourceLangPresenter;
import org.zanata.webtrans.client.view.TransUnitChangeSourceLangDisplay;
import org.zanata.webtrans.client.view.TransUnitChangeSourceLangView;

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

      bindPresenter(AppPresenter.class, AppDisplay.class, AppView.class);
      bindPresenter(AttentionKeyShortcutPresenter.class, AttentionKeyShortcutDisplay.class, AttentionKeyShortcutView.class);
      bindPresenter(KeyShortcutPresenter.class, KeyShortcutDisplay.class, KeyShortcutView.class);
      bindPresenter(DocumentListPresenter.class, DocumentListDisplay.class, DocumentListView.class);
      bindPresenter(SearchResultsPresenter.class, SearchResultsPresenter.Display.class, SearchResultsView.class);
      bindPresenter(TranslationPresenter.class, TranslationPresenter.Display.class, TranslationView.class);
      bindPresenter(TransFilterPresenter.class, TransFilterDisplay.class, TransFilterView.class);
      bindPresenter(WorkspaceUsersPresenter.class, WorkspaceUsersDisplay.class, WorkspaceUsersView.class);
      bindPresenter(TransMemoryPresenter.class, TranslationMemoryDisplay.class, TransMemoryView.class);
      bindPresenter(GlossaryPresenter.class, GlossaryDisplay.class, GlossaryView.class);
      bindPresenter(GlossaryDetailsPresenter.class, GlossaryDetailsDisplay.class, GlossaryDetailsView.class);
      bindPresenter(TransMemoryDetailsPresenter.class, TransMemoryDetailsDisplay.class, TransMemoryDetailsView.class);
      bindPresenter(TransUnitNavigationPresenter.class, TransUnitNavigationDisplay.class, TransUnitNavigationView.class);
      bindPresenter(EditorOptionsPresenter.class, EditorOptionsDisplay.class, EditorOptionsView.class);
      bindPresenter(DocumentListOptionsPresenter.class, DocumentListOptionsDisplay.class, DocumentListOptionsView.class);
      bindPresenter(OptionsPresenter.class, OptionsDisplay.class, OptionsView.class);
      bindPresenter(TranslationEditorPresenter.class, TranslationEditorDisplay.class, TranslationEditorView.class);
      bindPresenter(ValidationOptionsPresenter.class, ValidationOptionsDisplay.class, ValidationOptionsView.class);
      bindPresenter(NotificationPresenter.class, NotificationDisplay.class, NotificationView.class);
      bindPresenter(TransUnitsTablePresenter.class, TransUnitsTableDisplay.class, TransUnitsTableView.class);
      bindPresenter(SideMenuPresenter.class, SideMenuDisplay.class, SideMenuView.class);
      bind(ForceReviewCommentPresenter.class).asEagerSingleton();
      bindPresenter(TransUnitChangeSourceLangPresenter.class, TransUnitChangeSourceLangDisplay.class, TransUnitChangeSourceLangView.class);

      bind(SourceContentsPresenter.class).in(Singleton.class);
      bind(TargetContentsDisplay.class).to(TargetContentsView.class);
      bind(SourceContentsDisplay.class).to(SourceContentsView.class);
      bindPresenter(TransMemoryMergePresenter.class, TransMemoryMergePopupPanelDisplay.class, TransMemoryMergePopupPanelView.class);

      bind(EventWrapper.class).to(EventWrapperImpl.class).in(Singleton.class);
      bind(TimerFactory.class).to(TimerFactoryImpl.class).in(Singleton.class);
      bind(NativeEvent.class).to(NativeEventImpl.class).in(Singleton.class);
      bind(History.class).to(HistoryImpl.class).in(Singleton.class);
      bind(Window.class).to(WindowImpl.class).in(Singleton.class);
      bind(Window.Location.class).to(WindowLocationImpl.class).in(Singleton.class);

      // NB: if we bind directly to SeamDispatchAsync, we can't use
      // replace-class in
      // the module definition unless the replacement extends SeamDispatchAsync
      bind(CachingDispatchAsync.class).to(DelegatingDispatchAsync.class).in(Singleton.class);

      bind(Identity.class).toProvider(IdentityProvider.class).in(Singleton.class);
      bind(UserWorkspaceContext.class).toProvider(UserWorkspaceContextProvider.class).in(Singleton.class);
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

   static class UserWorkspaceContextProvider implements Provider<UserWorkspaceContext>
   {
      @Override
      public UserWorkspaceContext get()
      {
         return Application.getUserWorkspaceContext();
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

   /**
    * Referencing from http://eleanormaclure.files.wordpress.com/2011/03/colour-coding.pdf.
    * We store 22 visually distinct color and cycle through them.
    * If we ever get more than 22 concurrent users on one row, we will have two users share same color.
    *
    * @see org.zanata.webtrans.client.service.DistinctColorListImpl
    */
   @Provides
   @Named("distinctColor")
   public List<String> distinctColorList()
   {
      // @formatter:off
      return ImmutableList.<String>builder()
            .add(distinctColor(240, 163, 255))
            .add(distinctColor(0, 117, 220))
            .add(distinctColor(153, 63, 0))
            .add(distinctColor(76, 0, 92))
            .add(distinctColor(25, 25, 25))
            .add(distinctColor(0, 92, 49))
            .add(distinctColor(43, 206, 72))
            .add(distinctColor(255, 204, 153))
            .add(distinctColor(128, 128, 128))
            .add(distinctColor(148, 255, 181))
            .add(distinctColor(143, 124, 0))
            .add(distinctColor(157, 204, 0))
            .add(distinctColor(194, 0, 136))
            .add(distinctColor(0, 51, 128))
            .add(distinctColor(255, 164, 5))
            .add(distinctColor(66, 102, 0))
            .add(distinctColor(255, 0, 16))
            .add(distinctColor(94, 241, 242))
            .add(distinctColor(0, 153, 143))
            .add(distinctColor(224, 255, 102))
            .add(distinctColor(116, 10, 255))
            .add(distinctColor(153, 0, 0))
            .add(distinctColor(255, 255, 128))
            .add(distinctColor(255, 255, 0))
            .add(distinctColor(255, 80, 5))
            .build();
      // @formatter:on
   }

   private static String distinctColor(int rndRedColor, int rndGreenColor, int rndBlueColor)
   {
      return CssColor.make(rndRedColor, rndGreenColor, rndBlueColor).value();
   }

   /**
    * @see EventProcessor
    * @return RemoteEventService
    */
   @Provides
   public RemoteEventService removeEventService()
   {
      return RemoteEventServiceFactory.getInstance().getRemoteEventService();
   }
}
