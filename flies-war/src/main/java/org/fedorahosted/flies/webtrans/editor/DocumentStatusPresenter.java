package org.fedorahosted.flies.webtrans.editor;

import net.customware.gwt.dispatch.client.DispatchAsync;
import net.customware.gwt.presenter.client.EventBus;
import net.customware.gwt.presenter.client.place.Place;
import net.customware.gwt.presenter.client.place.PlaceRequest;

import org.fedorahosted.flies.common.ContentState;
import org.fedorahosted.flies.gwt.common.WorkspaceContext;
import org.fedorahosted.flies.gwt.model.DocumentId;
import org.fedorahosted.flies.gwt.rpc.GetStatusCount;
import org.fedorahosted.flies.gwt.rpc.GetStatusCountResult;
import org.fedorahosted.flies.webtrans.client.DocumentSelectionEvent;
import org.fedorahosted.flies.webtrans.client.DocumentSelectionHandler;
import org.fedorahosted.flies.webtrans.client.events.TransUnitUpdatedEvent;
import org.fedorahosted.flies.webtrans.client.events.TransUnitUpdatedEventHandler;
import org.fedorahosted.flies.webtrans.client.rpc.CachingDispatchAsync;

import com.allen_sauer.gwt.log.client.Log;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;

public class DocumentStatusPresenter extends TranslationStatsBarPresenter {
	
	private final WorkspaceContext workspaceContext;
	private final DispatchAsync dispatcher;
	private DocumentId documentid;
	private HandlerRegistration updateHandlerRegistration;
	
	@Inject
	public DocumentStatusPresenter(Display display, EventBus eventBus,
			CachingDispatchAsync dispatcher,
			WorkspaceContext workspaceContext) {
		super(display, eventBus);
		this.dispatcher = dispatcher;
        this.workspaceContext = workspaceContext;
	}

	@Override
	public Place getPlace() {
		return null;
	}

	@Override
	protected void onBind() {
		registerHandler(eventBus.addHandler(DocumentSelectionEvent.getType(), new DocumentSelectionHandler() {
			@Override
			public void onDocumentSelected(DocumentSelectionEvent event) {
				requestStatusCount(event.getDocument().getId());
			}
		}));
	}

	@Override
	protected void onPlaceRequest(PlaceRequest request) {
		
	}

	@Override
	protected void onUnbind() {
		if(updateHandlerRegistration != null)
			updateHandlerRegistration.removeHandler();
	}

	@Override
	public void refreshDisplay() {
	
	}

	@Override
	public void revealDisplay() {
		
	}
	
	private void requestStatusCount(final DocumentId newDocumentId) {
		if(updateHandlerRegistration != null) {
			updateHandlerRegistration.removeHandler();
		}
		documentid  = newDocumentId;
		dispatcher.execute(new GetStatusCount(newDocumentId), new AsyncCallback<GetStatusCountResult>() {
			@Override
			public void onFailure(Throwable caught) {
				Log.error("error fetching GetStatusCount: " + caught.getMessage());
			}
			@Override
			public void onSuccess(GetStatusCountResult result) {
				getDisplay().setCount(result.getCount());
				updateHandlerRegistration = eventBus.addHandler(TransUnitUpdatedEvent.getType(), updateHandler);
				// TODO move this registration to before getting the count
			}
	});
	}	

	private final TransUnitUpdatedEventHandler updateHandler = new TransUnitUpdatedEventHandler() {
		
		@Override
		public void onTransUnitUpdated(TransUnitUpdatedEvent event) {
			Log.info("trans unit updated. updating stats.");
			if(documentid == null){
				return;
			}
			if(!event.getDocumentId().equals(documentid)){
				return;
			}
			
			int fuzzyCount = getDisplay().getCount(ContentState.NeedReview);
			int translatedCount = getDisplay().getCount(ContentState.Approved);
			int untranslatedCount = getDisplay().getCount(ContentState.New);
			
			switch (event.getPreviousStatus() ) {
			case Approved:
				translatedCount--;
				break;
			case NeedReview:
				fuzzyCount--;
				break;
			case New:
				untranslatedCount--;
				break;
			}
			
			switch (event.getNewStatus() ) {
			case Approved:
				translatedCount++;
				break;
			case NeedReview:
				fuzzyCount++;
				break;
			case New:
				untranslatedCount++;
				break;
			}
			
			getDisplay().setCount(fuzzyCount, translatedCount, untranslatedCount);
			
		}
	};	
	
}
