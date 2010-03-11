package org.fedorahosted.flies.webtrans.client;

import java.util.ArrayList;

import org.fedorahosted.flies.gwt.model.Person;
import org.fedorahosted.flies.webtrans.client.WorkspaceUsersPresenter.Display;
import org.fedorahosted.flies.webtrans.client.rpc.CachingDispatchAsync;

import com.google.inject.Inject;

import net.customware.gwt.presenter.client.EventBus;
import net.customware.gwt.presenter.client.place.Place;
import net.customware.gwt.presenter.client.place.PlaceRequest;
import net.customware.gwt.presenter.client.widget.WidgetDisplay;
import net.customware.gwt.presenter.client.widget.WidgetPresenter;

public class SidePanelPresenter extends WidgetPresenter<SidePanelPresenter.Display> {
	
	public interface Display extends WidgetDisplay{
		void collapseUsersPanel();
		void expandUsersPanel();
	}
	
	private final WorkspaceUsersPresenter workspaceUsersPresenter;
	
	@Inject
	public SidePanelPresenter(final Display display, final EventBus eventBus, 
			WorkspaceUsersPresenter workspaceUsersPresenter) {
		super(display, eventBus);
		this.workspaceUsersPresenter = workspaceUsersPresenter;
	}

	@Override
	public Place getPlace() {
		return null;
	}

	@Override
	protected void onBind() {
		workspaceUsersPresenter.bind();
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
