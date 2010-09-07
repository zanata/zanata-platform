package net.openl10n.flies.webtrans.client;

import net.customware.gwt.dispatch.client.DispatchAsync;
import net.customware.gwt.presenter.client.EventBus;
import net.customware.gwt.presenter.client.place.Place;
import net.customware.gwt.presenter.client.place.PlaceRequest;
import net.customware.gwt.presenter.client.widget.WidgetDisplay;
import net.customware.gwt.presenter.client.widget.WidgetPresenter;
import net.openl10n.flies.webtrans.client.AppPresenter.Display.MainView;
import net.openl10n.flies.webtrans.client.editor.ListEditorPresenter;
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
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PopupPanel;
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

      void setEditorView(Widget editorView);

      void setSidePanel(Widget sidePanel);

      void showInMainView(MainView view);

      HasClickHandlers getSignOutLink();

      HasClickHandlers getLeaveWorkspaceLink();

      HasClickHandlers getHelpLink();

      HasClickHandlers getDocumentsLink();

      void setUserLabel(String userLabel);

      void setWorkspaceNameLabel(String workspaceNameLabel);

      void setSelectedDocument(DocumentInfo document);
   }

   private final DocumentListPresenter documentListPresenter;
   private final TranslationEditorPresenter translationEditorPresenter;
   private final SidePanelPresenter sidePanelPresenter;
   private final WorkspaceContext workspaceContext;
   private final DispatchAsync dispatcher;
   private final Identity identity;

   private final WebTransMessages messages;

   private DocumentId selectedDocument;

   @Inject
   public AppPresenter(Display display, EventBus eventBus, CachingDispatchAsync dispatcher, final ListEditorPresenter tableEditorPresenter, final TranslationEditorPresenter translationEditorPresenter, final DocumentListPresenter documentListPresenter, final TransUnitNavigationPresenter transUnitNavigationPresenter, final SidePanelPresenter sidePanelPresenter, final Identity identity, final WorkspaceContext workspaceContext, final WebTransMessages messages)
   {
      super(display, eventBus);
      this.identity = identity;
      this.messages = messages;
      this.dispatcher = dispatcher;
      this.documentListPresenter = documentListPresenter;
      this.translationEditorPresenter = translationEditorPresenter;
      this.sidePanelPresenter = sidePanelPresenter;
      this.workspaceContext = workspaceContext;
   }

   @Override
   public Place getPlace()
   {
      return null;
   }

   private PopupPanel existingPopup;
   
   @Override
   protected void onBind()
   {

      registerHandler(eventBus.addHandler(NotificationEvent.getType(), new NotificationEventHandler()
      {

         @Override
         public void onNotification(NotificationEvent event)
         {
            if(existingPopup != null) {
               existingPopup.hide();
               existingPopup = null;
            }
            PopupPanel popup = new PopupPanel(true);
            popup.addStyleDependentName("Notification");
            popup.addStyleName("Severity-" + event.getSeverity().name());
            Widget center = translationEditorPresenter.getDisplay().asWidget();
            popup.setWidth(center.getOffsetWidth() - 80 + "px");
            popup.setWidget(new Label(event.getMessage()));
            popup.setPopupPosition(center.getAbsoluteLeft() + 40, 0);
            existingPopup = popup;
            popup.show();
         }
      }));

      Window.enableScrolling(false);

      documentListPresenter.bind();

      display.setDocumentListView(documentListPresenter.getDisplay().asWidget());

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

      translationEditorPresenter.bind();
      display.setEditorView(translationEditorPresenter.getDisplay().asWidget());

      sidePanelPresenter.bind();
      display.setSidePanel(sidePanelPresenter.getDisplay().asWidget());

      display.showInMainView(MainView.Documents);

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
   protected void onPlaceRequest(PlaceRequest request)
   {
   }

   @Override
   protected void onUnbind()
   {
   }

   @Override
   public void refreshDisplay()
   {
   }

   @Override
   public void revealDisplay()
   {
   }
}
