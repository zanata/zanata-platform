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
import org.fedorahosted.flies.webtrans.client.rpc.CachingDispatchAsync;

import com.allen_sauer.gwt.log.client.Log;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.TextBox;
import com.google.inject.Inject;

public class TransMemoryPresenter extends WidgetPresenter<TransMemoryPresenter.Display> {
	private final WorkspaceContext workspaceContext;
	private final CachingDispatchAsync dispatcher;
	private boolean transMemoryVisible = false;
	
	public interface Display extends WidgetDisplay {
		Button getSearchButton();
		TextBox getTmTextBox();
		void createTable(ArrayList<TransMemory> memories);
		void clearResults();
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
				display.clearResults();
				final String query = display.getTmTextBox().getText();
				GetTranslationMemory action = new GetTranslationMemory(
						query, 
						workspaceContext.getLocaleId(), false);
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
			}
		});
		
		registerHandler(eventBus.addHandler(SelectionEvent.getType(), new SelectionHandler<TransUnit>() {
			@Override
			public void onSelection(SelectionEvent<TransUnit> event) {
				if(transMemoryVisible) {
					//Start automatically fuzzy search
					final String query = event.getSelectedItem().getSource();
					final GetTranslationMemory action = new GetTranslationMemory(
							query, 
							workspaceContext.getLocaleId(), 
							true);
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
				}
			}
		})); 
		
		registerHandler(eventBus.addHandler(VisibilityEvent.getType(), new VisibilityHandler(){
			@Override
			public void onVisibilityChange(VisibilityEvent tabSelectionEvent) {
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
