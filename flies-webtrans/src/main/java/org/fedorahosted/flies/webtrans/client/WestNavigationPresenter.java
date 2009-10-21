package org.fedorahosted.flies.webtrans.client;

import net.customware.gwt.presenter.client.EventBus;
import net.customware.gwt.presenter.client.place.Place;
import net.customware.gwt.presenter.client.place.PlaceRequest;
import net.customware.gwt.presenter.client.widget.WidgetDisplay;
import net.customware.gwt.presenter.client.widget.WidgetPresenter;

import com.google.gwt.user.client.ui.HasWidgets;
import com.google.inject.Inject;


public class WestNavigationPresenter extends WidgetPresenter<WestNavigationPresenter.Display>{
	
	public static final Place PLACE = new Place("WestNavigationPresenter");
	
	//private final DispatchAsync dispatcher;	

	public interface Display extends WidgetDisplay {
		HasWidgets getWidgets();
	}

	private final WorkspaceUsersPresenter workspaceUsersPresenter;
	private final DocumentListPresenter docListPresenter;
	private final TransUnitInfoPresenter transUnitInfoPresenter;

	
	@Inject
	public WestNavigationPresenter(Display display, EventBus eventBus, 
			WorkspaceUsersPresenter workspaceUsersPresenter, 
			DocumentListPresenter docListPresenter,
			TransUnitInfoPresenter transUnitInfoPresenter){//, final DispatchAsync dispatcher) {
		super(display, eventBus);
		//this.dispatcher = dispatcher;
		this.workspaceUsersPresenter = workspaceUsersPresenter;
		this.docListPresenter = docListPresenter;
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
		docListPresenter.bind();
		display.getWidgets().add(docListPresenter.getDisplay().asWidget());
		transUnitInfoPresenter.bind();
		display.getWidgets().add(transUnitInfoPresenter.getDisplay().asWidget());
		workspaceUsersPresenter.bind();
		display.getWidgets().add(workspaceUsersPresenter.getDisplay().asWidget());

	}

	@Override
	protected void onPlaceRequest(PlaceRequest request) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void onUnbind() {
		docListPresenter.unbind();
		transUnitInfoPresenter.unbind();
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
