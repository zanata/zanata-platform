package org.fedorahosted.flies.webtrans.client;

import java.util.Arrays;

import net.customware.gwt.presenter.client.EventBus;
import net.customware.gwt.presenter.client.place.Place;
import net.customware.gwt.presenter.client.place.PlaceRequest;
import net.customware.gwt.presenter.client.widget.WidgetDisplay;
import net.customware.gwt.presenter.client.widget.WidgetPresenter;

import org.fedorahosted.flies.gwt.model.Person;
import org.fedorahosted.flies.gwt.model.PersonId;
import org.fedorahosted.flies.webtrans.client.ui.HasChildTreeNodes;
import org.fedorahosted.flies.webtrans.client.ui.HasFilter;
import org.fedorahosted.flies.webtrans.client.ui.HasNodeMouseOutHandlers;
import org.fedorahosted.flies.webtrans.client.ui.HasNodeMouseOverHandlers;
import org.fedorahosted.flies.webtrans.client.ui.TreeNodeImpl;

import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.user.client.ui.DecoratedPopupPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.inject.Inject;

public class WorkspaceUsersPresenter extends WidgetPresenter<WorkspaceUsersPresenter.Display> {

	public interface Display extends WidgetDisplay{
		HasChildTreeNodes<Person> getTree();
		HasFilter<Person> getFilter();
		HasNodeMouseOverHandlers getNodeMouseOver();
		HasNodeMouseOutHandlers getNodeMouseOut();
	}
	
	@Inject
	public WorkspaceUsersPresenter(final Display display, final EventBus eventBus) {
		super(display, eventBus);
	}
	
	
	@Override
	public Place getPlace() {
		return null;
	}

	@Override
	protected void onBind() {
		Person [] translators = new Person[]{
			new Person( new PersonId("bob"), "Bob Smith"),
			new Person( new PersonId("jane"), "Jane English"),
			new Person( new PersonId("bill"), "Bill Martin")
			};	
		getDisplay().getFilter().setList(Arrays.asList(translators));
		
		final DecoratedPopupPanel userPopupPanel = new DecoratedPopupPanel(true);
		
		getDisplay().getNodeMouseOver().addNodeMouseOverHandler(new MouseOverHandler() {

			@Override
			public void onMouseOver(MouseOverEvent event) {
				if (event.getSource() instanceof TreeNodeImpl<?, ?>) {
					TreeNodeImpl<PersonId, Person> source = (TreeNodeImpl<PersonId, Person>) event.getSource();	
					
					VerticalPanel popupMainPanel = new VerticalPanel();
					Person overPerson = source.getObject();
					Label popupTitle = new Label ("User Profile");
					Label userID = new Label("User ID: " + overPerson.getId().toString());
					Label userName = new Label ("User Name: " + overPerson.getName().toString());
					
					popupMainPanel.add(popupTitle);
					popupMainPanel.add(userID);
					popupMainPanel.add(userName);
					
					userPopupPanel.setWidget(popupMainPanel);
					userPopupPanel.setPopupPosition(source.getAbsoluteLeft() + 125, source.getAbsoluteTop() - 5);
					userPopupPanel.show();
				}
			}
			
		});
		
		getDisplay().getNodeMouseOut().addNodeMouseOutHandler(new MouseOutHandler() {
		
			@Override
			public void onMouseOut(MouseOutEvent event) {
				userPopupPanel.clear();
				userPopupPanel.hide();
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
