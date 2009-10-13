package org.fedorahosted.flies.webtrans.client;

import org.fedorahosted.flies.webtrans.client.Application.WindowResizeEvent;

import net.customware.gwt.presenter.client.EventBus;
import net.customware.gwt.presenter.client.place.Place;
import net.customware.gwt.presenter.client.place.PlaceRequest;
import net.customware.gwt.presenter.client.widget.WidgetDisplay;
import net.customware.gwt.presenter.client.widget.WidgetPresenter;


import com.allen_sauer.gwt.log.client.Log;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
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

	
	@Inject
	public WestNavigationPresenter(final Display display, final EventBus eventBus, WorkspaceUsersPresenter workspaceUsersPresenter, DocumentListPresenter documentListPresenter){//, final DispatchAsync dispatcher) {
		super(display, eventBus);
		//this.dispatcher = dispatcher;
		this.workspaceUsersPresenter = workspaceUsersPresenter;
		this.documentListPresenter = documentListPresenter;
		bind();
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
		display.getWidgets().add(workspaceUsersPresenter.getDisplay().asWidget());

		documentListPresenter.addValueChangeHandler(new ValueChangeHandler<String>() {
			@Override
			public void onValueChange(ValueChangeEvent<String> event) {
				Log.info("selected document: "+event.getValue());
			}
		});

	}

	@Override
	protected void onPlaceRequest(PlaceRequest request) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void onUnbind() {
		// TODO Auto-generated method stub
		
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
