package org.zanata.rest.editor.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.zanata.async.AsyncTaskHandleManager;
import org.zanata.async.AsyncTaskKey;
import org.zanata.async.handle.TransMemoryMergeTaskHandle;
import org.zanata.common.LocaleId;
import org.zanata.common.ProjectType;
import org.zanata.model.HAccount;
import org.zanata.model.TestFixture;
import org.zanata.security.ZanataIdentity;
import org.zanata.rest.editor.service.TransMemoryMergeManager.TMMergeForDocTaskKey;
import org.zanata.service.TransMemoryMergeService;
import org.zanata.webtrans.shared.auth.EditorClientId;
import org.zanata.webtrans.shared.model.DocumentId;
import org.zanata.webtrans.shared.model.WorkspaceId;
import org.zanata.webtrans.shared.rest.dto.TransMemoryMergeCancelRequest;
import org.zanata.webtrans.shared.rest.dto.TransMemoryMergeRequest;
import org.zanata.webtrans.shared.rpc.MergeRule;

public class TransMemoryMergeManagerTest {

    private TransMemoryMergeManager manager;
    @Mock
    private AsyncTaskHandleManager asyncTaskHandleManager;
    @Mock
    private TransMemoryMergeService transMemoryMergeService;
    private HAccount authenticated;
    private TransMemoryMergeRequest request;
    @Captor
    private ArgumentCaptor<TransMemoryMergeTaskHandle> handleCaptor;
    @Captor
    private ArgumentCaptor<AsyncTaskKey> taskKeyCaptor;
    private TransMemoryMergeCancelRequest cancelRequest;
    @Mock private ZanataIdentity identity;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        manager = new TransMemoryMergeManager(asyncTaskHandleManager,
                transMemoryMergeService, identity);

        WorkspaceId workspaceId = TestFixture.workspaceId(LocaleId.DE,
                "project", "version", ProjectType.Gettext);
        EditorClientId editorClientId = new EditorClientId("session", 1);
        DocumentId documentId = new DocumentId(1L, "doc");
        request = new TransMemoryMergeRequest(editorClientId,
                workspaceId.getProjectIterationId(), documentId,
                workspaceId.getLocaleId(), 100, MergeRule.FUZZY,
                MergeRule.FUZZY, MergeRule.FUZZY, MergeRule.FUZZY);

        cancelRequest = new TransMemoryMergeCancelRequest(
                workspaceId.getProjectIterationId(), documentId,
                workspaceId.getLocaleId());

        when(identity.getAccountUsername()).thenReturn("admin");
    }

    @Test
    public void
            startTMMergeWillReturnTrueIfNoProcessForThisRequestIsAlreadyRunning() {
        boolean result = manager.startTransMemoryMerge(request);

        assertThat(result).isTrue();
        Mockito.verify(asyncTaskHandleManager).registerTaskHandle(
                handleCaptor.capture(), taskKeyCaptor.capture());
        TransMemoryMergeTaskHandle handle = handleCaptor.getValue();
        assertThat(handle.getTriggeredBy())
                .isEqualTo(identity.getAccountUsername());
        Mockito.verify(transMemoryMergeService).executeMergeAsync(request,
                handle);
    }

    @Test
    public void startTMMergeWillReturnTrueIfProcessForThisRequestIsCancelled() {
        TransMemoryMergeTaskHandle existingHandle =
                new TransMemoryMergeTaskHandle() {
                    private static final long serialVersionUID = 1L;
                    @Override
                    public boolean isCancelled() {
                        return true;
                    }
                };

        TMMergeForDocTaskKey taskKey =
                new TMMergeForDocTaskKey(
                        request.documentId,
                        request.localeId);
        when(asyncTaskHandleManager.getHandleByKey(taskKey))
                .thenReturn(existingHandle);
        boolean result = manager.startTransMemoryMerge(request);

        assertThat(result).isTrue();
        Mockito.verify(asyncTaskHandleManager).registerTaskHandle(
                handleCaptor.capture(), taskKeyCaptor.capture());
        TransMemoryMergeTaskHandle handle = handleCaptor.getValue();
        assertThat(handle.getTriggeredBy())
                .isEqualTo(identity.getAccountUsername());
        Mockito.verify(transMemoryMergeService).executeMergeAsync(request,
                handle);
    }

    @Test
    public void startTMMergeWillReturnTrueIfProcessForThisRequestIsDone() {
        TransMemoryMergeTaskHandle existingHandle =
                new TransMemoryMergeTaskHandle() {
                    private static final long serialVersionUID = 1L;
                    @Override
                    public boolean isDone() {
                        return true;
                    }
                };

        TMMergeForDocTaskKey taskKey =
                new TMMergeForDocTaskKey(
                        request.documentId,
                        request.localeId);
        when(asyncTaskHandleManager.getHandleByKey(taskKey))
                .thenReturn(existingHandle);
        boolean result = manager.startTransMemoryMerge(request);

        assertThat(result).isTrue();
        Mockito.verify(asyncTaskHandleManager).registerTaskHandle(
                handleCaptor.capture(), taskKeyCaptor.capture());
        TransMemoryMergeTaskHandle handle = handleCaptor.getValue();
        assertThat(handle.getTriggeredBy())
                .isEqualTo(identity.getAccountUsername());
        Mockito.verify(transMemoryMergeService).executeMergeAsync(request,
                handle);
    }

    @Test
    public void
            startTMMergeWillReturnFalseIfProcessForSameRequestIsAlreadyRunning() {
        TMMergeForDocTaskKey taskKey =
                new TMMergeForDocTaskKey(
                        request.documentId,
                        request.localeId);
        when(asyncTaskHandleManager.getHandleByKey(taskKey))
                .thenReturn(new TransMemoryMergeTaskHandle());
        boolean result = manager.startTransMemoryMerge(request);

        assertThat(result).isFalse();
        Mockito.verifyZeroInteractions(transMemoryMergeService);
    }

    @Test
    public void cancelTMMergeWillReturnFalseIfNoProcessForThisRequest() {
        boolean result = manager.cancelTransMemoryMerge(cancelRequest);

        assertThat(result).isFalse();
    }

    @Test
    public void cancelTMMergeWillReturnFalseIfProcessForThisRequestIsDone() {
        TransMemoryMergeTaskHandle existingHandle =
                new TransMemoryMergeTaskHandle() {
                    private static final long serialVersionUID = 1L;
                    @Override
                    public boolean isDone() {
                        return true;
                    }
                };

        TMMergeForDocTaskKey taskKey =
                new TMMergeForDocTaskKey(
                        cancelRequest.documentId,
                        cancelRequest.localeId);
        when(asyncTaskHandleManager.getHandleByKey(taskKey))
                .thenReturn(existingHandle);
        boolean result = manager.cancelTransMemoryMerge(cancelRequest);

        assertThat(result).isFalse();
    }

    @Test
    public void cancelTMMergeWillReturnFalseIfProcessForThisRequestIsCancelled() {
        TransMemoryMergeTaskHandle existingHandle =
                new TransMemoryMergeTaskHandle() {
                    private static final long serialVersionUID = 1L;
                    @Override
                    public boolean isCancelled() {
                        return true;
                    }
                };

        TMMergeForDocTaskKey taskKey =
                new TMMergeForDocTaskKey(
                        cancelRequest.documentId,
                        cancelRequest.localeId);
        when(asyncTaskHandleManager.getHandleByKey(taskKey))
                .thenReturn(existingHandle);
        boolean result = manager.cancelTransMemoryMerge(cancelRequest);

        assertThat(result).isFalse();
    }

    @Test
    public void cancelTMMergeWillReturnFalseIfProcessForThisRequestIsNotTriggeredByTheSamePerson() {
        TransMemoryMergeTaskHandle existingHandle =
                new TransMemoryMergeTaskHandle();
        existingHandle.setTriggeredBy("someone else");

        TMMergeForDocTaskKey taskKey =
                new TMMergeForDocTaskKey(
                        cancelRequest.documentId,
                        cancelRequest.localeId);
        when(asyncTaskHandleManager.getHandleByKey(taskKey))
                .thenReturn(existingHandle);
        boolean result = manager.cancelTransMemoryMerge(cancelRequest);

        assertThat(result).isFalse();
    }

    @Test
    public void cancelTMMergeWillReturnTrueIfProcessForThisRequestIsRunningAndTriggeredByTheSamePerson() {
        TransMemoryMergeTaskHandle existingHandle =
                new TransMemoryMergeTaskHandle() {
                    private static final long serialVersionUID = 1L;
                    @Override
                    public boolean cancel(boolean mayInterruptIfRunning) {
                        return true;
                    }
                };
        existingHandle.setTriggeredBy(identity.getAccountUsername());

        TMMergeForDocTaskKey taskKey =
                new TMMergeForDocTaskKey(
                        cancelRequest.documentId,
                        cancelRequest.localeId);
        when(asyncTaskHandleManager.getHandleByKey(taskKey))
                .thenReturn(existingHandle);
        boolean result = manager.cancelTransMemoryMerge(cancelRequest);

        assertThat(result).isTrue();
    }

}
