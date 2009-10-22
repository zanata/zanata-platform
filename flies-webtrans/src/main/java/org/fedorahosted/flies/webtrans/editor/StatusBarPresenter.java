package org.fedorahosted.flies.webtrans.editor;

import net.customware.gwt.dispatch.client.DispatchAsync;
import net.customware.gwt.presenter.client.EventBus;
import net.customware.gwt.presenter.client.place.Place;
import net.customware.gwt.presenter.client.place.PlaceRequest;
import net.customware.gwt.presenter.client.widget.WidgetDisplay;
import net.customware.gwt.presenter.client.widget.WidgetPresenter;

import org.fedorahosted.flies.gwt.rpc.GetStatusCount;
import org.fedorahosted.flies.gwt.rpc.GetStatusCountResult;
import org.fedorahosted.flies.webtrans.client.DocumentSelectionEvent;
import org.fedorahosted.flies.webtrans.client.DocumentSelectionHandler;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;

public class StatusBarPresenter extends WidgetPresenter<StatusBarPresenter.Display>{

	public static final Place PLACE = new Place("StatusBar");
	private final DispatchAsync dispatcher;	
	
	public interface Display extends WidgetDisplay {
		StatusBar getStatusBar();
	}

	@Inject
	public StatusBarPresenter(final Display display, final EventBus eventBus, DispatchAsync dispatcher) {
		super(display, eventBus);
		this.dispatcher = dispatcher;
		requestStatusCount();
	}
	
	@Override
	public Place getPlace() {
		return PLACE;
	}

	@Override
	protected void onBind() {
		// TODO Auto-generated method stub
		registerHandler(eventBus.addHandler(DocumentSelectionEvent.getType(), new DocumentSelectionHandler() {
			@Override
			public void onDocumentSelected(DocumentSelectionEvent event) {
				// TODO switch WebTransTableModel to the new document
				requestStatusCount();
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
	
	private void requestStatusCount() {
		dispatcher.execute(new GetStatusCount(), new AsyncCallback<GetStatusCountResult>() {
			@Override
			public void onFailure(Throwable caught) {
				
			}
			@Override
			public void onSuccess(GetStatusCountResult result) {
				// TODO Auto-generated method stub
				display.getStatusBar().setFuzzy((int)result.getFuzzy());
				display.getStatusBar().setTranslated((int)result.getTranslated());
				display.getStatusBar().setUntranslated((int)result.getUntranslated());
			}
		});
	}
}
