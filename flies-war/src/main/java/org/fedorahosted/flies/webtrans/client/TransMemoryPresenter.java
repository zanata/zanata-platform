package org.fedorahosted.flies.webtrans.client;

import java.util.ArrayList;

import net.customware.gwt.presenter.client.EventBus;
import net.customware.gwt.presenter.client.place.Place;
import net.customware.gwt.presenter.client.place.PlaceRequest;
import net.customware.gwt.presenter.client.widget.WidgetDisplay;
import net.customware.gwt.presenter.client.widget.WidgetPresenter;

import org.fedorahosted.flies.gwt.model.TransMemory;
import org.fedorahosted.flies.gwt.rpc.GetTranslationMemory;
import org.fedorahosted.flies.gwt.rpc.GetTranslationMemoryResult;
import org.fedorahosted.flies.webtrans.client.rpc.CachingDispatchAsync;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.TextBox;
import com.google.inject.Inject;

public class TransMemoryPresenter extends WidgetPresenter<TransMemoryPresenter.Display> {
	private final WorkspaceContext workspaceContext;
	private final CachingDispatchAsync dispatcher;
	private boolean transMemoryVisible = false;
	private boolean transUnitSelected = false;
	
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
			if(transMemoryVisible && transUnitSelected) {
				display.clearResults();
				GetTranslationMemory action = new GetTranslationMemory(
						display.getTmTextBox().getText(), 
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
			}
		});
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

	public void isTransMemoryVisible(boolean visible) {
		transMemoryVisible = visible;
		
	}

	public void isTransUnitSelected(boolean selected) {
		transUnitSelected  = selected;
	}
}
