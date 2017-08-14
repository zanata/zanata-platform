package org.zanata.webtrans.client;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.zanata.common.ProjectType;
import org.zanata.webtrans.client.events.WorkspaceContextUpdateEvent;
import org.zanata.webtrans.shared.model.UserWorkspaceContext;
import org.zanata.webtrans.shared.rpc.SessionEventData;
import org.zanata.webtrans.shared.rpc.WorkspaceContextUpdate;
import com.allen_sauer.gwt.log.client.Log;
import com.google.gwt.user.client.rpc.AsyncCallback;

import de.novanic.eventservice.client.config.EventServiceConfigurationTransferable;
import de.novanic.eventservice.client.event.Event;
import de.novanic.eventservice.client.event.RemoteEventService;
import de.novanic.eventservice.client.event.domain.Domain;
import net.customware.gwt.presenter.client.EventBus;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static org.zanata.webtrans.test.GWTTestData.userWorkspaceContext;

/**
 * @author Patrick Huang <a
 *         href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
public class EventProcessorTest {
    private EventProcessor eventProcessor;
    @Mock
    private EventBus eventBus;
    private UserWorkspaceContext userWorkspaceContext;
    @Mock
    private RemoteEventService remoteEventService;
    @Mock
    private EventProcessor.StartCallback startCallback;
    @Captor
    private ArgumentCaptor<Domain> domainCaptor;
    @Captor
    private ArgumentCaptor<AsyncCallback<Void>> callbackCaptor;
    @Mock
    private EventServiceConfigurationTransferable configuration;

    @Before
    public void beforeMethod() {
        MockitoAnnotations.initMocks(this);
        userWorkspaceContext = userWorkspaceContext();
        EventProcessor processor =
                new EventProcessor(eventBus, userWorkspaceContext,
                        remoteEventService);
        eventProcessor = spy(processor);
        when(eventProcessor.eventServiceConfiguration()).thenReturn(
                configuration);
    }

    @Test
    public void testStart() throws Exception {
        int currentLogLevel = Log.getCurrentLogLevel();
        Log.setCurrentLogLevel(Log.LOG_LEVEL_INFO);

        eventProcessor.start(startCallback);

        verify(remoteEventService).addListener(domainCaptor.capture(),
                eq(eventProcessor), callbackCaptor.capture());
        assertThat(domainCaptor.getValue().getName())
                .isEqualTo(userWorkspaceContext.getWorkspaceContext()
                        .getWorkspaceId().toString());
        AsyncCallback<Void> callback = callbackCaptor.getValue();

        when(configuration.getConnectionId()).thenReturn("connectionId");
        callback.onSuccess(null);
        verify(startCallback).onSuccess("connectionId");

        Log.setCurrentLogLevel(Log.LOG_LEVEL_OFF);
        RuntimeException caught = new RuntimeException("Test exception");
        callback.onFailure(caught);
        verify(startCallback).onFailure(caught);

        Log.setCurrentLogLevel(currentLogLevel);
    }

    @Test
    public void callApplyWithNotSessionEvent() throws Exception {
        Event notSessionEvent = mock(Event.class);

        eventProcessor.apply(notSessionEvent);

        verifyZeroInteractions(eventBus);
    }

    @Test
    public void callApplyWithSessionEvent() {
        WorkspaceContextUpdate sessionEventData =
                new WorkspaceContextUpdate(true, ProjectType.Gettext, null);
        ArgumentCaptor<WorkspaceContextUpdateEvent> eventCaptor =
                ArgumentCaptor.forClass(WorkspaceContextUpdateEvent.class);

        eventProcessor.apply(sessionEventData);

        verify(eventBus).fireEvent(eventCaptor.capture());
    }

    @Test
    public void applyWithBogusSessionEventData() {
        SessionEventData sessionEventData = mock(SessionEventData.class);

        eventProcessor.apply(sessionEventData);

        verifyZeroInteractions(eventBus);
    }
}
