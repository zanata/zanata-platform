package org.fedorahosted.flies.webtrans.client;

import org.fedorahosted.flies.webtrans.editor.filter.FilterPresenter;

import net.customware.gwt.presenter.client.EventBus;
import net.customware.gwt.presenter.client.place.Place;
import net.customware.gwt.presenter.client.place.PlaceRequest;
import net.customware.gwt.presenter.client.widget.WidgetDisplay;
import net.customware.gwt.presenter.client.widget.WidgetPresenter;

import com.google.gwt.user.client.ui.HasWidgets;
import com.google.inject.Inject;


public class WestNavigationPresenter extends WidgetPresenter<WestNavigationPresenter.Display>{
	
	public interface Display extends WidgetDisplay {
		HasWidgets getWidgets();
	}

	private final WorkspaceUsersPresenter workspaceUsersPresenter;
	private final DocumentListPresenter docListPresenter;
	private final FilterPresenter transUnitInfoPresenter;

	
	@Inject
	public WestNavigationPresenter(Display display, EventBus eventBus, 
			WorkspaceUsersPresenter workspaceUsersPresenter, 
			DocumentListPresenter docListPresenter,
			FilterPresenter transUnitInfoPresenter){//, final DispatchAsync dispatcher) {
		super(display, eventBus);
		//this.dispatcher = dispatcher;
		this.workspaceUsersPresenter = workspaceUsersPresenter;
		this.docListPresenter = docListPresenter;
		this.transUnitInfoPresenter = transUnitInfoPresenter;
	}

	@Override
	public Place getPlace() {
		return null;
	}

	@Override
	protected void onBind() {
		docListPresenter.bind();
		display.getWidgets().add(docListPresenter.getDisplay().asWidget());

		transUnitInfoPresenter.bind();
		display.getWidgets().add(transUnitInfoPresenter.getDisplay().asWidget());
		
		workspaceUsersPresenter.bind();
		display.getWidgets().add(workspaceUsersPresenter.getDisplay().asWidget());

	}

	@Override
	protected void onPlaceRequest(PlaceRequest request) {
	}

	@Override
	protected void onUnbind() {
		docListPresenter.unbind();
		transUnitInfoPresenter.unbind();
		workspaceUsersPresenter.unbind();
	}

	@Override
	public void refreshDisplay() {
	}

	@Override
	public void revealDisplay() {
	}

	
}
