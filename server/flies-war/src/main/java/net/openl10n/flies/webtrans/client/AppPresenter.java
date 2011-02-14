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
package net.openl10n.flies.webtrans.client;

import net.customware.gwt.presenter.client.EventBus;
import net.customware.gwt.presenter.client.widget.WidgetDisplay;
import net.customware.gwt.presenter.client.widget.WidgetPresenter;
import net.openl10n.flies.webtrans.client.AppPresenter.Display.MainView;
import net.openl10n.flies.webtrans.client.editor.filter.TransFilterPresenter;
import net.openl10n.flies.webtrans.client.events.DocumentSelectionEvent;
import net.openl10n.flies.webtrans.client.events.DocumentSelectionHandler;
import net.openl10n.flies.webtrans.client.events.NotificationEvent;
import net.openl10n.flies.webtrans.client.events.NotificationEventHandler;
import net.openl10n.flies.webtrans.client.rpc.CachingDispatchAsync;
import net.openl10n.flies.webtrans.shared.auth.Identity;
import net.openl10n.flies.webtrans.shared.model.DocumentId;
import net.openl10n.flies.webtrans.shared.model.DocumentInfo;
import net.openl10n.flies.webtrans.shared.model.WorkspaceContext;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class AppPresenter extends WidgetPresenter<AppPresenter.Display>
{

   public interface Display extends WidgetDisplay
   {
      enum MainView
      {
         Documents, Editor;
      }

      void setDocumentListView(Widget documentListView);

      void setTranslationView(Widget translationView);

      void setFilterView(Widget filterView);

      void showInMainView(MainView editor);

      HasClickHandlers getSignOutLink();

      HasClickHandlers getLeaveWorkspaceLink();

      HasClickHandlers getHelpLink();

      HasClickHandlers getDocumentsLink();

      void setUserLabel(String userLabel);

      void setWorkspaceNameLabel(String workspaceNameLabel);

      void setSelectedDocument(DocumentInfo document);

      void setNotificationMessage(String var);
   }

   private final DocumentListPresenter documentListPresenter;
   private final TranslationPresenter translationPresenter;
   private final TransFilterPresenter transFilterPresenter;
   private final WorkspaceContext workspaceContext;
   private final Identity identity;

   private final WebTransMessages messages;

   private DocumentId selectedDocument;

   @Inject
   public AppPresenter(Display display, EventBus eventBus, CachingDispatchAsync dispatcher, final TranslationPresenter translationPresenter, final DocumentListPresenter documentListPresenter, final TransFilterPresenter transFilterPresenter, final Identity identity, final WorkspaceContext workspaceContext, final WebTransMessages messages)
   {
      super(display, eventBus);
      this.identity = identity;
      this.messages = messages;
      this.documentListPresenter = documentListPresenter;
      this.translationPresenter = translationPresenter;
      this.transFilterPresenter = transFilterPresenter;
      this.workspaceContext = workspaceContext;
   }

   @Override
   protected void onBind()
   {

      registerHandler(eventBus.addHandler(NotificationEvent.getType(), new NotificationEventHandler()
      {

         @Override
         public void onNotification(NotificationEvent event)
         {
            display.setNotificationMessage(event.getMessage());
         }
      }));

      Window.enableScrolling(false);

      documentListPresenter.bind();
      translationPresenter.bind();
      transFilterPresenter.bind();

      display.setDocumentListView(documentListPresenter.getDisplay().asWidget());
      display.setTranslationView(translationPresenter.getDisplay().asWidget());
      display.setFilterView(transFilterPresenter.getDisplay().asWidget());

      display.showInMainView(MainView.Documents);


      registerHandler(eventBus.addHandler(DocumentSelectionEvent.getType(), new DocumentSelectionHandler()
      {
         @Override
         public void onDocumentSelected(DocumentSelectionEvent event)
         {
            if (selectedDocument == null || !event.getDocument().getId().equals(selectedDocument))
            {
               display.setSelectedDocument(event.getDocument());
            }
            display.showInMainView(MainView.Editor);
         }
      }));


      registerHandler(display.getDocumentsLink().addClickHandler(new ClickHandler()
      {
         @Override
         public void onClick(ClickEvent event)
         {
            display.showInMainView(MainView.Documents);
         }
      }));

      registerHandler(display.getSignOutLink().addClickHandler(new ClickHandler()
      {
         @Override
         public void onClick(ClickEvent event)
         {
            Application.redirectToLogout();
         }
      }));

      registerHandler(display.getLeaveWorkspaceLink().addClickHandler(new ClickHandler()
      {
         @Override
         public void onClick(ClickEvent event)
         {
            Application.closeWindow();
            // As the editor was created at new window, it should be closed
            // rather than redirected to project home.
            // Application.redirectToFliesProjectHome(workspaceContext.getWorkspaceId());
         }
      }));

      display.setUserLabel(identity.getPerson().getName());

      display.setWorkspaceNameLabel(workspaceContext.getWorkspaceName());

      Window.setTitle(messages.windowTitle(workspaceContext.getWorkspaceName(), workspaceContext.getLocaleName()));
   }

   @Override
   protected void onUnbind()
   {
   }

   @Override
   public void onRevealDisplay()
   {
   }
}
