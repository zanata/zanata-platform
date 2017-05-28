package org.zanata.service.impl;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import javax.enterprise.inject.Produces;
import javax.inject.Inject;

import org.hibernate.event.spi.PostUpdateEvent;
import org.hibernate.persister.entity.EntityPersister;
import org.jglue.cdiunit.deltaspike.SupportDeltaspikeCore;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.zanata.async.AsyncTaskHandle;
import org.zanata.async.AsyncTaskHandleManager;
import org.zanata.model.HAccount;
import org.zanata.model.HProject;
import org.zanata.model.HProjectIteration;
import org.zanata.service.IndexingService;
import org.zanata.test.CdiUnitRunner;

@RunWith(CdiUnitRunner.class)
@SupportDeltaspikeCore
public class SlugEntityUpdatedListenerTest {

    @Inject
    private SlugEntityUpdatedListener listener;
    @Mock
    private PostUpdateEvent event;
    @Mock
    private EntityPersister persister;
    @Captor
    private ArgumentCaptor<AsyncTaskHandle> asyncTaskHandleArgumentCaptor;

    @Produces
    @Mock
    AsyncTaskHandleManager taskHandleManager;

    @Produces
    @Mock
    IndexingService indexingService;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void willIgnoreIfNotHProjectOrHProjectIteration() {
        when(event.getEntity()).thenReturn(new HAccount());

        listener.onPostUpdate(event);

        verify(event).getEntity();
        verifyNoMoreInteractions(event);
    }

    @Test
    public void willFireProjectUpdatedEventForHProject() throws Exception {
        HProject entity = new HProject();
        when(event.getEntity()).thenReturn(entity);
        when(event.getPersister()).thenReturn(persister);
        // slug is the first property
        when(persister.getPropertyNames())
                .thenReturn(new String[] { "slug", "name" });
        when(event.getOldState())
                .thenReturn(new String[] { "old-slug", "name" });
        when(event.getState()).thenReturn(new String[] { "new-slug", "name" });

        listener.onPostUpdate(event);

        verify(taskHandleManager)
                .registerTaskHandle(asyncTaskHandleArgumentCaptor.capture());
        verify(indexingService).reindexHTextFlowTargetsForProject(entity,
                asyncTaskHandleArgumentCaptor.getValue());
    }

    @Test
    public void willFireProjectUpdatedEventForHProjectIteration() throws Exception {
        HProjectIteration entity = new HProjectIteration();
        when(event.getEntity()).thenReturn(entity);
        when(event.getPersister()).thenReturn(persister);
        // slug is the first property
        when(persister.getPropertyNames())
                .thenReturn(new String[] { "slug" });
        when(event.getOldState())
                .thenReturn(new String[] { "old-slug" });
        when(event.getState()).thenReturn(new String[] { "new-slug" });

        listener.onPostUpdate(event);

        verify(taskHandleManager)
                .registerTaskHandle(asyncTaskHandleArgumentCaptor.capture());
        verify(indexingService).reindexHTextFlowTargetsForProjectIteration(entity,
                asyncTaskHandleArgumentCaptor.getValue());
    }

}
