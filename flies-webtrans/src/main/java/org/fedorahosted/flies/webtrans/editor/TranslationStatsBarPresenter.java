package org.fedorahosted.flies.webtrans.editor;

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
import org.fedorahosted.flies.webtrans.client.events.TransUnitUpdatedEvent;
import org.fedorahosted.flies.webtrans.client.events.TransUnitUpdatedEventHandler;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;

public class TranslationStatsBarPresenter extends WidgetPresenter<TranslationStatsBarPresenter.Display>{

	private final DispatchAsync dispatcher;	
	private final WorkspaceContext workspaceContext;
		
	public interface Display extends WidgetDisplay, HasTransUnitCount {
	}

	@Inject
	public TranslationStatsBarPresenter(final WorkspaceContext workspaceContext, final Display display, final EventBus eventBus, final DispatchAsync dispatcher) {
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
				requestStatusCount(event.getDocumentId(), workspaceContext.getLocaleId());
			}
		}));
		
		registerHandler(eventBus.addHandler(TransUnitUpdatedEvent.getType(), new TransUnitUpdatedEventHandler() {
			
			@Override
			public void onTransUnitUpdated(TransUnitUpdatedEvent event) {
				
				int fuzzyCount = display.getFuzzy();
				int translatedCount = display.getTranslated();
				int untranslatedCount = display.getUntranslated();
				
				switch (event.getData().getPreviousStatus() ) {
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
				
				switch (event.getData().getNewStatus() ) {
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
				
				display.setStatus(fuzzyCount, translatedCount, untranslatedCount);
				
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
	
	private void requestStatusCount(DocumentId id, LocaleId localeid) {
		dispatcher.execute(new GetStatusCount(id, localeid), new AsyncCallback<GetStatusCountResult>() {
			@Override
			public void onFailure(Throwable caught) {
			}
			@Override
			public void onSuccess(GetStatusCountResult result) {
				display.setStatus((int) result.getFuzzy(), (int)result.getTranslated(), (int)result.getUntranslated());
			}
		});
	}
}
