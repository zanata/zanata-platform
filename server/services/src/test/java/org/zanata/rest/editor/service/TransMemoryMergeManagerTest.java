package org.zanata.rest.editor.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.zanata.async.AsyncTaskHandle;
import org.zanata.async.AsyncTaskHandleManager;
import org.zanata.async.AsyncTaskKey;
import org.zanata.async.handle.MergeTranslationsTaskHandle;
import org.zanata.async.handle.TransMemoryMergeTaskHandle;
import org.zanata.common.LocaleId;
import org.zanata.common.ProjectType;
import org.zanata.rest.dto.VersionTMMerge;
import org.zanata.security.ZanataIdentity;
import org.zanata.rest.editor.service.TransMemoryMergeManager.TMMergeForDocTaskKey;
import org.zanata.service.TransMemoryMergeService;
import org.zanata.webtrans.shared.auth.EditorClientId;
import org.zanata.webtrans.shared.model.DocumentId;
import org.zanata.webtrans.shared.model.WorkspaceId;
import org.zanata.webtrans.shared.rest.dto.InternalTMSource;
import org.zanata.webtrans.shared.rest.dto.TransMemoryMergeCancelRequest;
import org.zanata.webtrans.shared.rest.dto.TransMemoryMergeRequest;
import org.zanata.webtrans.shared.rpc.MergeRule;
import org.zanata.webtrans.test.GWTTestData;

public class TransMemoryMergeManagerTest {

    private TransMemoryMergeManager manager;
    @Mock
    private AsyncTaskHandleManager asyncTaskHandleManager;
    @Mock
    private TransMemoryMergeService transMemoryMergeService;
    private TransMemoryMergeRequest request;
    @Mock
    private MergeTranslationsTaskHandle taskHandle;
    @Captor
    private ArgumentCaptor<TransMemoryMergeTaskHandle> docTMMergeHandleCaptor;
    @Captor
    private ArgumentCaptor<MergeTranslationsTaskHandle> versionTMMergeHandleCaptor;
    @Captor
    private ArgumentCaptor<AsyncTaskKey> taskKeyCaptor;
    private TransMemoryMergeCancelRequest cancelRequest;
    @Mock private ZanataIdentity identity;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        manager = new TransMemoryMergeManager(asyncTaskHandleManager,
                transMemoryMergeService, identity);

        WorkspaceId workspaceId = GWTTestData
                .workspaceId(LocaleId.DE, "project", "version",
                        ProjectType.Gettext);
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
    public void canCheckIfTaskIsRunning() {
        assertThat(TransMemoryMergeManager.taskIsNotRunning(null)).isTrue();
        when(taskHandle.isCancelled()).thenReturn(true);
    }

    @Test
    public void taskIsNotRunningIfItsCancelled() {
        when(taskHandle.isCancelled()).thenReturn(true);
        assertThat(TransMemoryMergeManager.taskIsNotRunning(taskHandle)).isTrue();
    }

    @Test
    public void taskIsNotRunningIfItsDone() {
        when(taskHandle.isDone()).thenReturn(true);
        assertThat(TransMemoryMergeManager.taskIsNotRunning(taskHandle)).isTrue();
    }

    @Test
    public void
            startTMMergeWillReturnTrueIfNoProcessForThisRequestIsAlreadyRunning() {
        boolean result = manager.startTransMemoryMerge(request);

        assertThat(result).isTrue();
        Mockito.verify(asyncTaskHandleManager).registerTaskHandle(
                docTMMergeHandleCaptor.capture(), taskKeyCaptor.capture());
        TransMemoryMergeTaskHandle handle = docTMMergeHandleCaptor.getValue();
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
                .thenAnswer(it -> existingHandle);
        boolean result = manager.startTransMemoryMerge(request);

        assertThat(result).isTrue();
        Mockito.verify(asyncTaskHandleManager).registerTaskHandle(
                docTMMergeHandleCaptor.capture(), taskKeyCaptor.capture());
        TransMemoryMergeTaskHandle handle = docTMMergeHandleCaptor.getValue();
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
                .thenAnswer(it -> existingHandle);
        boolean result = manager.startTransMemoryMerge(request);

        assertThat(result).isTrue();
        Mockito.verify(asyncTaskHandleManager).registerTaskHandle(
                docTMMergeHandleCaptor.capture(), taskKeyCaptor.capture());
        TransMemoryMergeTaskHandle handle = docTMMergeHandleCaptor.getValue();
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
        TransMemoryMergeTaskHandle handle = new TransMemoryMergeTaskHandle();
        when(asyncTaskHandleManager.getHandleByKey(taskKey))
                .thenAnswer(it -> handle);
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
                .thenAnswer(it -> existingHandle);
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
                .thenAnswer(it -> existingHandle);
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
                .thenAnswer(it -> existingHandle);
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
                .thenAnswer(it -> existingHandle);
        boolean result = manager.cancelTransMemoryMerge(cancelRequest);

        assertThat(result).isTrue();
    }

    @Test
    public void
    startTMMergeForVersionIfNoProcessForThisRequestIsAlreadyRunning() {
        VersionTMMerge versionTMMerge =
                new VersionTMMerge(LocaleId.FR, 80, MergeRule.FUZZY, MergeRule.FUZZY,
                        MergeRule.FUZZY, MergeRule.FUZZY,
                        InternalTMSource.SELECT_ALL);
        long versionId = 1L;
        AsyncTaskHandle<Void> result = manager.start(versionId, versionTMMerge);

        Mockito.verify(asyncTaskHandleManager).registerTaskHandle(
                versionTMMergeHandleCaptor.capture(), taskKeyCaptor.capture());
        MergeTranslationsTaskHandle handle = versionTMMergeHandleCaptor.getValue();
        assertThat(result).isSameAs(handle);
        assertThat(handle.getTriggeredBy())
                .isEqualTo(identity.getAccountUsername());
        Mockito.verify(transMemoryMergeService).startMergeTranslations(
                versionId, versionTMMerge,
                handle);
    }

    @Test
    public void
    startTMMergeForVersionIfProcessForThisRequestIsAlreadyRunning() {
        long versionId = 1L;
        LocaleId localeId = LocaleId.FR;
        when(asyncTaskHandleManager.getHandleByKey(
                TransMemoryMergeManager.makeKey(versionId, localeId)))
                .thenAnswer(it -> taskHandle);
        VersionTMMerge versionTMMerge =
                new VersionTMMerge(localeId, 80, MergeRule.FUZZY, MergeRule.FUZZY,
                        MergeRule.FUZZY, MergeRule.FUZZY,
                        InternalTMSource.SELECT_ALL);

        assertThatThrownBy(() -> manager.start(versionId, versionTMMerge))
                .isInstanceOf(UnsupportedOperationException.class)
                .hasMessage("there is already a task running for version and locale");

        Mockito.verifyZeroInteractions(transMemoryMergeService);
    }

}
