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

import static org.zanata.webtrans.client.events.NotificationEvent.Severity.Error;
import static org.zanata.webtrans.client.events.NotificationEvent.Severity.Info;
import static org.zanata.webtrans.client.events.NotificationEvent.Severity.Warning;

import org.fusesource.restygwt.client.Method;
import org.fusesource.restygwt.client.MethodCallback;
import org.fusesource.restygwt.client.REST;
import org.zanata.common.LocaleId;
import org.zanata.webtrans.client.events.NotificationEvent;
import org.zanata.webtrans.client.events.TMMergeProgressEvent;
import org.zanata.webtrans.client.events.TMMergeProgressHandler;
import org.zanata.webtrans.client.events.TMMergeStartOrEndEvent;
import org.zanata.webtrans.client.events.TMMergeStartOrEndHandler;
import org.zanata.webtrans.client.resources.UiMessages;
import org.zanata.webtrans.client.ui.TransMemoryMergePopupPanelDisplay;
import org.zanata.webtrans.client.util.DateUtil;
import org.zanata.webtrans.shared.auth.EditorClientId;
import org.zanata.webtrans.shared.auth.Identity;
import org.zanata.webtrans.shared.model.DocumentId;
import org.zanata.webtrans.shared.model.ProjectIterationId;
import org.zanata.webtrans.shared.model.UserWorkspaceContext;
import org.zanata.webtrans.shared.model.WorkspaceId;
import org.zanata.webtrans.shared.rest.TransMemoryMergeResource;
import org.zanata.webtrans.shared.rest.dto.TransMemoryMergeCancelRequest;
import org.zanata.webtrans.shared.rest.dto.TransMemoryMergeRequest;
import org.zanata.webtrans.shared.rpc.MergeOptions;

import com.allen_sauer.gwt.log.client.Log;
import com.google.common.annotations.VisibleForTesting;
import com.google.inject.Inject;

import net.customware.gwt.presenter.client.EventBus;
import net.customware.gwt.presenter.client.widget.WidgetPresenter;

/**
 * @author Patrick Huang <a
 *         href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
public class TransMemoryMergePresenter extends
        WidgetPresenter<TransMemoryMergePopupPanelDisplay> implements
        TransMemoryMergePopupPanelDisplay.Listener, TMMergeProgressHandler,
        TMMergeStartOrEndHandler {

    private TransMemoryMergePopupPanelDisplay display;
    private final EventBus eventBus;
    private final TransMemoryMergeResource transMemoryMergeClient;
    private final Identity identity;
    private final UserWorkspaceContext userWorkspaceContext;
    private final UiMessages messages;
    private boolean mergeStarted;

    @Inject
    public TransMemoryMergePresenter(TransMemoryMergePopupPanelDisplay display,
            EventBus eventBus,
            TransMemoryMergeResource transMemoryMergeClient,
            Identity identity,
            UserWorkspaceContext userWorkspaceContext,
            UiMessages messages) {
        super(display, eventBus);
        this.display = display;
        this.eventBus = eventBus;
        this.transMemoryMergeClient = transMemoryMergeClient;
        this.identity = identity;
        this.userWorkspaceContext = userWorkspaceContext;
        this.messages = messages;
        display.setListener(this);
        registerHandler(eventBus.addHandler(TMMergeProgressEvent.TYPE, this));
        registerHandler(eventBus.addHandler(TMMergeStartOrEndEvent.TYPE, this));
    }

    @Override
    public void proceedToMergeTM(int percentage, MergeOptions mergeOptions) {

        DocumentId currentDoc = userWorkspaceContext.getSelectedDoc().getId();
        WorkspaceId workspaceId =
                userWorkspaceContext.getWorkspaceContext().getWorkspaceId();
        LocaleId localeId = workspaceId.getLocaleId();
        ProjectIterationId projectIterationId = workspaceId
                .getProjectIterationId();


        TransMemoryMergeRequest request =
                new TransMemoryMergeRequest(
                        identity.getEditorClientId(),
                        projectIterationId, currentDoc, localeId, percentage,
                        mergeOptions.getDifferentProject(),
                        mergeOptions.getDifferentDocument(),
                        mergeOptions.getDifferentResId(),
                        mergeOptions.getImportedMatch());
        // the result is always null as the call is async
        // this is just so compiler will check the return type for us
        REST.withCallback(
                new MethodCallback<Void>() {
                    @Override
                    public void onFailure(Method method, Throwable exception) {
                        Log.warn("TM merge failed", exception);
                        if (method.getResponse().getStatusCode() == 400) {
                            eventBus.fireEvent(new NotificationEvent(Warning, messages.mergeTMStartedBySomeone(currentDoc.getDocId())));
                        } else {
                            eventBus.fireEvent(new NotificationEvent(Error, messages
                                    .mergeTMFailed()));
                        }
                        display.hide();
                        mergeStarted = false;
                    }

                    @Override
                    public void onSuccess(Method method,
                            Void response) {
                        display.showProcessing(messages.mergeTMStarted());
                        mergeStarted = true;
                    }
                }).call(transMemoryMergeClient).merge(request);

    }

    @Override
    public void cancelMergeTM() {
        if (mergeStarted) {
            DocumentId currentDoc = userWorkspaceContext.getSelectedDoc().getId();
            WorkspaceId workspaceId =
                    userWorkspaceContext.getWorkspaceContext().getWorkspaceId();
            LocaleId localeId = workspaceId.getLocaleId();
            ProjectIterationId projectIterationId = workspaceId
                    .getProjectIterationId();
            TransMemoryMergeCancelRequest request =
                    new TransMemoryMergeCancelRequest();
            request.documentId = currentDoc;
            request.localeId = localeId;
            request.projectIterationId = projectIterationId;
            REST.withCallback(
            new MethodCallback<Void>() {
                @Override
                public void onFailure(Method method, Throwable exception) {
                    Log.warn("TM merge cancel failed", exception);
                    eventBus.fireEvent(new NotificationEvent(Error, messages
                            .mergeTMCancelFailed()));
                    display.hide();
                    mergeStarted = false;
                }

                @Override
                public void onSuccess(Method method,
                        Void response) {
                    display.hide();
                    mergeStarted = false;
                }
            }).call(transMemoryMergeClient).cancelMerge(request);

        } else {
            display.hide();
        }
    }

    public void prepareTMMerge() {
        display.showForm();
    }

    @Override
    protected void onBind() {
    }

    @Override
    protected void onUnbind() {
    }

    @Override
    protected void onRevealDisplay() {
    }

    @Override
    public void onTMMergeProgress(TMMergeProgressEvent event) {
        if (!isEventFromSelf(event.getEditorClientId(),
                identity.getEditorClientId())) {
            // if TM merge is initiated from another user, we don't care the progress
            return;
        }
        // TODO pahuang if we want to do undo, the undo will need to be performed asynchronously too
        /*final UndoLink undoLink = undoLinkProvider.get();
        undoLink.prepareUndoFor(result);

        List<String> rowIndicesOrNull =
                Lists.transform(result.getUpdateInfoList(),
                        SuccessRowIndexOrNullFunction.FUNCTION);
        Iterable<String> successRowIndices =
                Iterables.filter(rowIndicesOrNull,
                        StringNotEmptyPredicate.INSTANCE);

        Log.info("number of rows auto filled by TM merge: "
                + Iterables.size(successRowIndices));
        NotificationEvent notificationEvent =
                new NotificationEvent(Info, messages
                        .mergeTMSuccess(Lists
                                .newArrayList(successRowIndices)),
                        undoLink);*/

        display.showProcessing(messages.mergeProgressPercentage(event.getPercentDisplay()));
    }

    private static boolean isEventFromSelf(EditorClientId editorClientIdInEvent,
            EditorClientId editorClientIdInIdentity) {
        return editorClientIdInEvent
                .equals(editorClientIdInIdentity);
    }

    @Override
    public void onTMMergeStartOrEnd(TMMergeStartOrEndEvent event) {
        boolean eventFromOtherUser = !isEventFromSelf(event.getEditorClientId(),
                identity.getEditorClientId());
        if (!eventFromOtherUser) {
            handleEventFromCurrentUser(event);
        } else {
            handleEventFromOtherUser(event);
        }
    }

    private void handleEventFromOtherUser(TMMergeStartOrEndEvent event) {
        if (!event.isEnded()) {
            if (userWorkspaceContext.getSelectedDoc().getId()
                    .equals(event.getDocumentId())) {
                // another user has stared TM merge for this document
                eventBus.fireEvent(new NotificationEvent(Warning,
                        messages.mergeTMStartedBySomeone(event.getDocumentId().getDocId())));
            } else {
                // another user has started TM merge for other document
                eventBus.fireEvent(new NotificationEvent(Info,
                        messages.mergeTMStartedBySomeoneForDoc(event.getDocumentId().getDocId())));
            }
        } else {

            // TM merge triggered by another user has ended
            NotificationEvent.Severity level;
            if (userWorkspaceContext.getSelectedDoc().getId()
                    .equals(event.getDocumentId())) {
                level = Warning;
            } else {
                level = Info;
            }
            eventBus.fireEvent(new NotificationEvent(level,
                    messages.mergeTMFinished(event.getDocumentId().getDocId(),
                            event.getStartedBy(),
                            DateUtil.formatTimeOnly(event.getStartedTime()),
                            DateUtil.formatTimeOnly(event.getEndTime()))));
        }
    }

    private void handleEventFromCurrentUser(TMMergeStartOrEndEvent event) {
        // event are from current user, we only care about the ending event
        if (event.isEnded()) {
            if (event.getTextFlowCount() == 0) {
                eventBus.fireEvent(
                        new NotificationEvent(Info, messages
                                .noTranslationToMerge()));
            } else {
                eventBus.fireEvent(new NotificationEvent(Info,
                        messages.mergeTMSuccess(event.getTextFlowCount())));
            }
            display.hide();
            mergeStarted = false;
        }
    }

    @VisibleForTesting
    protected boolean isMergeStarted() {
        return mergeStarted;
    }
}
