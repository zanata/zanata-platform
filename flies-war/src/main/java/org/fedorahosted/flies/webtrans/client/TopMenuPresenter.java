package org.fedorahosted.flies.webtrans.client;

import net.customware.gwt.presenter.client.EventBus;
import net.customware.gwt.presenter.client.place.Place;
import net.customware.gwt.presenter.client.place.PlaceRequest;
import net.customware.gwt.presenter.client.widget.WidgetDisplay;
import net.customware.gwt.presenter.client.widget.WidgetPresenter;

import org.fedorahosted.flies.common.ContentState;
import org.fedorahosted.flies.webtrans.client.auth.Identity;
import org.fedorahosted.flies.webtrans.editor.table.TableEditorPresenter;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.inject.Inject;

public class TopMenuPresenter extends WidgetPresenter<TopMenuPresenter.Display>
		implements HasNavTransUnitHandlers {

	public interface Display extends WidgetDisplay {
		// HasClickHandlers getLogoutLink();
		HasWidgets getWidgets();

		HasText getUsername();

		HasText getProjectName();

		HasClickHandlers getPrevEntryButton();

		HasClickHandlers getNextEntryButton();

		HasClickHandlers getPrevFuzzyButton();

		HasClickHandlers getNextFuzzyButton();

		HasClickHandlers getPrevUntranslatedButton();

		HasClickHandlers getNextUntranslatedButton();
	}

	private final WorkspaceContext workspaceContext;
	private final Identity identity;

	@Inject
	public TopMenuPresenter(Display display, EventBus eventBus,
			WorkspaceContext workspaceContext, Identity identity,
			TableEditorPresenter webTransTablePresenter) {
		super(display, eventBus);
		this.workspaceContext = workspaceContext;
		this.identity = identity;
	}

	@Override
	public Place getPlace() {
		return null;
	}

	@Override
	protected void onBind() {
		// display.getLogoutLink().addClickHandler(new ClickHandler() {

		// @Override
		// public void onClick(ClickEvent event) {
		// identity.invalidate();
		// }
		// });

		// Prev Entry
		display.getPrevEntryButton().addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				fireEvent(new NavTransUnitEvent(null, -1));
			}
		});

//		display.getPrevEntryButton().addKeyUpHandler(new KeyUpHandler() {
//			@Override
//			public void onKeyUp(KeyUpEvent event) {
//				if (event.isControlKeyDown() && event.isAltKeyDown()
//						&& event.getNativeKeyCode() == KeyCodes.KEY_PAGEUP)
//					fireEvent(new NavTransUnitEvent(null, -1));
//			}
//		});

		display.getNextEntryButton().addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				fireEvent(new NavTransUnitEvent(null, +1));
			}
		});

//		display.getNextEntryButton().addKeyUpHandler(new KeyUpHandler() {
//			@Override
//			public void onKeyUp(KeyUpEvent event) {
//				if (event.isControlKeyDown() && event.isAltKeyDown()
//						&& event.getNativeKeyCode() == KeyCodes.KEY_PAGEDOWN)
//					fireEvent(new NavTransUnitEvent(null, +1));
//			}
//		});

		display.getPrevFuzzyButton().addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				fireEvent(new NavTransUnitEvent(ContentState.NeedReview, -1));
			}
		});

//		display.getPrevFuzzyButton().addKeyUpHandler(new KeyUpHandler() {
//			@Override
//			public void onKeyUp(KeyUpEvent event) {
//				if (event.isAltKeyDown()
//						&& event.getNativeKeyCode() == KeyCodes.KEY_PAGEUP)
//					fireEvent(new NavTransUnitEvent(ContentState.NeedReview, -1));
//			}
//		});

		display.getNextFuzzyButton().addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				fireEvent(new NavTransUnitEvent(ContentState.NeedReview, +1));
			}
		});

//		display.getNextFuzzyButton().addKeyUpHandler(new KeyUpHandler() {
//			@Override
//			public void onKeyUp(KeyUpEvent event) {
//				if (event.isAltKeyDown() && event.getNativeKeyCode() == 'J')
//					fireEvent(new NavTransUnitEvent(ContentState.NeedReview, +1));
//			}
//		});

		display.getPrevUntranslatedButton().addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				fireEvent(new NavTransUnitEvent(ContentState.New, -1));
			}
		});

		display.getNextUntranslatedButton().addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				fireEvent(new NavTransUnitEvent(ContentState.New, +1));
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
		display.getUsername().setText(identity.getPerson().getName());
		display.getProjectName().setText(workspaceContext.getWorkspaceName());
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
