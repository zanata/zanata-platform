package org.fedorahosted.flies.webtrans.editor;

import net.customware.gwt.dispatch.client.DispatchAsync;
import net.customware.gwt.presenter.client.EventBus;
import net.customware.gwt.presenter.client.place.Place;
import net.customware.gwt.presenter.client.place.PlaceRequest;

import org.fedorahosted.flies.gwt.model.DocumentId;
import org.fedorahosted.flies.gwt.rpc.GetStatusCount;
import org.fedorahosted.flies.gwt.rpc.GetStatusCountResult;
import org.fedorahosted.flies.webtrans.client.DocumentSelectionEvent;
import org.fedorahosted.flies.webtrans.client.DocumentSelectionHandler;
import org.fedorahosted.flies.webtrans.client.WorkspaceContext;
import org.fedorahosted.flies.webtrans.client.events.TransUnitUpdatedEvent;
import org.fedorahosted.flies.webtrans.client.events.TransUnitUpdatedEventHandler;

import com.allen_sauer.gwt.log.client.Log;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;

public class DocumentStatusPresenter extends TranslationStatsBarPresenter {
	
	private final WorkspaceContext workspaceContext;
	private final DispatchAsync dispatcher;
	private int latestStatusCountOffset = -1;
	private DocumentId documentid;
	
	@Inject
	public DocumentStatusPresenter(Display display, EventBus eventBus,
			DispatchAsync dispatcher,
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
				documentid=null;
				requestStatusCount(event.getDocumentId());
			}
		}));
		
		registerHandler(eventBus.addHandler(TransUnitUpdatedEvent.getType(), new TransUnitUpdatedEventHandler() {
			
			@Override
			public void onTransUnitUpdated(TransUnitUpdatedEvent event) {
				Log.info("trans unit updated. updating stats.");
				if(documentid == null){
					return;
				}
				if(!event.getDocumentId().equals(documentid)){
					return;
				}
				else if( event.getOffset() < latestStatusCountOffset){
					return;
				}
				
				int fuzzyCount = getDisplay().getFuzzy();
				int translatedCount = getDisplay().getTranslated();
				int untranslatedCount = getDisplay().getUntranslated();
				
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
				
				getDisplay().setStatus(fuzzyCount, translatedCount, untranslatedCount);
				
			}
		}));
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
	
	private void requestStatusCount(final DocumentId newDocumentId) {
		Log.info("requesting stats");
		dispatcher.execute(new GetStatusCount(newDocumentId, workspaceContext.getProjectContainerId(), workspaceContext.getLocaleId()), new AsyncCallback<GetStatusCountResult>() {
			@Override
			public void onFailure(Throwable caught) {
			}
			@Override
			public void onSuccess(GetStatusCountResult result) {
				getDisplay().setStatus((int) result.getFuzzy(), (int)result.getTranslated(), (int)result.getUntranslated());
				latestStatusCountOffset = result.getSequence();
				documentid = newDocumentId;
			}
	});
	}	

}
