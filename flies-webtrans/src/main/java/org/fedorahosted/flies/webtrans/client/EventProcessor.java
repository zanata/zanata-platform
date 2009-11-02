package org.fedorahosted.flies.webtrans.client;

import java.util.Iterator;

import org.fedorahosted.flies.gwt.rpc.GetEventsAction;
import org.fedorahosted.flies.gwt.rpc.GetEventsResult;
import org.fedorahosted.flies.gwt.rpc.SessionEvent;
import org.fedorahosted.flies.gwt.rpc.TransUnitUpdated;
import org.fedorahosted.flies.webtrans.client.events.TransUnitUpdatedEvent;

import net.customware.gwt.dispatch.client.DispatchAsync;
import net.customware.gwt.presenter.client.EventBus;

import com.allen_sauer.gwt.log.client.Log;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;

public class EventProcessor extends Timer{

	private final EventBus eventBus;
	private final DispatchAsync dispatcher;
	private final WorkspaceContext workspaceContext;
	
	@Inject
	public EventProcessor(EventBus eventBus, DispatchAsync dispatcher, WorkspaceContext workspaceContext) {
		this.eventBus = eventBus;
		this.dispatcher = dispatcher;
		this.workspaceContext = workspaceContext;
		scheduleRepeating(3000);
	}
	
	private int lastOffset = 0;
	private boolean running = false;
	@Override
	public void run() {
		if(!running) {
			Log.info("Running timer");
			running = true;
			dispatcher.execute( new GetEventsAction(
					workspaceContext.getProjectContainerId().getId(), workspaceContext.getLocaleId(), lastOffset), 
						new AsyncCallback<GetEventsResult>() {
							public void onSuccess(GetEventsResult result) {
								for(SessionEvent e : result.getEvents()) {
									++lastOffset;
									Log.info("firing event");
									eventBus.fireEvent( getEvent(e, lastOffset));
								}
								
								running = false;
								Log.info("Got "+ result.getEvents().size() + " events");
							};
							
							private GwtEvent<?> getEvent(SessionEvent e, int offset) {
								if(e instanceof TransUnitUpdated) {
									return new TransUnitUpdatedEvent( (TransUnitUpdated) e, offset);
								}
								throw new RuntimeException("Cannot handle event");
							}

							public void onFailure(Throwable caught) {
								Log.info("failed");
								running = false;
							};
						}
			);
		}
		else{
			Log.info("Skipping timer");

		}
	}
	
}
