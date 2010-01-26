package org.fedorahosted.flies.webtrans.client;

import net.customware.gwt.presenter.client.EventBus;
import net.customware.gwt.presenter.client.place.Place;
import net.customware.gwt.presenter.client.place.PlaceRequest;
import net.customware.gwt.presenter.client.widget.WidgetDisplay;
import net.customware.gwt.presenter.client.widget.WidgetPresenter;

import org.fedorahosted.flies.webtrans.editor.table.TableEditorPresenter;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.inject.Inject;

public class TransNavToolbarPresenter extends WidgetPresenter<TransNavToolbarPresenter.Display> {
	
	public interface Display extends WidgetDisplay {
		Button getPrevEntryButton();
		Button getNextEntryButton();
		Button getPrevFuzzyButton();
		Button getNextFuzzyButton();
		Button getPrevUntranslatedButton();
		Button getNextUntranslatedButton();
	}
	
	@Inject
	public TransNavToolbarPresenter(Display display, EventBus eventBus, WorkspaceContext workspaceContext, TableEditorPresenter webTransTablePresenter) {
		super(display, eventBus);
	}

	@Override
	public Place getPlace() {
		return null;
	}

	@Override
	protected void onBind() {
		display.getPrevEntryButton().addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {

			}
		});
		
		display.getNextEntryButton().addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {

			}
		});	
		
		display.getPrevFuzzyButton().addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {

			}
		});
		
		display.getNextFuzzyButton().addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {

			}
		});		
		
		display.getPrevUntranslatedButton().addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				
			}
		});
		
		display.getNextUntranslatedButton().addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				
			}
		});		
		
		refreshDisplay();
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
