package org.fedorahosted.flies.webtrans.client;

import net.customware.gwt.presenter.client.EventBus;
import net.customware.gwt.presenter.client.place.Place;
import net.customware.gwt.presenter.client.place.PlaceRequest;
import net.customware.gwt.presenter.client.widget.WidgetDisplay;
import net.customware.gwt.presenter.client.widget.WidgetPresenter;

import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.SourcesTabEvents;
import com.google.gwt.user.client.ui.TabListener;
import com.google.gwt.user.client.ui.TabPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class SouthPresenter extends WidgetPresenter<SouthPresenter.Display> {
	private final TransMemoryPresenter transMemorypresenter;
	
	public interface Display extends WidgetDisplay {
		HasWidgets getWidgets();
		HasText getGlossary();
		HasText getRelated();
		TabPanel getTabPanel();
	}
	
	@Inject
	public SouthPresenter(Display display, EventBus eventBus, TransMemoryPresenter transMemorypresenter) {
		super(display, eventBus);
		this.transMemorypresenter = transMemorypresenter;
		
	}

	@Override
	public Place getPlace() {
		return null;
	}

	@Override
	protected void onBind() {
		transMemorypresenter.bind();
		display.getWidgets().add(transMemorypresenter.getDisplay().asWidget());
		refreshDisplay();
		
		display.getTabPanel().addSelectionHandler(new SelectionHandler<Integer>() {

			@Override
			public void onSelection(SelectionEvent<Integer> event) {
				if (event.getSelectedItem() == 0) {
					//Translation Memory Tab is visible, Send event to TableEditorPresenter
					eventBus.fireEvent(new TMTabSelectionEvent(true));
				}
				else {
					eventBus.fireEvent(new TMTabSelectionEvent(false));
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

}
