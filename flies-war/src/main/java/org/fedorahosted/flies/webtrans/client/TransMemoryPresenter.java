package org.fedorahosted.flies.webtrans.client;

import java.util.ArrayList;

import net.customware.gwt.presenter.client.EventBus;
import net.customware.gwt.presenter.client.place.Place;
import net.customware.gwt.presenter.client.place.PlaceRequest;
import net.customware.gwt.presenter.client.widget.WidgetDisplay;
import net.customware.gwt.presenter.client.widget.WidgetPresenter;

import org.fedorahosted.flies.gwt.model.TransMemory;
import org.fedorahosted.flies.gwt.model.TransUnit;
import org.fedorahosted.flies.gwt.rpc.GetTranslationMemory;
import org.fedorahosted.flies.gwt.rpc.GetTranslationMemoryResult;
import org.fedorahosted.flies.gwt.rpc.GetTranslationMemory.SearchType;
import org.fedorahosted.flies.webtrans.client.rpc.CachingDispatchAsync;

import com.allen_sauer.gwt.log.client.Log;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.inject.Inject;

public class TransMemoryPresenter extends WidgetPresenter<TransMemoryPresenter.Display> {
	private final WorkspaceContext workspaceContext;
	private final CachingDispatchAsync dispatcher;
	private boolean transMemoryVisible = false;
	
	public interface Display extends WidgetDisplay {
		HasValue<Boolean> getExactButton();
		HasClickHandlers getSearchButton();
		HasText getTmTextBox();
		void createTable(ArrayList<TransMemory> memories);
		void clearResults();
		void startProcessing();
		void stopProcessing();
	}

	@Inject
	public TransMemoryPresenter(Display display, EventBus eventBus, CachingDispatchAsync dispatcher, WorkspaceContext workspaceContext) {
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
		
		display.getSearchButton().addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				display.startProcessing();
				display.clearResults();
				final String query = display.getTmTextBox().getText();
				GetTranslationMemory.SearchType searchType = 
					display.getExactButton().getValue() ? 
						SearchType.EXACT : SearchType.RAW;
				GetTranslationMemory action = new GetTranslationMemory(
						query, workspaceContext.getLocaleId(), searchType);
				dispatcher.execute(action, new AsyncCallback<GetTranslationMemoryResult>() {
					@Override
					public void onFailure(Throwable caught) {
					}
					@Override
					public void onSuccess(GetTranslationMemoryResult result) {
						ArrayList<TransMemory> memories = result.getMemories();
						display.createTable(memories);
					}
				});
				display.stopProcessing();
			}
		});
		
		registerHandler(eventBus.addHandler(SelectionEvent.getType(), new SelectionHandler<TransUnit>() {
			@Override
			public void onSelection(SelectionEvent<TransUnit> event) {
				display.getTmTextBox().setText("");
				display.clearResults();
				if(transMemoryVisible) {
					display.startProcessing();
					//Start automatically fuzzy search
					final String query = event.getSelectedItem().getSource();
					final GetTranslationMemory action = new GetTranslationMemory(
							query, 
							workspaceContext.getLocaleId(), 
							GetTranslationMemory.SearchType.FUZZY);
					dispatcher.execute(action, new AsyncCallback<GetTranslationMemoryResult>() {
						@Override
						public void onFailure(Throwable caught) {
							Log.error(caught.getMessage(), caught);
						}
						@Override
						public void onSuccess(GetTranslationMemoryResult result) {
							ArrayList<TransMemory> memories = result.getMemories();
							display.createTable(memories);
						}
					});
					display.stopProcessing();
				}
			}
		})); 
		
		registerHandler(eventBus.addHandler(TransMemoryVisibilityEvent.getType(), new TransMemoryVisibilityHandler(){
			@Override
			public void onVisibilityChange(TransMemoryVisibilityEvent tabSelectionEvent) {
				transMemoryVisible = tabSelectionEvent.isVisible();
				Log.debug("received visibility=="+transMemoryVisible);
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
}
