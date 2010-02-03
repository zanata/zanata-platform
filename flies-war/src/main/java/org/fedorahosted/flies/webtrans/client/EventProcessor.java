package org.fedorahosted.flies.webtrans.client;

import java.util.ArrayList;
import java.util.HashMap;

import net.customware.gwt.presenter.client.EventBus;

import org.fedorahosted.flies.gwt.rpc.EnterWorkspace;
import org.fedorahosted.flies.gwt.rpc.ExitWorkspace;
import org.fedorahosted.flies.gwt.rpc.HasEnterWorkspaceData;
import org.fedorahosted.flies.gwt.rpc.HasExitWorkspaceData;
import org.fedorahosted.flies.gwt.rpc.HasTransUnitEditData;
import org.fedorahosted.flies.gwt.rpc.HasTransUnitUpdatedData;
import org.fedorahosted.flies.gwt.rpc.SessionEventData;
import org.fedorahosted.flies.gwt.rpc.SessionEventMessageParts;
import org.fedorahosted.flies.gwt.rpc.TransUnitEditing;
import org.fedorahosted.flies.gwt.rpc.TransUnitUpdated;
import org.fedorahosted.flies.webtrans.client.events.EnterWorkspaceEvent;
import org.fedorahosted.flies.webtrans.client.events.ExitWorkspaceEvent;
import org.fedorahosted.flies.webtrans.client.events.TransUnitEditEvent;
import org.fedorahosted.flies.webtrans.client.events.TransUnitUpdatedEvent;
import org.fedorahosted.flies.webtrans.client.rpc.CachingDispatchAsync;
import org.jboss.errai.bus.client.ErraiBus;
import org.jboss.errai.bus.client.Message;
import org.jboss.errai.bus.client.MessageBus;
import org.jboss.errai.bus.client.MessageCallback;

import com.allen_sauer.gwt.log.client.Log;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;

public class EventProcessor implements /*RemoteEventListener*/ MessageCallback {

	private final EventBus eventBus;
//	private final DispatchAsync dispatcher;
//	private final WorkspaceContext workspaceContext;
	
	private interface EventFactory<T extends GwtEvent<?>> {
		T create(SessionEventData event);
	}
	
	private static class EventRegistry{
		HashMap<Class<? extends SessionEventData>, EventFactory<?>> factories = 
			new HashMap<Class<? extends SessionEventData>, EventFactory<?>>();

		public EventRegistry() {
			
			// put any additional factories here
			factories.put(TransUnitUpdated.class, new EventFactory<TransUnitUpdatedEvent>() {
				@Override
				public TransUnitUpdatedEvent create(SessionEventData event) {
					return new TransUnitUpdatedEvent(
							(HasTransUnitUpdatedData) event);
				}
			});
			
			factories.put(TransUnitEditing.class, new EventFactory<TransUnitEditEvent>() {
				@Override
				public TransUnitEditEvent create(SessionEventData event) {
					return new TransUnitEditEvent(
							(HasTransUnitEditData) event);
				}
			});
			
			factories.put(ExitWorkspace.class, new EventFactory<ExitWorkspaceEvent>() {
				@Override
				public ExitWorkspaceEvent create(SessionEventData event) {
					return new ExitWorkspaceEvent(
							(HasExitWorkspaceData) event);
				}
			});
			
			factories.put(EnterWorkspace.class, new EventFactory<EnterWorkspaceEvent>() {
				@Override
				public EnterWorkspaceEvent create(SessionEventData event) {
					return new EnterWorkspaceEvent(
							(HasEnterWorkspaceData) event);
				}
			});
		}
				
		public GwtEvent<?> getEvent(SessionEventData sessionEventData) {
			EventFactory<?> factory = factories.get(sessionEventData.getClass());
			if(factories == null) {
				Log.warn("Could not find factory for class " +
						sessionEventData.getClass());
				return null;
			}
			return factory.create(sessionEventData);
		}
	}
	
	private final EventRegistry eventRegistry;
	private MessageBus messageBus;
//	private ArrayList<AsyncCallback<Void>> waitingCallbacks = new ArrayList<AsyncCallback<Void>>();
//	private boolean registerSucceeded = false;
	
	@Inject
	public EventProcessor(EventBus eventBus, CachingDispatchAsync dispatcher, WorkspaceContext workspaceContext) {
		this.eventBus = eventBus;
//		this.dispatcher = dispatcher;
//		this.workspaceContext = workspaceContext;
		this.eventRegistry = new EventRegistry();
		this.messageBus = ErraiBus.get();
		messageBus.subscribe(workspaceContext.getSubject(), this);
		Log.info("Subscribed to ErraiBus subject "+workspaceContext.getSubject());
		// FIXME make sure we don't lose events at init time
//		remoteEventService.addListener(domain, this, new AsyncCallback<Void>() {
//			@Override
//			public void onSuccess(Void result) {
//				Log.info("EventProcessor is now listening for events in the domain "+domain.getName());
//				for (AsyncCallback<Void> callback : waitingCallbacks)
//					callback.onSuccess(result);
//				waitingCallbacks = null;
//				registerSucceeded = true;
//			}
//			@Override
//			public void onFailure(Throwable e) {
//				Log.error(e.getMessage(), e);
//				for (AsyncCallback<Void> callback : waitingCallbacks)
//					callback.onFailure(e);
//				waitingCallbacks = null;
//				registerSucceeded = false;
//			}
//		});
	}
	
//	/**
//	 * Calls back when the EventProcessor is registered with the domain.
//	 * Callback is immediate if already registered.
//	 * @param callback
//	 */
//	public void addCallback(AsyncCallback<Void> callback) {
//		if (waitingCallbacks != null)
//			waitingCallbacks.add(callback);
//		// we already got our callback, so pass the result straight back:
//		else if (registerSucceeded)
//			callback.onSuccess(null);
//		else
//			callback.onFailure(new Exception());
//	}
	
	
	@Override
	public void callback(Message message) {
		SessionEventData ed = message.get(SessionEventData.class, SessionEventMessageParts.Data);
if(ed == null) { Log.error("Data=null: "+message); return;}
		GwtEvent<?> gwtEvent = eventRegistry.getEvent(ed);
		if(gwtEvent != null) {
			Log.info("received event "+gwtEvent);
			eventBus.fireEvent(gwtEvent);
		} else {
			Log.warn("unknown event "+ed);
		}
	}
	
}
