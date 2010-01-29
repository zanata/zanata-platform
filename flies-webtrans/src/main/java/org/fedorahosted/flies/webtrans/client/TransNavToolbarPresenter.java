package org.fedorahosted.flies.webtrans.client;

import net.customware.gwt.presenter.client.EventBus;
import net.customware.gwt.presenter.client.place.Place;
import net.customware.gwt.presenter.client.place.PlaceRequest;
import net.customware.gwt.presenter.client.widget.WidgetDisplay;
import net.customware.gwt.presenter.client.widget.WidgetPresenter;

import org.fedorahosted.flies.common.ContentState;
import org.fedorahosted.flies.webtrans.editor.table.TableEditorPresenter;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.Button;
import com.google.inject.Inject;

public class TransNavToolbarPresenter extends WidgetPresenter<TransNavToolbarPresenter.Display> implements HasNavTransUnitHandlers {
	
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
		
		//Prev Entry
		display.getPrevEntryButton().addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				fireEvent(new NavTransUnitEvent(null, -1));
			}
		});
		
//		display.getPrevEntryButton().addKeyUpHandler(new KeyUpHandler() {
//			@Override
//			public void onKeyUp(KeyUpEvent event) {
//				if(event.isControlKeyDown() && event.isAltKeyDown() && event.getNativeKeyCode() == KeyCodes.KEY_PAGEUP)
//					fireEvent(new NavTransUnitEvent(null, -1));
//			}
//		});	
		
		//Next Entry
		display.getNextEntryButton().addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				fireEvent(new NavTransUnitEvent(null, +1));
			}
		});	
//		
//		display.getNextEntryButton().addKeyUpHandler(new KeyUpHandler() {
//			@Override
//			public void onKeyUp(KeyUpEvent event) {
//				if(event.isControlKeyDown() && event.isAltKeyDown() && event.getNativeKeyCode() == KeyCodes.KEY_PAGEDOWN)
//				fireEvent(new NavTransUnitEvent(null, +1));
//			}
//		});	
		
		//Prev Fuzzy
		display.getPrevFuzzyButton().addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				fireEvent(new NavTransUnitEvent(ContentState.NeedReview, -1));
			}
		});
		
//		display.getPrevFuzzyButton().addKeyUpHandler(new KeyUpHandler() {
//			@Override
//			public void onKeyUp(KeyUpEvent event) {
//				if(event.isAltKeyDown() && event.getNativeKeyCode() == KeyCodes.KEY_PAGEUP)
//				fireEvent(new NavTransUnitEvent(ContentState.NeedReview, -1));
//			}
//		});	
		
		//Next Fuzzy
		display.getNextFuzzyButton().addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				fireEvent(new NavTransUnitEvent(ContentState.NeedReview, +1));
			}
		});		
		
//		display.getNextFuzzyButton().addKeyUpHandler(new KeyUpHandler() {
//			@Override
//			public void onKeyUp(KeyUpEvent event) {
//				if(event.isAltKeyDown() && event.getNativeKeyCode() == 'J')
//				fireEvent(new NavTransUnitEvent(ContentState.NeedReview, +1));
//			}
//		});	
		
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

	@Override
	public HandlerRegistration addNavTransUnitHandler(
			NavTransUnitHandler handler) {
		// TODO Auto-generated method stub
		return eventBus.addHandler(NavTransUnitEvent.getType(), handler);
	}

	@Override
	public void fireEvent(GwtEvent<?> event) {
		eventBus.fireEvent(event);
		
	}
	

}
