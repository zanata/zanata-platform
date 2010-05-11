package org.fedorahosted.flies.webtrans.client;

import java.util.ArrayList;

import net.customware.gwt.dispatch.client.DispatchAsync;
import net.customware.gwt.presenter.client.EventBus;
import net.customware.gwt.presenter.client.place.Place;
import net.customware.gwt.presenter.client.place.PlaceRequest;
import net.customware.gwt.presenter.client.widget.WidgetDisplay;
import net.customware.gwt.presenter.client.widget.WidgetPresenter;

import org.fedorahosted.flies.common.TransUnitCount;
import org.fedorahosted.flies.webtrans.client.AppPresenter.Display.MainView;
import org.fedorahosted.flies.webtrans.client.editor.HasTransUnitCount;
import org.fedorahosted.flies.webtrans.client.editor.filter.TransFilterPresenter;
import org.fedorahosted.flies.webtrans.client.editor.table.TableEditorPresenter;
import org.fedorahosted.flies.webtrans.client.events.DocumentSelectionEvent;
import org.fedorahosted.flies.webtrans.client.events.DocumentSelectionHandler;
import org.fedorahosted.flies.webtrans.client.events.NotificationEvent;
import org.fedorahosted.flies.webtrans.client.events.NotificationEventHandler;
import org.fedorahosted.flies.webtrans.client.events.TransUnitUpdatedEvent;
import org.fedorahosted.flies.webtrans.client.events.TransUnitUpdatedEventHandler;
import org.fedorahosted.flies.webtrans.client.rpc.CachingDispatchAsync;
import org.fedorahosted.flies.webtrans.client.ui.HasPager;
import org.fedorahosted.flies.webtrans.shared.auth.Identity;
import org.fedorahosted.flies.webtrans.shared.common.WorkspaceContext;
import org.fedorahosted.flies.webtrans.shared.model.DocumentId;
import org.fedorahosted.flies.webtrans.shared.model.DocumentInfo;
import org.fedorahosted.flies.webtrans.shared.model.DocumentStatus;
import org.fedorahosted.flies.webtrans.shared.rpc.GetProjectStatusCount;
import org.fedorahosted.flies.webtrans.shared.rpc.GetProjectStatusCountResult;

import com.allen_sauer.gwt.log.client.Log;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.gen2.table.event.client.PageChangeEvent;
import com.google.gwt.gen2.table.event.client.PageChangeHandler;
import com.google.gwt.gen2.table.event.client.PageCountChangeEvent;
import com.google.gwt.gen2.table.event.client.PageCountChangeHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class AppPresenter extends WidgetPresenter<AppPresenter.Display> {

	public interface Display extends WidgetDisplay {
		
		enum MainView{
			Documents,
			Editor;
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
	public AppPresenter(Display display, EventBus eventBus,
			CachingDispatchAsync dispatcher,
			final TableEditorPresenter tableEditorPresenter,
			final TranslationEditorPresenter translationEditorPresenter,
			final DocumentListPresenter documentListPresenter,
			final TransUnitNavigationPresenter transUnitNavigationPresenter,
			final SidePanelPresenter sidePanelPresenter, 
			final Identity identity,
			final WorkspaceContext workspaceContext, 
			final WebTransMessages messages ) {
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
	public Place getPlace() {
		return null;
	}

	@Override
	protected void onBind() {

		registerHandler(eventBus.addHandler(NotificationEvent.getType(),
				new NotificationEventHandler() {

					@Override
					public void onNotification(NotificationEvent event) {
						PopupPanel popup = new PopupPanel(true);
						popup.addStyleDependentName("Notification");
						popup.addStyleName("Severity-"
								+ event.getSeverity().name());
						Widget center = translationEditorPresenter.getDisplay()
								.asWidget();
						popup.setWidth(center.getOffsetWidth() - 40 + "px");
						popup.setWidget(new Label(event.getMessage()));
						popup.setPopupPosition(center.getAbsoluteLeft() + 20,
								center.getAbsoluteTop() + 30);
						popup.show();
					}
				}));

		Window.enableScrolling(false);

		documentListPresenter.bind();

		display.setDocumentListView(documentListPresenter.getDisplay()
				.asWidget());

		registerHandler(eventBus.addHandler(DocumentSelectionEvent.getType(),
				new DocumentSelectionHandler() {
					@Override
					public void onDocumentSelected(DocumentSelectionEvent event) {
						if(selectedDocument == null || !event.getDocument().getId().equals(selectedDocument)) {
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
		
		registerHandler(display.getDocumentsLink().addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				display.showInMainView(MainView.Documents);
			}
		}));
		
		
		registerHandler( display.getSignOutLink().addClickHandler( new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				Application.redirectToLogout();
			}
		}));
		
		registerHandler( display.getLeaveWorkspaceLink().addClickHandler( new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				Application.redirectToFliesProjectHome(workspaceContext.getWorkspaceId());
			}
		}));
		
		display.setUserLabel(identity.getPerson().getName());
		
		display.setWorkspaceNameLabel(workspaceContext.getWorkspaceName());
		
		Window.setTitle( messages.windowTitle(workspaceContext.getWorkspaceName(), workspaceContext.getLocaleName()));
	}

	@Override
	protected void onPlaceRequest(PlaceRequest request) {
	}

	@Override
	protected void onUnbind() {
	}

	@Override
	public void refreshDisplay() {
	}

	@Override
	public void revealDisplay() {
	}
}
