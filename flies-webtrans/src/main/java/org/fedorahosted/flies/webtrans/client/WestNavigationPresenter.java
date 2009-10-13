package org.fedorahosted.flies.webtrans.client;

import net.customware.gwt.presenter.client.EventBus;
import net.customware.gwt.presenter.client.place.Place;
import net.customware.gwt.presenter.client.place.PlaceRequest;
import net.customware.gwt.presenter.client.widget.WidgetDisplay;
import net.customware.gwt.presenter.client.widget.WidgetPresenter;

import com.allen_sauer.gwt.log.client.Log;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.inject.Inject;


public class WestNavigationPresenter extends WidgetPresenter<WestNavigationPresenter.Display>{
	
	public static final Place PLACE = new Place("WestNavigationPresenter");
	
	//private final DispatchAsync dispatcher;	

	public interface Display extends WidgetDisplay {
		HasWidgets getWidgets();
	}

	private final WorkspaceUsersPresenter workspaceUsersPresenter;
	private final DocumentListPresenter documentListPresenter;
	private final TransUnitInfoPresenter transUnitInfoPresenter;

	
	@Inject
	public WestNavigationPresenter(final Display display, final EventBus eventBus, WorkspaceUsersPresenter workspaceUsersPresenter, DocumentListPresenter documentListPresenter,TransUnitInfoPresenter transUnitInfoPresenter){//, final DispatchAsync dispatcher) {
		super(display, eventBus);
		//this.dispatcher = dispatcher;
		this.workspaceUsersPresenter = workspaceUsersPresenter;
		this.documentListPresenter = documentListPresenter;
		this.transUnitInfoPresenter = transUnitInfoPresenter;
	}

	@Override
	public Place getPlace() {
		return PLACE;
	}

	@Override
	protected void onBind() {
		// Disabled: This pushes the footer (DockPanel.SOUTH) off the bottom of the visible window
//		eventBus.addHandler(WindowResizeEvent.getType(), new ResizeHandler() {
//			
//			@Override
//			public void onResize(ResizeEvent event) {
//				Log.info("handling resize in LeftNavigationPresenter");
//				display.asWidget().setHeight(event.getHeight() + "px");
//			}
//		});
		display.asWidget().setHeight("100%");
		display.getWidgets().add(documentListPresenter.getDisplay().asWidget());
		display.getWidgets().add(transUnitInfoPresenter.getDisplay().asWidget());
		display.getWidgets().add(workspaceUsersPresenter.getDisplay().asWidget());
	}

	@Override
	protected void onPlaceRequest(PlaceRequest request) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void onUnbind() {
		documentListPresenter.unbind();
		workspaceUsersPresenter.unbind();
	}

	@Override
	public void refreshDisplay() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void revealDisplay() {
		// TODO Auto-generated method stub
		
	}

	
}
