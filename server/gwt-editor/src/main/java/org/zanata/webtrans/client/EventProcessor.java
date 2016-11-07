package org.zanata.webtrans.client;

import java.util.HashMap;

import de.novanic.eventservice.client.config.EventServiceConfigurationTransferable;
import net.customware.gwt.presenter.client.EventBus;

import org.zanata.webtrans.client.events.CommentChangedEvent;
import org.zanata.webtrans.client.events.EnterWorkspaceEvent;
import org.zanata.webtrans.client.events.ExitWorkspaceEvent;
import org.zanata.webtrans.client.events.PublishWorkspaceChatEvent;
import org.zanata.webtrans.client.events.TransUnitEditEvent;
import org.zanata.webtrans.client.events.TransUnitUpdatedEvent;
import org.zanata.webtrans.client.events.WorkspaceContextUpdateEvent;
import org.zanata.webtrans.shared.model.UserWorkspaceContext;
import org.zanata.webtrans.shared.rpc.AddReviewComment;
import org.zanata.webtrans.shared.rpc.EnterWorkspace;
import org.zanata.webtrans.shared.rpc.ExitWorkspace;
import org.zanata.webtrans.shared.rpc.HasEnterWorkspaceData;
import org.zanata.webtrans.shared.rpc.HasExitWorkspaceData;
import org.zanata.webtrans.shared.rpc.HasTransUnitEditData;
import org.zanata.webtrans.shared.rpc.HasTransUnitUpdatedData;
import org.zanata.webtrans.shared.rpc.HasWorkspaceChatData;
import org.zanata.webtrans.shared.rpc.HasWorkspaceContextUpdateData;
import org.zanata.webtrans.shared.rpc.PublishWorkspaceChat;
import org.zanata.webtrans.shared.rpc.SessionEventData;
import org.zanata.webtrans.shared.rpc.TransUnitEdit;
import org.zanata.webtrans.shared.rpc.TransUnitUpdated;
import org.zanata.webtrans.shared.rpc.WorkspaceContextUpdate;

import com.allen_sauer.gwt.log.client.Log;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.UmbrellaException;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;

import de.novanic.eventservice.client.config.ConfigurationTransferableDependentFactory;
import de.novanic.eventservice.client.event.Event;
import de.novanic.eventservice.client.event.RemoteEventService;
import de.novanic.eventservice.client.event.domain.Domain;
import de.novanic.eventservice.client.event.domain.DomainFactory;
import de.novanic.eventservice.client.event.listener.RemoteEventListener;

public class EventProcessor implements RemoteEventListener {

    private interface EventFactory<T extends GwtEvent<?>> {
        T create(SessionEventData event);
    }

    public static interface StartCallback {
        void onSuccess(String connectionId);

        void onFailure(Throwable e);
    }

    private static class EventRegistry {
        HashMap<Class<? extends SessionEventData>, EventFactory<?>> factories =
                new HashMap<Class<? extends SessionEventData>, EventFactory<?>>();

        public EventRegistry() {

            // put any additional factories here
            factories.put(TransUnitUpdated.class,
                    (EventFactory<TransUnitUpdatedEvent>) event ->
                            new TransUnitUpdatedEvent(
                                    (HasTransUnitUpdatedData) event));

            factories.put(ExitWorkspace.class,
                    (EventFactory<ExitWorkspaceEvent>) event ->
                            new ExitWorkspaceEvent(
                                    (HasExitWorkspaceData) event));

            factories.put(EnterWorkspace.class,
                    (EventFactory<EnterWorkspaceEvent>) event ->
                            new EnterWorkspaceEvent(
                                    (HasEnterWorkspaceData) event));

            factories.put(WorkspaceContextUpdate.class,
                    (EventFactory<WorkspaceContextUpdateEvent>) event ->
                            new WorkspaceContextUpdateEvent(
                                    (HasWorkspaceContextUpdateData) event));

            factories.put(TransUnitEdit.class,
                    (EventFactory<TransUnitEditEvent>) event ->
                            new TransUnitEditEvent(
                                    (HasTransUnitEditData) event));

            factories.put(PublishWorkspaceChat.class,
                    (EventFactory<PublishWorkspaceChatEvent>) event ->
                            new PublishWorkspaceChatEvent(
                                    (HasWorkspaceChatData) event));
            factories.put(AddReviewComment.class,
                    (EventFactory<CommentChangedEvent>) event -> {
                        AddReviewComment comment = (AddReviewComment) event;
                        return new CommentChangedEvent(comment.getTransUnitId(), comment.getCommentCount());
                    });
        }

        public GwtEvent<?> getEvent(SessionEventData sessionEventData) {
            EventFactory<?> factory =
                    factories.get(sessionEventData.getClass());
            if (factory == null) {
                Log.warn("Could not find factory for class "
                        + sessionEventData.getClass());
                return null;
            }
            return factory.create(sessionEventData);
        }
    }

    private final EventRegistry eventRegistry;
    private final RemoteEventService remoteEventService;
    private final Domain domain;
    private final EventBus eventBus;

    @Inject
    public EventProcessor(EventBus eventBus,
            UserWorkspaceContext userWorkspaceContext,
            RemoteEventService remoteEventService) {
        this.eventBus = eventBus;
        this.remoteEventService = remoteEventService;
        this.eventRegistry = new EventRegistry();
        this.domain =
                DomainFactory.getDomain(userWorkspaceContext
                        .getWorkspaceContext().getWorkspaceId().toString());
    }

    public void start(final StartCallback callback) {
        remoteEventService.addListener(domain, this, new AsyncCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                Log.info("EventProcessor is now listening for events in the domain "
                        + domain.getName());
                String connectionId =
                        eventServiceConfiguration().getConnectionId();
                callback.onSuccess(connectionId);
            }

            @Override
            public void onFailure(Throwable e) {
                Log.error("Failed to start EventProcessor", e);
                callback.onFailure(e);
            }
        });
    }

    /**
     * mark as protected so that we can mock and test in unit test.
     *
     * @return EventServiceConfigurationTransferable
     */
    protected EventServiceConfigurationTransferable eventServiceConfiguration() {
        return ConfigurationTransferableDependentFactory.getConfiguration();
    }

    @Override
    public void apply(Event event) {
        // Log.info("received remote event "+event);
        if (event instanceof SessionEventData) {
            SessionEventData ed = (SessionEventData) event;
            GwtEvent<?> gwtEvent = eventRegistry.getEvent(ed);
            if (gwtEvent != null) {
                Log.debug("received event " + event + ", GWT event "
                        + gwtEvent.getClass().getName());
                try {
                    eventBus.fireEvent(gwtEvent);
                } catch (UmbrellaException e) {
                    Log.error("Event failed", e);
                }
            } else {
                Log.warn("unknown event " + event);
            }
        }
    }

}
