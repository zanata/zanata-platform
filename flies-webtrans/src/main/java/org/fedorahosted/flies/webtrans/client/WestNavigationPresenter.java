package org.fedorahosted.flies.webtrans.client;

import java.util.ArrayList;

import net.customware.gwt.presenter.client.EventBus;
import net.customware.gwt.presenter.client.place.Place;
import net.customware.gwt.presenter.client.place.PlaceRequest;
import net.customware.gwt.presenter.client.widget.WidgetDisplay;
import net.customware.gwt.presenter.client.widget.WidgetPresenter;

import org.fedorahosted.flies.webtrans.model.DocName;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
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
//		Button button = new Button("replaceList");
//		display.getWidgets().add(button);
//		button.addClickHandler(new ClickHandler() {
//			
//			@Override
//			public void onClick(ClickEvent event) {
//				ArrayList<DocName> names = new ArrayList<DocName>();
//				names.add(new DocName("id1", "name1", "path1"));
//				names.add(new DocName("id2", "name2", "path1"));
//				names.add(new DocName("id3", "name1", "path2"));
//				names.add(new DocName("id4", "name2", "path2"));
//				names.add(new DocName("id5", "name2", ""));
//				names.add(new DocName("id6", "name1", null));
//				documentListPresenter.setDocNameList(names);
//			}
//		});
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
