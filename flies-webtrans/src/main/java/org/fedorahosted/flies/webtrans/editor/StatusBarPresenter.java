package org.fedorahosted.flies.webtrans.editor;

import java.util.Locale;

import net.customware.gwt.dispatch.client.DispatchAsync;
import net.customware.gwt.presenter.client.EventBus;
import net.customware.gwt.presenter.client.place.Place;
import net.customware.gwt.presenter.client.place.PlaceRequest;
import net.customware.gwt.presenter.client.widget.WidgetDisplay;
import net.customware.gwt.presenter.client.widget.WidgetPresenter;

import org.fedorahosted.flies.gwt.model.DocumentId;
import org.fedorahosted.flies.gwt.model.LocaleId;
import org.fedorahosted.flies.gwt.rpc.GetStatusCount;
import org.fedorahosted.flies.gwt.rpc.GetStatusCountResult;
import org.fedorahosted.flies.webtrans.client.DocumentSelectionEvent;
import org.fedorahosted.flies.webtrans.client.DocumentSelectionHandler;
import org.fedorahosted.flies.webtrans.client.WorkspaceContext;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.widgetideas.client.ProgressBar;
import com.google.gwt.widgetideas.client.ProgressBar.TextFormatter;
import com.google.inject.Inject;

public class StatusBarPresenter extends WidgetPresenter<StatusBarPresenter.Display>{

	public static final Place PLACE = new Place("StatusBar");
	private final DispatchAsync dispatcher;	
	private final WorkspaceContext workspaceContext;
	int translated;
	int fuzzy;
	int untranslated;
		
	public interface Display extends WidgetDisplay {
		StatusBar getStatusBar();
	}

	@Inject
	public StatusBarPresenter(WorkspaceContext workspaceContext, final Display display, final EventBus eventBus, DispatchAsync dispatcher) {
		super(display, eventBus);
		this.dispatcher = dispatcher;
		this.workspaceContext = workspaceContext;
	}
	
	@Override
	public Place getPlace() {
		return PLACE;
	}

	@Override
	protected void onBind() {
		registerHandler(eventBus.addHandler(DocumentSelectionEvent.getType(), new DocumentSelectionHandler() {
			@Override
			public void onDocumentSelected(DocumentSelectionEvent event) {
				// TODO switch WebTransTableModel to the new document
				requestStatusCount(event.getDocumentId(), workspaceContext.getLocaleId());
			}
		}));
	}

	@Override
	protected void onPlaceRequest(PlaceRequest request) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void onUnbind() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void refreshDisplay() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void revealDisplay() {
		// TODO Auto-generated method stub
		
	}
	
	private void requestStatusCount(DocumentId id, LocaleId localeid) {
		dispatcher.execute(new GetStatusCount(id, localeid), new AsyncCallback<GetStatusCountResult>() {
			@Override
			public void onFailure(Throwable caught) {
			}
			@Override
			public void onSuccess(GetStatusCountResult result) {
				// TODO Auto-generated method stub
				display.getStatusBar().setStatus((int) result.getFuzzy(), (int)result.getTranslated(), (int)result.getUntranslated());
				display.getStatusBar().setProgressBar();
			}
		});
	}
}
