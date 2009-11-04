package org.fedorahosted.flies.webtrans.client;

import java.util.HashMap;

import net.customware.gwt.dispatch.client.DispatchAsync;
import net.customware.gwt.presenter.client.EventBus;

import org.fedorahosted.flies.gwt.rpc.GetEventsAction;
import org.fedorahosted.flies.gwt.rpc.GetEventsResult;
import org.fedorahosted.flies.gwt.rpc.HasTransUnitUpdatedData;
import org.fedorahosted.flies.gwt.rpc.SessionEvent;
import org.fedorahosted.flies.gwt.rpc.SessionEventData;
import org.fedorahosted.flies.gwt.rpc.TransUnitUpdated;
import org.fedorahosted.flies.webtrans.client.events.TransUnitUpdatedEvent;

import com.allen_sauer.gwt.log.client.Log;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;

public class EventProcessor extends Timer{

	private final EventBus eventBus;
	private final DispatchAsync dispatcher;
	private final WorkspaceContext workspaceContext;
	
	private interface EventFactory<T extends GwtEvent<?>> {
		T create(SessionEvent<?> event);
	}
	
	private static class EventRegistry{
		HashMap<Class<? extends SessionEventData>, EventFactory<?>> factories = 
			new HashMap<Class<? extends SessionEventData>, EventFactory<?>>();

		public EventRegistry() {
			
			// put any additional factories here
			factories.put(TransUnitUpdated.class, new EventFactory<TransUnitUpdatedEvent>() {
				@Override
				public TransUnitUpdatedEvent create(SessionEvent<?> event) {
					return new TransUnitUpdatedEvent(
							(HasTransUnitUpdatedData) event.getData(), event.getSequence());
				}
			});
		}
		
		public GwtEvent<?> getEvent(SessionEvent<?> sessionEvent) {
			EventFactory<?> factory = factories.get(sessionEvent.getData().getClass());
			if(factories == null) {
				Log.warn("Could not find factory for class " +
						sessionEvent.getData().getClass());
				return null;
			}
			return factory.create(sessionEvent);
		}
	}
	
	private final EventRegistry eventRegistry;
	
	@Inject
	public EventProcessor(EventBus eventBus, DispatchAsync dispatcher, WorkspaceContext workspaceContext) {
		this.eventBus = eventBus;
		this.dispatcher = dispatcher;
		this.workspaceContext = workspaceContext;
		this.eventRegistry = new EventRegistry();
		scheduleRepeating(3000);
	}
	
	private int lastSequence = 0;
	private boolean running = false;
	@Override
	public void run() {
		if(!running) {
			running = true;
			dispatcher.execute( new GetEventsAction(
					workspaceContext.getProjectContainerId().getId(), workspaceContext.getLocaleId(), lastSequence), 
						new AsyncCallback<GetEventsResult>() {
							public void onSuccess(GetEventsResult result) {
								for(SessionEvent<?> e : result.getEvents()) {
									lastSequence = e.getSequence();
									GwtEvent<?> event = eventRegistry.getEvent(e);
									if( event != null) eventBus.fireEvent( event );
								}
								
								running = false;
							};
							
							public void onFailure(Throwable caught) {
								running = false;
							};
						}
			);
		}
	}
	
}
