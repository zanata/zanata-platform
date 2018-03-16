/*
 * Copyright 2012, Red Hat, Inc. and individual contributors as indicated by the
 * @author tags. See the copyright.txt file in the distribution for a full
 * listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this software; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
 * site: http://www.fsf.org.
 */

package org.zanata.webtrans.client.presenter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

import java.util.Date;

import org.fusesource.restygwt.client.Method;
import org.fusesource.restygwt.client.MethodCallback;
import org.fusesource.restygwt.client.callback.CallbackAware;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Answers;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.zanata.common.LocaleId;
import org.zanata.common.ProjectType;
import org.zanata.webtrans.client.events.NotificationEvent;
import org.zanata.webtrans.client.events.TMMergeProgressEvent;
import org.zanata.webtrans.client.events.TMMergeStartOrEndEvent;
import org.zanata.webtrans.client.resources.UiMessages;
import org.zanata.webtrans.client.ui.TransMemoryMergePopupPanelDisplay;
import org.zanata.webtrans.shared.auth.EditorClientId;
import org.zanata.webtrans.shared.auth.Identity;
import org.zanata.webtrans.shared.model.DocumentId;
import org.zanata.webtrans.shared.model.DocumentInfo;
import org.zanata.webtrans.shared.model.ProjectIterationId;
import org.zanata.webtrans.shared.model.UserWorkspaceContext;
import org.zanata.webtrans.shared.model.WorkspaceContext;
import org.zanata.webtrans.shared.model.WorkspaceId;
import org.zanata.webtrans.shared.model.WorkspaceRestrictions;
import org.zanata.webtrans.shared.rest.TransMemoryMergeResource;
import org.zanata.webtrans.shared.rest.dto.TransMemoryMergeCancelRequest;
import org.zanata.webtrans.shared.rest.dto.TransMemoryMergeRequest;
import org.zanata.webtrans.shared.rpc.MergeOptions;
import org.zanata.webtrans.shared.rpc.MergeRule;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import net.customware.gwt.presenter.client.EventBus;

/**
 * @author Patrick Huang <a
 *         href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
public class TransMemoryMergePresenterTest {
    private TransMemoryMergePresenter presenter;
    @Mock
    private TransMemoryMergePopupPanelDisplay display;
    @Mock
    private EventBus eventBus;
    @Mock
    private UiMessages messages;
    @Captor
    private ArgumentCaptor<NotificationEvent> notificationEventCaptor;
    private FakeTransMemoryMergeResource mergeResource;
    @Mock
    private Identity identity;
    private WorkspaceId workspaceId;
    private DocumentId documentId;
    private EditorClientId editorClientId;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        ProjectIterationId projectIterationId =
                new ProjectIterationId("project", "master",
                        ProjectType.Gettext);
        workspaceId = new WorkspaceId(
                projectIterationId, LocaleId.DE);
        WorkspaceContext workspaceContext = new WorkspaceContext(
                workspaceId, "de", "de");
        WorkspaceRestrictions workspaceRestrictions =
                new WorkspaceRestrictions(true, false, true, false, false);
        UserWorkspaceContext userWorkspaceContext =
                new UserWorkspaceContext(workspaceContext,
                        workspaceRestrictions, Lists.newArrayList());
        documentId = new DocumentId(1L, "path/book");
        userWorkspaceContext
                .setSelectedDoc(new DocumentInfo(
                        documentId, "book", "path/", LocaleId.EN, null, null,
                Maps.newHashMap(), null));

        mergeResource = new FakeTransMemoryMergeResource();

        editorClientId = new EditorClientId("session", 1L);
        when(identity.getEditorClientId()).thenReturn(editorClientId);

        presenter =
                new TransMemoryMergePresenter(display, eventBus,
                        mergeResource, identity, userWorkspaceContext,
                        messages);

        verify(display).setListener(presenter);


    }

    @Test
    public void willShowFormOnPrepareTMMerge() {
        presenter.prepareTMMerge();

        verify(display).showForm();
    }

    @Test
    public void willHideFormOnCancel() {
        presenter.cancelMergeTM();

        verify(display).hide();
    }

    @Test
    public void canRequestTMMerge() {
        // Given:

        // When:
        MergeOptions opts = MergeOptions.allFuzzy();
        opts.setDifferentDocument(MergeRule.REJECT);
        presenter.proceedToMergeTM(80, opts);

        // Then:
        assertThat(mergeResource.request).isNotNull();

        assertThat(mergeResource.request.getDifferentDocumentRule()).isEqualTo(MergeRule.REJECT);
        assertThat(mergeResource.request.getDifferentProjectRule()).isEqualTo(MergeRule.FUZZY);
        assertThat(mergeResource.request.getDifferentContextRule()).isEqualTo(MergeRule.FUZZY);
        assertThat(mergeResource.request.getImportedMatchRule()).isEqualTo(MergeRule.FUZZY);
        assertThat(mergeResource.request.getThresholdPercent()).isEqualTo(80);
        assertThat(mergeResource.request.editorClientId).isSameAs(editorClientId);
        assertThat(mergeResource.request.documentId).isSameAs(documentId);
        assertThat(mergeResource.request.localeId).isSameAs(workspaceId.getLocaleId());

    }

    @Test
    public void onRequestTMMergeFailureDueToSomeoneRunningItWillHideFormAndNotify() {
        // When:
        MergeOptions opts = MergeOptions.allFuzzy();
        opts.setDifferentProject(MergeRule.REJECT);
        presenter.proceedToMergeTM(100, opts);

        assertThat(mergeResource.callback).isNotNull();
        // REST call failed
        Method method = Mockito.mock(Method.class, withSettings().defaultAnswer(
                Answers.RETURNS_DEEP_STUBS));
        when(method.getResponse().getStatusCode()).thenReturn(400);
        mergeResource.callback.onFailure(method, new RuntimeException("Die!!!!!"));

        // Then:
        verify(messages).mergeTMStartedBySomeone(documentId.getDocId());
        verify(eventBus).fireEvent(notificationEventCaptor.capture());
        verify(display).hide();
        NotificationEvent event = notificationEventCaptor.getValue();
        assertThat(event.getSeverity()).isEqualTo(NotificationEvent.Severity.Warning);
        assertThat(event.getMessage()).isSameAs(messages.mergeTMStartedBySomeone(documentId.getDocId()));
        assertThat(presenter.isMergeStarted()).isFalse();
    }

    @Test
    public void onRequestTMMergeFailureOnOtherErrorWillHideFormAndNotify() {
        // When:
        MergeOptions opts = MergeOptions.allFuzzy();
        opts.setDifferentProject(MergeRule.REJECT);
        presenter.proceedToMergeTM(100, opts);

        assertThat(mergeResource.callback).isNotNull();
        // REST call failed
        Method method = Mockito.mock(Method.class, withSettings().defaultAnswer(
                Answers.RETURNS_DEEP_STUBS));
        when(method.getResponse().getStatusCode()).thenReturn(500);
        mergeResource.callback.onFailure(method, new RuntimeException("Die!!!!!"));

        // Then:
        verify(messages).mergeTMFailed();
        verify(eventBus).fireEvent(notificationEventCaptor.capture());
        verify(display).hide();
        NotificationEvent event = notificationEventCaptor.getValue();
        assertThat(event.getSeverity()).isEqualTo(NotificationEvent.Severity.Error);
        assertThat(event.getMessage()).isSameAs(messages.mergeTMFailed());
        assertThat(presenter.isMergeStarted()).isFalse();
    }

    @Test
    public void
            onRequestTMMergeSuccessWithStartTheAsyncMergeOnServer() {
        // Given:
        when(messages.mergeTMStarted()).thenReturn("merge started");

        // When:
        MergeOptions opts = MergeOptions.allFuzzy();
        opts.setDifferentProject(MergeRule.REJECT);
        presenter.proceedToMergeTM(100, opts);

        assertThat(mergeResource.callback).isNotNull();

        // REST call success
        mergeResource.callback.onSuccess(null, null);

        // Then:
        verify(display).showProcessing("merge started");
        assertThat(presenter.isMergeStarted()).isTrue();
    }

    @Test
    public void onTMMergeProgressEventWillShowProgress() {
        TMMergeProgressEvent event =
                new TMMergeProgressEvent(1, 10, editorClientId, documentId);
        when(messages.mergeProgressPercentage(event.getPercentDisplay())).thenReturn("10% processed");
        presenter.onTMMergeProgress(
                event);

        verify(display).showProcessing("10% processed");
    }

    @Test
    public void onTMMergeStartedFromSelfNothingHappen() {
        TMMergeStartOrEndEvent event =
                new TMMergeStartOrEndEvent("self", new Date(), editorClientId,
                        documentId, null, 10);
        presenter.onTMMergeStartOrEnd(event);
        verifyZeroInteractions(messages, display);
    }

    @Test
    public void onTMMergeEndedFromSelfAndSomeTextFlowsAreMerged() {
        when(messages.mergeTMSuccess(10)).thenReturn("10 merged");
        TMMergeStartOrEndEvent event =
                new TMMergeStartOrEndEvent("self", new Date(), editorClientId,
                        documentId, new Date(), 10);
        presenter.onTMMergeStartOrEnd(event);

        verify(eventBus).fireEvent(notificationEventCaptor.capture());
        verify(display).hide();
        NotificationEvent notificationEvent = notificationEventCaptor.getValue();
        assertThat(notificationEvent.getSeverity()).isEqualTo(NotificationEvent.Severity.Info);
        assertThat(notificationEvent.getMessage()).isEqualTo("10 merged");
        assertThat(presenter.isMergeStarted()).isFalse();
    }

    @Test
    public void onTMMergeEndedFromSelfAndNoTextFlowIsMerged() {
        when(messages.noTranslationToMerge()).thenReturn("nothing to be merged");
        TMMergeStartOrEndEvent event =
                new TMMergeStartOrEndEvent("self", new Date(), editorClientId,
                        documentId, new Date(), 0);
        presenter.onTMMergeStartOrEnd(event);

        verify(eventBus).fireEvent(notificationEventCaptor.capture());
        verify(display).hide();
        NotificationEvent notificationEvent = notificationEventCaptor.getValue();
        assertThat(notificationEvent.getSeverity()).isEqualTo(NotificationEvent.Severity.Info);
        assertThat(notificationEvent.getMessage()).isEqualTo("nothing to be merged");
        assertThat(presenter.isMergeStarted()).isFalse();
    }

    @Test
    public void onTMMergeStartedFromOthersOnSameDoc() {
        TMMergeStartOrEndEvent event =
                new TMMergeStartOrEndEvent("someone", new Date(), new EditorClientId("other session", 2L),
                        documentId, null, 10);
        when(messages.mergeTMStartedBySomeone(documentId.getDocId())).thenReturn("someone else started TM merge");
        presenter.onTMMergeStartOrEnd(event);

        verify(eventBus).fireEvent(notificationEventCaptor.capture());
        NotificationEvent notificationEvent = notificationEventCaptor.getValue();
        assertThat(notificationEvent.getSeverity()).isEqualTo(NotificationEvent.Severity.Warning);
        assertThat(notificationEvent.getMessage()).isEqualTo("someone else started TM merge");
    }

    @Test
    public void onTMMergeStartedFromOthersOnDifferentDoc() {
        TMMergeStartOrEndEvent event =
                new TMMergeStartOrEndEvent("someone", new Date(), new EditorClientId("other session", 2L),
                        new DocumentId(2L, "otherDoc"), null, 10);
        when(messages.mergeTMStartedBySomeoneForDoc("otherDoc")).thenReturn("someone else started TM merge");
        presenter.onTMMergeStartOrEnd(event);

        verify(eventBus).fireEvent(notificationEventCaptor.capture());
        NotificationEvent notificationEvent = notificationEventCaptor.getValue();
        assertThat(notificationEvent.getSeverity()).isEqualTo(NotificationEvent.Severity.Info);
        assertThat(notificationEvent.getMessage()).isEqualTo("someone else started TM merge");
    }

    @Test
    public void onTMMergeEndedFromOthersOnSameDoc() {
        Date time = new Date();
        TMMergeStartOrEndEvent event =
                new TMMergeStartOrEndEvent("someone",
                        time, new EditorClientId("other session", 2L),
                        documentId, time, 10);
        when(messages.mergeTMFinished(Mockito.eq(documentId.getDocId()), Mockito.eq("someone"), Mockito.anyString(), Mockito.anyString())).thenReturn("someone else finished TM merge");
        presenter.onTMMergeStartOrEnd(event);

        verify(eventBus).fireEvent(notificationEventCaptor.capture());
        NotificationEvent notificationEvent = notificationEventCaptor.getValue();
        assertThat(notificationEvent.getSeverity()).isEqualTo(NotificationEvent.Severity.Warning);
        assertThat(notificationEvent.getMessage()).isEqualTo("someone else finished TM merge");
    }

    @Test
    public void onTMMergeEndedFromOthersOnDifferentDoc() {
        Date time = new Date();
        DocumentId otherDoc = new DocumentId(2L, "otherDoc");
        TMMergeStartOrEndEvent event =
                new TMMergeStartOrEndEvent("someone",
                        time, new EditorClientId("other session", 2L),
                        otherDoc, time, 10);
        when(messages.mergeTMFinished(Mockito.eq(otherDoc.getDocId()), Mockito.eq("someone"), Mockito.anyString(), Mockito.anyString())).thenReturn("someone else finished TM merge");
        presenter.onTMMergeStartOrEnd(event);

        verify(eventBus).fireEvent(notificationEventCaptor.capture());
        NotificationEvent notificationEvent = notificationEventCaptor.getValue();
        assertThat(notificationEvent.getSeverity()).isEqualTo(NotificationEvent.Severity.Info);
        assertThat(notificationEvent.getMessage()).isEqualTo("someone else finished TM merge");
    }

    private static class FakeTransMemoryMergeResource implements TransMemoryMergeResource,
            CallbackAware {

        private TransMemoryMergeRequest request;
        @SuppressWarnings("unused")
        private TransMemoryMergeCancelRequest cancelRequest;
        private MethodCallback<?> callback;

        @Override
        @SuppressWarnings("rawtypes")
        public void setCallback(MethodCallback callback) {
            this.callback = callback;
        }

        @Override
        public void merge(TransMemoryMergeRequest request) {
            this.request = request;
        }

        @Override
        public void cancelMerge(TransMemoryMergeCancelRequest request) {
            cancelRequest = request;
        }
    }
}
