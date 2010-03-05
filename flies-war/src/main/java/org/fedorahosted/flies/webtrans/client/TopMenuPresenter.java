package org.fedorahosted.flies.webtrans.client;

import net.customware.gwt.presenter.client.EventBus;
import net.customware.gwt.presenter.client.place.Place;
import net.customware.gwt.presenter.client.place.PlaceRequest;
import net.customware.gwt.presenter.client.widget.WidgetDisplay;
import net.customware.gwt.presenter.client.widget.WidgetPresenter;

import org.fedorahosted.flies.webtrans.client.auth.Identity;

import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class TopMenuPresenter extends WidgetPresenter<TopMenuPresenter.Display> {

	public interface Display extends WidgetDisplay {
		// HasClickHandlers getLogoutLink();
		HasWidgets getWidgets();

		HasText getUsername();

		HasText getProjectName();
		
		void setTransUnitNavigation(Widget transUnitNavigation);

	}

	private final WorkspaceContext workspaceContext;
	private final Identity identity;
	private final TransUnitNavigationPresenter transUnitNavigationPresenter;

	@Inject
	public TopMenuPresenter(Display display, EventBus eventBus,
			TransUnitNavigationPresenter transUnitNavigationPresenter, 
			WorkspaceContext workspaceContext, Identity identity) {
		super(display, eventBus);
		this.workspaceContext = workspaceContext;
		this.identity = identity;
		this.transUnitNavigationPresenter = transUnitNavigationPresenter;
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

		transUnitNavigationPresenter.bind();
		
		display.setTransUnitNavigation(transUnitNavigationPresenter.getDisplay().asWidget());
		
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
}
