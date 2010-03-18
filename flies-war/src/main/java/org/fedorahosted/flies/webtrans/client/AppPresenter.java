package org.fedorahosted.flies.webtrans.client;

import java.util.ArrayList;

import net.customware.gwt.dispatch.client.DispatchAsync;
import net.customware.gwt.presenter.client.EventBus;
import net.customware.gwt.presenter.client.place.Place;
import net.customware.gwt.presenter.client.place.PlaceRequest;
import net.customware.gwt.presenter.client.widget.WidgetDisplay;
import net.customware.gwt.presenter.client.widget.WidgetPresenter;

import org.fedorahosted.flies.common.ContentState;
import org.fedorahosted.flies.common.LocaleId;
import org.fedorahosted.flies.common.TransUnitCount;
import org.fedorahosted.flies.gwt.model.DocumentStatus;
import org.fedorahosted.flies.gwt.model.ProjectContainerId;
import org.fedorahosted.flies.gwt.rpc.ActivateWorkspaceAction;
import org.fedorahosted.flies.gwt.rpc.ActivateWorkspaceResult;
import org.fedorahosted.flies.gwt.rpc.ExitWorkspaceAction;
import org.fedorahosted.flies.gwt.rpc.ExitWorkspaceResult;
import org.fedorahosted.flies.gwt.rpc.GetProjectStatusCount;
import org.fedorahosted.flies.gwt.rpc.GetProjectStatusCountResult;
import org.fedorahosted.flies.webtrans.client.AppPresenter.Display.MainView;
import org.fedorahosted.flies.webtrans.client.LoginPresenter.LoggedIn;
import org.fedorahosted.flies.webtrans.client.auth.Identity;
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
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.gen2.table.client.TableModel;
import com.google.gwt.gen2.table.event.client.PageChangeEvent;
import com.google.gwt.gen2.table.event.client.PageChangeHandler;
import com.google.gwt.gen2.table.event.client.PageCountChangeEvent;
import com.google.gwt.gen2.table.event.client.PageCountChangeHandler;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
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
	}

	private final TableEditorPresenter tableEditorPresenter;
	private final DocumentListPresenter documentListPresenter;
	private final EventProcessor eventProcessor;
	private final LoginPresenter loginPresenter;
	private final TransUnitNavigationPresenter transUnitNavigationPresenter;
	private final TransMemoryPresenter transMemoryPresenter;
	private final TransFilterPresenter transFilterPresenter;
	private final SidePanelPresenter sidePanelPresenter;

	private final DispatchAsync dispatcher;
	private String workspaceName;
	private String localeName;
	private final Identity identity;
	
	private final TransUnitCount projectCount = new TransUnitCount();

	@Inject
	public AppPresenter(Display display, EventBus eventBus,
			CachingDispatchAsync dispatcher,
			final TableEditorPresenter tableEditorPresenter,
			final DocumentListPresenter documentListPresenter,
			final EventProcessor eventProcessor,
			final LoginPresenter loginPresenter,
			final TransMemoryPresenter transMemoryPresenter,
			final TransUnitNavigationPresenter transUnitNavigationPresenter,
			final TransFilterPresenter transFilterPresenter,
			final SidePanelPresenter sidePanelPresenter, final Identity identity) {
		super(display, eventBus);
		this.identity = identity;
		this.dispatcher = dispatcher;
		this.tableEditorPresenter = tableEditorPresenter;
		this.eventProcessor = eventProcessor;
		this.loginPresenter = loginPresenter;
		this.documentListPresenter = documentListPresenter;
		this.transUnitNavigationPresenter = transUnitNavigationPresenter;
		this.transMemoryPresenter = transMemoryPresenter;
		this.transFilterPresenter = transFilterPresenter;
		this.sidePanelPresenter = sidePanelPresenter;
	}

	@Override
	public Place getPlace() {
		return null;
	}

	protected void bindApp() {

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

		// When user close the workspace, send ExitWorkSpaceAction
		Window.addCloseHandler(new CloseHandler<Window>() {
			@Override
			public void onClose(CloseEvent<Window> event) {
				dispatcher.execute(new ExitWorkspaceAction(
						findProjectContainerId(), findLocaleId(), identity
								.getPerson().getId()),
						new AsyncCallback<ExitWorkspaceResult>() {
							@Override
							public void onFailure(Throwable caught) {

							}

							@Override
							public void onSuccess(ExitWorkspaceResult result) {
								identity.invalidate();
							}

						});
			}
		});

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
		
//		dispatcher.execute(new GetProjectStatusCount(workspaceContext.getProjectContainerId(), workspaceContext.getLocaleId()), new AsyncCallback<GetProjectStatusCountResult>() {
//			@Override
//			public void onFailure(Throwable caught) {
//			}
//			@Override
//			public void onSuccess(GetProjectStatusCountResult result) {
//				ArrayList<DocumentStatus> liststatus = result.getStatus();
//				for(DocumentStatus doc : liststatus) {
////					projectCount.increment(doc.ge, count)
////					fuzzy =fuzzy+ doc.getFuzzy();
////					translated = translated + doc.getTranslated();
////					untranslated = untranslated + doc.getUntranslated();
//				}
//				getDisplay().setCount((int) fuzzy, (int)translated, (int)untranslated);
//			}
//		});
		
		
		
	}

	private static LocaleId findLocaleId() {
		String localeId = Window.Location.getParameter("localeId");
		return localeId == null ? null : new LocaleId(localeId);
	}

	private static ProjectContainerId findProjectContainerId() {
		String projContainerId = Window.Location
				.getParameter("projContainerId");
		if (projContainerId == null)
			return null;
		try {
			int id = Integer.parseInt(projContainerId);
			return new ProjectContainerId(id);
		} catch (NumberFormatException nfe) {
			return null;
		}
	}

	@Override
	protected void onBind() {
		loginPresenter.bind();
		loginPresenter.ensureLoggedIn(new LoggedIn() {
			@Override
			public void onSuccess() {
				AsyncCallback<Void> activateWorkspace = new AsyncCallback<Void>() {

					@Override
					public void onSuccess(Void result) {
						Log.info("AppPresenter ActivateWorkspace requested");

						dispatcher.execute(new ActivateWorkspaceAction(
								findProjectContainerId(), findLocaleId()),
								new AsyncCallback<ActivateWorkspaceResult>() {
									@Override
									public void onFailure(Throwable caught) {
										Log.info(caught.getMessage(), caught);
										Log
												.info("AppPresenter ActivateWorkspace failed, logging in...");
										loginPresenter.bind();
										loginPresenter
												.ensureLoggedIn(new LoggedIn() {
													@Override
													public void onSuccess() {
														// dispatcher.execute(new
														// ActivateWorkspaceAction(findProjectContainerId(),
														// findLocaleId()), new
														// AsyncCallback<ActivateWorkspaceResult>()
														// {
														// @Override
														// public void
														// onFailure(Throwable
														// caught) {
														// }
														// @Override
														// public void
														// onSuccess(ActivateWorkspaceResult
														// result) {
														// setWorkspaceName(result.getWorkspaceName());
														// setLocaleName(result.getLocaleName());
														bindApp();
														// }
														// });
													}
												});
									}

									@Override
									public void onSuccess(
											ActivateWorkspaceResult result) {
										Log
												.info("AppPresenter ActivateWorkspace - success");
										setWorkspaceName(result
												.getWorkspaceName());
										setLocaleName(result.getLocaleName());
										bindApp();
									}
								});
					}

					@Override
					public void onFailure(Throwable e) {
						Log.error(e.getMessage(), e);
					}
				};
				eventProcessor.addCallback(activateWorkspace);
				// activateWorkspace.onSuccess(null);
			}
		});

	}

	private void setWorkspaceName(String workspaceName) {
		this.workspaceName = workspaceName;
	}

	public String getWorkspaceName() {
		return workspaceName;
	}

	private void setLocaleName(String localeName) {
		this.localeName = localeName;
	}

	public String getLocaleName() {
		return localeName;
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
