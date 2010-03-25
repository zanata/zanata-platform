package org.fedorahosted.flies.webtrans.client;

import java.util.ArrayList;

import net.customware.gwt.dispatch.client.DispatchAsync;
import net.customware.gwt.presenter.client.EventBus;
import net.customware.gwt.presenter.client.place.Place;
import net.customware.gwt.presenter.client.place.PlaceRequest;
import net.customware.gwt.presenter.client.widget.WidgetDisplay;
import net.customware.gwt.presenter.client.widget.WidgetPresenter;

import org.fedorahosted.flies.common.TransUnitCount;
import org.fedorahosted.flies.gwt.auth.Identity;
import org.fedorahosted.flies.gwt.common.WorkspaceContext;
import org.fedorahosted.flies.gwt.model.DocumentInfo;
import org.fedorahosted.flies.gwt.model.DocumentStatus;
import org.fedorahosted.flies.gwt.rpc.GetProjectStatusCount;
import org.fedorahosted.flies.gwt.rpc.GetProjectStatusCountResult;
import org.fedorahosted.flies.webtrans.client.AppPresenter.Display.MainView;
import org.fedorahosted.flies.webtrans.client.events.TransUnitUpdatedEvent;
import org.fedorahosted.flies.webtrans.client.events.TransUnitUpdatedEventHandler;
import org.fedorahosted.flies.webtrans.client.rpc.CachingDispatchAsync;
import org.fedorahosted.flies.webtrans.client.ui.HasPager;
import org.fedorahosted.flies.webtrans.editor.HasTransUnitCount;
import org.fedorahosted.flies.webtrans.editor.filter.TransFilterPresenter;
import org.fedorahosted.flies.webtrans.editor.table.TableEditorPresenter;

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
		void setTransUnitNavigationView(Widget transUnitNavigation);
		void setTranslationMemoryView(Widget translationMemoryView);
		void setFilterView(Widget filterView);
		
		void showInMainView(MainView view);

		HasPager getTableEditorPager();
		void setTableEditorPagerVisible(boolean visible);
		void setTransUnitCountBarVisible(boolean visible);
		HasTransUnitCount getTransUnitCountBar();
		
		HasClickHandlers getSignOutLink();
		HasClickHandlers getLeaveWorkspaceLink();
		HasClickHandlers getHelpLink();
		HasClickHandlers getDocumentsLink();
		void setUserLabel(String userLabel);
		void setWorkspaceNameLabel(String workspaceNameLabel);
		void setSelectedDocument(DocumentInfo document);
	}

	private final TableEditorPresenter tableEditorPresenter;
	private final DocumentListPresenter documentListPresenter;
	private final TransUnitNavigationPresenter transUnitNavigationPresenter;
	private final TransMemoryPresenter transMemoryPresenter;
	private final TransFilterPresenter transFilterPresenter;
	private final SidePanelPresenter sidePanelPresenter;
	private final WorkspaceContext workspaceContext;
	private final DispatchAsync dispatcher;
	private final Identity identity;
	
	private final TransUnitCount projectCount = new TransUnitCount();
	private final WebTransMessages messages;

	@Inject
	public AppPresenter(Display display, EventBus eventBus,
			CachingDispatchAsync dispatcher,
			final TableEditorPresenter tableEditorPresenter,
			final DocumentListPresenter documentListPresenter,
			final TransMemoryPresenter transMemoryPresenter,
			final TransUnitNavigationPresenter transUnitNavigationPresenter,
			final TransFilterPresenter transFilterPresenter,
			final SidePanelPresenter sidePanelPresenter, 
			final Identity identity,
			final WorkspaceContext workspaceContext, 
			final WebTransMessages messages ) {
		super(display, eventBus);
		this.identity = identity;
		this.messages = messages;
		this.dispatcher = dispatcher;
		this.tableEditorPresenter = tableEditorPresenter;
		this.documentListPresenter = documentListPresenter;
		this.transUnitNavigationPresenter = transUnitNavigationPresenter;
		this.transMemoryPresenter = transMemoryPresenter;
		this.transFilterPresenter = transFilterPresenter;
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
						Widget center = tableEditorPresenter.getDisplay()
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
		tableEditorPresenter.bind();
		transUnitNavigationPresenter.bind();

		display.setDocumentListView(documentListPresenter.getDisplay()
				.asWidget());
		display.setEditorView(tableEditorPresenter.getDisplay().asWidget());
		display.setTransUnitNavigationView(transUnitNavigationPresenter
				.getDisplay().asWidget());

		registerHandler(eventBus.addHandler(DocumentSelectionEvent.getType(),
				new DocumentSelectionHandler() {
					@Override
					public void onDocumentSelected(DocumentSelectionEvent event) {
						display.showInMainView(MainView.Editor);
						display.setSelectedDocument(event.getDocument());
					}
				}));

		registerHandler(display.getTableEditorPager().addValueChangeHandler(
				new ValueChangeHandler<Integer>() {

					@Override
					public void onValueChange(ValueChangeEvent<Integer> event) {
						tableEditorPresenter.cancelEdit();
						tableEditorPresenter.gotoPage(event.getValue() - 1,
								false);
					}
				}));

		// TODO this uses incubator's HandlerRegistration
		tableEditorPresenter.addPageChangeHandler(new PageChangeHandler() {
			@Override
			public void onPageChange(PageChangeEvent event) {
				display.getTableEditorPager().setValue(event.getNewPage() + 1);
			}
		});

		// TODO this uses incubator's HandlerRegistration
		tableEditorPresenter
				.addPageCountChangeHandler(new PageCountChangeHandler() {
					@Override
					public void onPageCountChange(PageCountChangeEvent event) {
						display.getTableEditorPager().setPageCount(
								event.getNewPageCount());
					}
				});

		transMemoryPresenter.bind();
		display.setTranslationMemoryView(transMemoryPresenter.getDisplay()
				.asWidget());

		sidePanelPresenter.bind();

		transFilterPresenter.bind();
		display.setFilterView(transFilterPresenter.getDisplay().asWidget());

		display.showInMainView(MainView.Documents);
		
		registerHandler(eventBus.addHandler(TransUnitUpdatedEvent.getType(), new TransUnitUpdatedEventHandler() {
			@Override
			public void onTransUnitUpdated(TransUnitUpdatedEvent event) {
				projectCount.decrement(event.getPreviousStatus());
				projectCount.increment(event.getNewStatus());
				getDisplay().getTransUnitCountBar().setCount(projectCount);
			}
		}));
		
		registerHandler(display.getDocumentsLink().addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				display.showInMainView(MainView.Documents);
			}
		}));
		
		dispatcher.execute(new GetProjectStatusCount(), new AsyncCallback<GetProjectStatusCountResult>() {
			@Override
			public void onFailure(Throwable caught) {
			}
			@Override
			public void onSuccess(GetProjectStatusCountResult result) {
				ArrayList<DocumentStatus> liststatus = result.getStatus();
				for(DocumentStatus doc : liststatus) {
					projectCount.add(doc.getCount());
				}
				display.setTransUnitCountBarVisible(true);
				display.getTransUnitCountBar().setCount(projectCount);
				
			}
		});
		
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
		tableEditorPresenter.unbind();
		// TODO impl
	}

	@Override
	public void refreshDisplay() {
	}

	@Override
	public void revealDisplay() {
	}
}
