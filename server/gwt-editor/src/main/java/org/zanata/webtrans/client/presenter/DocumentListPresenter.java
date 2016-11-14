/*
 * Copyright 2010, Red Hat, Inc. and individual contributors as indicated by the
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import net.customware.gwt.presenter.client.EventBus;
import net.customware.gwt.presenter.client.widget.WidgetPresenter;

import org.zanata.common.LocaleId;
import org.zanata.common.ProjectType;
import org.zanata.rest.dto.stats.ContainerTranslationStatistics;
import org.zanata.rest.dto.stats.TranslationStatistics;
import org.zanata.rest.dto.stats.TranslationStatistics.StatUnit;
import org.zanata.webtrans.client.Application;
import org.zanata.webtrans.client.events.DocValidationResultEvent;
import org.zanata.webtrans.client.events.DocumentSelectionEvent;
import org.zanata.webtrans.client.events.DocumentSelectionHandler;
import org.zanata.webtrans.client.events.DocumentStatsUpdatedEvent;
import org.zanata.webtrans.client.events.NotificationEvent;
import org.zanata.webtrans.client.events.RefreshProjectStatsEvent;
import org.zanata.webtrans.client.events.RunDocValidationEvent;
import org.zanata.webtrans.client.events.RunDocValidationEventHandler;
import org.zanata.webtrans.client.events.TransUnitUpdatedEvent;
import org.zanata.webtrans.client.events.TransUnitUpdatedEventHandler;
import org.zanata.webtrans.client.events.UserConfigChangeEvent;
import org.zanata.webtrans.client.events.UserConfigChangeHandler;
import org.zanata.webtrans.client.events.WorkspaceContextUpdateEvent;
import org.zanata.webtrans.client.events.WorkspaceContextUpdateEventHandler;
import org.zanata.webtrans.client.history.History;
import org.zanata.webtrans.client.history.HistoryToken;
import org.zanata.webtrans.client.resources.WebTransMessages;
import org.zanata.webtrans.client.rpc.CachingDispatchAsync;
import org.zanata.webtrans.client.rpc.HasQueueDispatch;
import org.zanata.webtrans.client.rpc.QueueDispatcher;
import org.zanata.webtrans.client.service.UserOptionsService;
import org.zanata.webtrans.client.ui.DocumentListTable.DocValidationStatus;
import org.zanata.webtrans.client.ui.DocumentNode;
import org.zanata.webtrans.client.view.DocumentListDisplay;
import org.zanata.webtrans.shared.model.AuditInfo;
import org.zanata.webtrans.shared.model.DocumentId;
import org.zanata.webtrans.shared.model.DocumentInfo;
import org.zanata.webtrans.shared.model.TransUnit;
import org.zanata.webtrans.shared.model.TransUnitUpdateInfo;
import org.zanata.webtrans.shared.model.UserWorkspaceContext;
import org.zanata.webtrans.shared.model.ValidationId;
import org.zanata.webtrans.shared.model.WorkspaceId;
import org.zanata.webtrans.shared.rpc.DownloadAllFilesAction;
import org.zanata.webtrans.shared.rpc.DownloadAllFilesResult;
import org.zanata.webtrans.shared.rpc.GetDocumentStats;
import org.zanata.webtrans.shared.rpc.GetDocumentStatsResult;
import org.zanata.webtrans.shared.rpc.GetDownloadAllFilesProgress;
import org.zanata.webtrans.shared.rpc.GetDownloadAllFilesProgressResult;
import org.zanata.webtrans.shared.rpc.RunDocValidationAction;
import org.zanata.webtrans.shared.rpc.RunDocValidationResult;

import com.allen_sauer.gwt.log.client.Log;
import com.google.common.base.Objects;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gwt.core.shared.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;

public class DocumentListPresenter extends WidgetPresenter<DocumentListDisplay>
        implements DocumentListDisplay.Listener, DocumentSelectionHandler,
        UserConfigChangeHandler, TransUnitUpdatedEventHandler,
        WorkspaceContextUpdateEventHandler, RunDocValidationEventHandler {
    private final UserWorkspaceContext userWorkspaceContext;
    private DocumentInfo currentDocument;
    private final WebTransMessages messages;
    private final History history;
    private final UserOptionsService userOptionsService;
    private final LocaleId localeId;

    private Map<DocumentId, DocumentNode> nodes = Maps.newTreeMap();
    private Map<DocumentId, Integer> pageRows = Maps.newHashMap();
    private List<DocumentNode> filteredNodes = Lists.newArrayList();

    private final CachingDispatchAsync dispatcher;

    private final HasQueueDispatch<GetDocumentStats, GetDocumentStatsResult> docStatQueueDispatcher;

    /**
     * For quick lookup of document id by full path (including document name).
     * Primarily for use with history token.
     */
    private HashMap<String, DocumentId> idsByPath = Maps.newHashMap();

    private final PathDocumentFilter filter = new PathDocumentFilter();

    @Inject
    public DocumentListPresenter(DocumentListDisplay display,
            EventBus eventBus, CachingDispatchAsync dispatcher,
            UserWorkspaceContext userworkspaceContext,
            final WebTransMessages messages, History history,
            UserOptionsService userOptionsService) {
        super(display, eventBus);
        this.dispatcher = dispatcher;
        this.userWorkspaceContext = userworkspaceContext;
        this.messages = messages;
        this.history = history;
        this.userOptionsService = userOptionsService;
        docStatQueueDispatcher = new QueueDispatcher<>(dispatcher);

        localeId =
                userWorkspaceContext.getWorkspaceContext().getWorkspaceId()
                        .getLocaleId();
    }

    @Override
    protected void onBind() {
        display.setListener(this);

        display.setStatsFilters(DocumentListDisplay.STATS_OPTION_WORDS);

        registerHandler(eventBus.addHandler(DocumentSelectionEvent.getType(),
                this));
        registerHandler(eventBus.addHandler(TransUnitUpdatedEvent.getType(),
                this));
        registerHandler(eventBus.addHandler(UserConfigChangeEvent.TYPE, this));
        registerHandler(eventBus.addHandler(
                WorkspaceContextUpdateEvent.getType(), this));
        registerHandler(eventBus.addHandler(RunDocValidationEvent.getType(),
                this));

        display.setLayout(userOptionsService.getConfigHolder().getState()
                .getDisplayTheme().name());

        setupDownloadZipButton(getProjectType());
    }

    private ProjectType getProjectType() {
        return userWorkspaceContext.getWorkspaceContext().getWorkspaceId()
                .getProjectIterationId().getProjectType();
    }

    public void setupDownloadZipButton(ProjectType projectType) {
        if (isZipFileDownloadAllowed(projectType)) {
            display.setEnableDownloadZip(true);
            if (isPoProject(projectType)) {
                display.setDownloadZipButtonText(messages.downloadAllAsZip());
                display.setDownloadZipButtonTitle(messages
                        .downloadAllAsZipDescription());
            } else {
                display.setDownloadZipButtonText(messages
                        .downloadAllAsOfflinePoZip());
                display.setDownloadZipButtonTitle(messages
                        .downloadAllAsOfflinePoZipDescription());
            }
        } else {
            display.setEnableDownloadZip(false);
            display.setDownloadZipButtonText(messages.downloadAllAsZip());
            display.setDownloadZipButtonTitle(messages.projectTypeNotSet());
        }
    }

    private boolean isPoProject(ProjectType projectType) {
        return projectType == ProjectType.Gettext
                || projectType == ProjectType.Podir;
    }

    protected boolean isZipFileDownloadAllowed(ProjectType projectType) {
        return projectType != null;
    }

    @Override
    public void fireDocumentSelection(DocumentInfo doc) {
        // generate history token
        HistoryToken token = history.getHistoryToken();

        currentDocument = doc;
        token.setDocumentPath(doc.getPath() + doc.getName());
        token.setView(MainView.Editor);
        if (doc.hasError() != null) {
            if (doc.hasError()) {
                token.setFilterHasError(true);
            } else {
                token.setFilterHasError(false);
            }
        }

        // don't carry searches over to the next document
        token.setEditorTextSearch("");
        history.newItem(token);

        userWorkspaceContext.setSelectedDoc(doc);
    }

    @Override
    public void fireFilterToken(String value) {
        HistoryToken token = HistoryToken.fromTokenString(history.getToken());
        if (!value.equals(token.getDocFilterText())) {
            token.setDocFilterText(value);
            history.newItem(token.toTokenString());
        }
    }

    @Override
    public void fireExactSearchToken(boolean value) {
        HistoryToken token = HistoryToken.fromTokenString(history.getToken());
        if (value != token.getDocFilterExact()) {
            token.setDocFilterExact(value);
            history.newItem(token.toTokenString());
        }
    }

    @Override
    public void fireCaseSensitiveToken(boolean value) {
        HistoryToken token = HistoryToken.fromTokenString(history.getToken());
        if (value != token.isDocFilterCaseSensitive()) {
            token.setDocFilterCaseSensitive(value);
            history.newItem(token.toTokenString());
        }
    }

    @Override
    public void statsOptionChange() {
        for (Integer row : pageRows.values()) {
            display.setStatsFilters(row);
        }
    }

    public void updateFilterAndRun(String docFilterText,
            boolean docFilterExact, boolean docFilterCaseSensitive) {
        display.updateFilter(docFilterCaseSensitive, docFilterExact,
                docFilterText);

        filter.setCaseSensitive(docFilterCaseSensitive);
        filter.setFullText(docFilterExact);
        filter.setPattern(docFilterText);

        runFilter();
    }

    @Override
    protected void onUnbind() {
    }

    @Override
    public void onRevealDisplay() {
        // Auto-generated method stub
    }

    public void setDocuments(List<DocumentInfo> sortedDocumentList) {
        nodes.clear();
        idsByPath.clear();

        for (DocumentInfo doc : sortedDocumentList) {
            idsByPath.put(doc.getPath() + doc.getName(), doc.getId());
            nodes.put(doc.getId(), new DocumentNode(doc));
        }
        runFilter();
    }

    public void queryStats() {
        int BATCH_SIZE = 1000;

        ArrayList<GetDocumentStats> queueList =
                new ArrayList<GetDocumentStats>();
        for (int i = 0; i < filteredNodes.size();) {
            int fromIndex = i;
            int toIndex =
                    i + BATCH_SIZE > filteredNodes.size() ? filteredNodes
                            .size() : i + BATCH_SIZE;
            List<DocumentNode> subList =
                    filteredNodes.subList(fromIndex, toIndex);
            queueList.add(new GetDocumentStats(convertFromNodetoId(subList)));
            i = toIndex;
        }
        docStatQueueDispatcher.setQueueAndExecute(queueList,
                getDocumentStatCallBack);
    }

    /**
     * Facilitate unit testing. Will be no-op if in client(GWT compiled) mode.
     */
    protected void setStatesForTest(List<DocumentNode> sortedNodes,
            Map<DocumentId, DocumentNode> nodes) {
        if (!GWT.isClient()) {
            this.filteredNodes = sortedNodes;
            this.nodes = nodes;
        }
    }

    private final AsyncCallback<GetDocumentStatsResult> getDocumentStatCallBack =
            new AsyncCallback<GetDocumentStatsResult>() {
                @Override
                public void onFailure(Throwable caught) {
                    eventBus.fireEvent(new NotificationEvent(
                            NotificationEvent.Severity.Error,
                            "Unable get stats for documents"));
                }

                @Override
                public void onSuccess(GetDocumentStatsResult result) {
                    for (Entry<DocumentId, ContainerTranslationStatistics> entry : result
                            .getStatsMap().entrySet()) {
                        DocumentInfo docInfo = getDocumentInfo(entry.getKey());
                        docInfo.setStats(entry.getValue());

                        docInfo.setLastTranslated(result.getLastTranslatedMap()
                                .get(entry.getKey()));

                        Integer row = pageRows.get(entry.getKey());
                        if (row != null) {
                            display.updateStats(row, docInfo.getStats());
                            display.updateLastTranslated(row,
                                    docInfo.getLastTranslated());
                        }

                        eventBus.fireEvent(new DocumentStatsUpdatedEvent(entry
                                .getKey(), docInfo.getStats()));
                    }
                    eventBus.fireEvent(new RefreshProjectStatsEvent(Lists
                            .newArrayList(nodes.values())));
                    docStatQueueDispatcher.executeQueue();

                }
            };

    private List<DocumentId> convertFromNodetoId(List<DocumentNode> nodes) {
        ArrayList<DocumentId> documentIds = Lists.newArrayList();

        for (DocumentNode node : nodes) {
            documentIds.add(node.getDocInfo().getId());
        }
        return documentIds;
    }

    private void updatePageCountAndGotoFirstPage() {
        int pageCount =
                (int) Math.ceil(filteredNodes.size()
                        * 1.0
                        / userOptionsService.getConfigHolder().getState()
                                .getDocumentListPageSize());
        display.getPageNavigation().setPageCount(pageCount);
        gotoPage(1);
    }

    private void gotoPage(int page) {
        int pageSize =
                userOptionsService.getConfigHolder().getState()
                        .getDocumentListPageSize();
        int fromIndex = (page - 1) * pageSize;
        int toIndex =
                (fromIndex + pageSize) > filteredNodes.size() ? filteredNodes
                        .size() : fromIndex + pageSize;
        pageRows =
                display.buildContent(filteredNodes.subList(fromIndex, toIndex));
        display.getPageNavigation().setValue(page, false);
    }

    /**
     * Filter the document list based on the current filter patterns. Empty
     * filter patterns will show all documents.
     */
    private void runFilter() {
        filteredNodes.clear();
        for (DocumentNode docNode : nodes.values()) {
            if (filter.accept(docNode.getDocInfo())) {
                filteredNodes.add(docNode);
            }
        }
        updatePageCountAndGotoFirstPage();
    }

    /**
     *
     * @param docId
     *            the id of the document
     * @return document info corresponding to the id, or null if the document is
     *         not in the document list
     */
    public DocumentInfo getDocumentInfo(DocumentId docId) {
        DocumentNode node = nodes.get(docId);
        return (node == null ? null : node.getDocInfo());
    }

    /**
     *
     * @param fullPathAndName
     *            document path + document name
     * @return the id for the document, or null if the document is not in the
     *         document list or there is no document list
     */
    public DocumentId getDocumentId(String fullPathAndName) {
        if (idsByPath != null) {
            return idsByPath.get(fullPathAndName);
        }
        return null;
    }

    private void setSelection(final DocumentId documentId) {
        if (currentDocument != null && currentDocument.getId() == documentId) {
            Log.info("same selection doc id:" + documentId);
            return;
        }
        currentDocument = null;
        DocumentNode node = nodes.get(documentId);
        if (node != null) {
            userWorkspaceContext.setSelectedDoc(node.getDocInfo());
            // required in order to show the document selected in doclist when
            // loading from bookmarked history token
            userWorkspaceContext.setSelectedDoc(node.getDocInfo());


        }
    }

    @Override
    public void onDocumentSelected(DocumentSelectionEvent event) {
        // match bookmarked selection, but prevent selection feedback loop
        // from history
        DocumentId current =
                currentDocument == null ? null : currentDocument.getId();
        if (!Objects.equal(event.getDocumentId(), current)) {
            setSelection(event.getDocumentId());
        }

    }

    @Override
    public void onTransUnitUpdated(TransUnitUpdatedEvent event) {
        TransUnitUpdateInfo updateInfo = event.getUpdateInfo();
        // update stats for containing document
        DocumentInfo updatedDoc = getDocumentInfo(updateInfo.getDocumentId());
        ContainerTranslationStatistics currentStats = updatedDoc.getStats();
        if (currentStats != null) {
            adjustStats(currentStats, updateInfo);
            updatedDoc.setStats(currentStats);

            updateLastTranslatedInfo(updatedDoc, event.getUpdateInfo()
                    .getTransUnit());

            Integer row = pageRows.get(updatedDoc.getId());
            if (row != null) {
                display.updateStats(row, updatedDoc.getStats());
                AuditInfo lastTranslated =
                        new AuditInfo(event.getUpdateInfo().getTransUnit()
                                .getLastModifiedTime(), event.getUpdateInfo()
                                .getTransUnit().getLastModifiedBy());
                display.updateLastTranslated(row, lastTranslated);
            }
            eventBus.fireEvent(new DocumentStatsUpdatedEvent(
                    updatedDoc.getId(), currentStats));
        }

        eventBus.fireEvent(new RefreshProjectStatsEvent(Lists
                .newArrayList(nodes.values())));
    }

    private void updateLastTranslatedInfo(DocumentInfo doc,
            TransUnit updatedTransUnit) {
        doc.setLastTranslated(new AuditInfo(updatedTransUnit
                .getLastModifiedTime(), updatedTransUnit.getLastModifiedBy()));
    }

    /**
     * @param stats
     *            the stats object to update
     * @param updateInfo
     *            info describing the change in translations
     */
    private void adjustStats(ContainerTranslationStatistics stats,
            TransUnitUpdateInfo updateInfo) {
        TranslationStatistics msgStatistic =
                stats.getStats(localeId.getId(), StatUnit.MESSAGE);
        TranslationStatistics wordStatistic =
                stats.getStats(localeId.getId(), StatUnit.WORD);

        msgStatistic.decrement(updateInfo.getPreviousState(), 1);
        msgStatistic.increment(updateInfo.getTransUnit().getStatus(), 1);

        wordStatistic.decrement(updateInfo.getPreviousState(),
                updateInfo.getSourceWordCount());
        wordStatistic.increment(updateInfo.getTransUnit().getStatus(),
                updateInfo.getSourceWordCount());
    }

    @Override
    public void onUserConfigChanged(UserConfigChangeEvent event) {
        display.setLayout(userOptionsService.getConfigHolder().getState()
                .getDisplayTheme().name());
        if (event.getView() == MainView.Documents) {
            updatePageCountAndGotoFirstPage();
        }
    }

    @Override
    public void downloadAllFiles() {
        WorkspaceId workspaceId =
                userWorkspaceContext.getWorkspaceContext().getWorkspaceId();
        dispatcher.execute(new DownloadAllFilesAction(workspaceId
                .getProjectIterationId().getProjectSlug(), workspaceId
                .getProjectIterationId().getIterationSlug(), workspaceId
                .getLocaleId().getId(), isPoProject(getProjectType())),
                new AsyncCallback<DownloadAllFilesResult>() {
                    @Override
                    public void onFailure(Throwable caught) {
                        eventBus.fireEvent(new NotificationEvent(
                                NotificationEvent.Severity.Warning,
                                "Unable generate all files to download"));
                        display.hideConfirmation();
                    }

                    @Override
                    public void onSuccess(DownloadAllFilesResult result) {
                        if (result.isPrepared()) {
                            processId = result.getProcessId();
                            display.updateFileDownloadProgress(0, 0);
                            display.setDownloadInProgress(true);
                            display.startGetDownloadStatus(1000);
                        } else {
                            eventBus.fireEvent(new NotificationEvent(
                                    NotificationEvent.Severity.Warning,
                                    "Permission denied for this action"));
                            display.hideConfirmation();
                        }
                    }
                });
    }

    @Override
    public void cancelDownloadAllFiles() {
        display.hideConfirmation();
    }

    private String processId;

    @Override
    public void updateDownloadFileProgress() {
        dispatcher.execute(new GetDownloadAllFilesProgress(processId),
                new AsyncCallback<GetDownloadAllFilesProgressResult>() {
                    @Override
                    public void onFailure(Throwable caught) {
                        eventBus.fireEvent(new NotificationEvent(
                                NotificationEvent.Severity.Warning,
                                "Unable get progress of file preparation"));
                        display.hideConfirmation();
                    }

                    @Override
                    public void onSuccess(
                            GetDownloadAllFilesProgressResult result) {
                        display.updateFileDownloadProgress(
                                result.getCurrentProgress(),
                                result.getMaxProgress());

                        if (result.isDone()) {
                            display.stopGetDownloadStatus();
                            final String url =
                                    Application.getAllFilesDownloadURL(result
                                            .getDownloadId());
                            display.setAndShowFilesDownloadLink(url);
                            eventBus.fireEvent(new NotificationEvent(
                                    NotificationEvent.Severity.Info,
                                    "File ready to download", display
                                            .getDownloadAllFilesInlineLink(url)));
                        }
                    }
                });
    }

    @Override
    public void onWorkspaceContextUpdated(WorkspaceContextUpdateEvent event) {
        userWorkspaceContext.setProjectActive(event.isProjectActive());
        userWorkspaceContext.getWorkspaceContext().getWorkspaceId()
                .getProjectIterationId().setProjectType(event.getProjectType());
        setupDownloadZipButton(event.getProjectType());
    }

    @Override
    public void onRunDocValidation(RunDocValidationEvent event) {
        if (event.getView() != MainView.Documents) {
            return;
        }
        List<ValidationId> valIds =
                userOptionsService.getConfigHolder().getState()
                        .getEnabledValidationIds();
        if (valIds.isEmpty() || pageRows.keySet().isEmpty()) {
            // no validation to run. we just need to re-enable the button.
            // see org.zanata.webtrans.client.presenter.ValidationOptionsPresenter.onCompleteRunDocValidation()
            eventBus.fireEvent(new DocValidationResultEvent(null));
            return;
        }
        ArrayList<DocumentId> docList = Lists.newArrayList();
        for (DocumentId documentId : pageRows.keySet()) {
            display.showRowLoading(pageRows.get(documentId));
            docList.add(documentId);
        }

        dispatcher.execute(new RunDocValidationAction(valIds, docList),
                new AsyncCallback<RunDocValidationResult>() {
                    @Override
                    public void
                            onSuccess(RunDocValidationResult result) {
                        Log.debug("Success doc validation - "
                                + result.getResultMap().size());

                        for (Entry<DocumentId, Boolean> entry : result
                                .getResultMap().entrySet()) {
                            Integer row = pageRows.get(entry.getKey());
                            DocumentNode node =
                                    nodes.get(entry.getKey());

                            DocValidationStatus status =
                                    entry.getValue() ? DocValidationStatus.HasError
                                            : DocValidationStatus.NoError;

                            if (row != null) {
                                display.updateRowHasError(row, status);

                                if (node != null) {
                                    node.getDocInfo().setHasError(
                                            entry.getValue());
                                }
                            }
                        }
                        eventBus.fireEvent(new DocValidationResultEvent(
                                new Date()));
                    }

                    @Override
                    public void onFailure(Throwable caught) {
                        eventBus.fireEvent(new NotificationEvent(
                                NotificationEvent.Severity.Error,
                                "Unable to run validation"));
                        eventBus.fireEvent(new DocValidationResultEvent(
                                new Date()));
                    }
                });
    }

    @Override
    public void sortList(String header, boolean asc) {
        HeaderComparator comparator = new HeaderComparator(header);
        Collections.sort(filteredNodes, comparator);
        if (!asc) {
            Collections.reverse(filteredNodes);
        }
        gotoPage(1);
    }

    private class HeaderComparator implements Comparator<DocumentNode> {
        private String header;

        public HeaderComparator(String header) {
            this.header = header;
        }

        @Override
        public int compare(DocumentNode o1, DocumentNode o2) {
            if (header.equals(DocumentListDisplay.PATH_HEADER)) {
                return comparePath(o1, o2);
            } else if (header.equals(DocumentListDisplay.DOC_HEADER)) {
                return compareDoc(o1, o2);
            } else if (header.equals(DocumentListDisplay.STATS_HEADER)) {
                return compareStats(o1, o2);
            } else if (header.equals(DocumentListDisplay.COMPLETE_HEADER)) {
                return compareTranslated(o1, o2);
            } else if (header.equals(DocumentListDisplay.INCOMPLETE_HEADER)) {
                return compareUntranslated(o1, o2);
            } else if (header.equals(DocumentListDisplay.REMAINING_HEADER)) {
                return compareRemaining(o1, o2);
            } else if (header.equals(DocumentListDisplay.LAST_UPLOAD_HEADER)) {
                return compareLastUpload(o1, o2);
            } else if (header
                    .equals(DocumentListDisplay.LAST_TRANSLATED_HEADER)) {
                return compareLastTranslated(o1, o2);
            }
            return 0;
        }

        private int comparePath(DocumentNode o1, DocumentNode o2) {
            if (o1.getDocInfo().getPath() == null
                    || o2.getDocInfo().getPath() == null) {
                return (o1.getDocInfo().getPath() == null) ? -1 : 1;
            } else {
                return o1.getDocInfo().getPath()
                        .compareTo(o2.getDocInfo().getPath());
            }
        }

        private int compareDoc(DocumentNode o1, DocumentNode o2) {
            return o1.getDocInfo().getName()
                    .compareTo(o2.getDocInfo().getName());
        }

        private int compareStats(DocumentNode o1, DocumentNode o2) {
            if (o1.getDocInfo().getStats() == null
                    || o2.getDocInfo().getStats() == null) {
                return (o1.getDocInfo().getStats() == null) ? -1 : 1;
            }

            if (display.getSelectedStatsOption().equals(
                    DocumentListDisplay.STATS_OPTION_MESSAGE)) {
                TranslationStatistics msgStats1 =
                        o1.getDocInfo().getStats()
                                .getStats(localeId.getId(), StatUnit.MESSAGE);
                TranslationStatistics msgStats2 =
                        o2.getDocInfo().getStats()
                                .getStats(localeId.getId(), StatUnit.MESSAGE);

                return (int) (msgStats1.getPercentTranslated() - msgStats2
                        .getPercentTranslated());
            } else {
                TranslationStatistics msgStats1 =
                        o1.getDocInfo().getStats()
                                .getStats(localeId.getId(), StatUnit.WORD);
                TranslationStatistics msgStats2 =
                        o2.getDocInfo().getStats()
                                .getStats(localeId.getId(), StatUnit.WORD);

                return (int) (msgStats1.getPercentTranslated() - msgStats2
                        .getPercentTranslated());
            }
        }

        private int compareTranslated(DocumentNode o1, DocumentNode o2) {
            if (o1.getDocInfo().getStats() == null
                    || o2.getDocInfo().getStats() == null) {
                return (o1.getDocInfo().getStats() == null) ? -1 : 1;
            }
            if (display.getSelectedStatsOption().equals(
                    DocumentListDisplay.STATS_OPTION_MESSAGE)) {
                TranslationStatistics msgStats1 =
                        o1.getDocInfo().getStats()
                                .getStats(localeId.getId(), StatUnit.MESSAGE);
                TranslationStatistics msgStats2 =
                        o2.getDocInfo().getStats()
                                .getStats(localeId.getId(), StatUnit.MESSAGE);
                return (int) (msgStats1.getTranslatedAndApproved() - msgStats2.getTranslatedAndApproved());
            } else {
                TranslationStatistics msgStats1 =
                        o1.getDocInfo().getStats()
                                .getStats(localeId.getId(), StatUnit.WORD);
                TranslationStatistics msgStats2 =
                        o2.getDocInfo().getStats()
                                .getStats(localeId.getId(), StatUnit.WORD);
                return (int) (msgStats1.getTranslatedAndApproved() - msgStats2.getTranslatedAndApproved());
            }
        }

        private int compareUntranslated(DocumentNode o1, DocumentNode o2) {
            if (o1.getDocInfo().getStats() == null
                    || o2.getDocInfo().getStats() == null) {
                return (o1.getDocInfo().getStats() == null) ? -1 : 1;
            }

            if (display.getSelectedStatsOption().equals(
                    DocumentListDisplay.STATS_OPTION_MESSAGE)) {
                TranslationStatistics msgStats1 =
                        o1.getDocInfo().getStats()
                                .getStats(localeId.getId(), StatUnit.MESSAGE);
                TranslationStatistics msgStats2 =
                        o2.getDocInfo().getStats()
                                .getStats(localeId.getId(), StatUnit.MESSAGE);
                return (int) (msgStats1.getIncomplete() - msgStats2
                        .getIncomplete());
            } else {
                TranslationStatistics wordStats1 =
                        o1.getDocInfo().getStats()
                                .getStats(localeId.getId(), StatUnit.WORD);
                TranslationStatistics wordStats2 =
                        o2.getDocInfo().getStats()
                                .getStats(localeId.getId(), StatUnit.WORD);
                return (int) (wordStats1.getIncomplete() - wordStats2
                        .getIncomplete());
            }
        }

        private int compareRemaining(DocumentNode o1, DocumentNode o2) {
            if (o1.getDocInfo().getStats() == null
                    || o2.getDocInfo().getStats() == null) {
                return (o1.getDocInfo().getStats() == null) ? -1 : 1;
            }

            TranslationStatistics msgStats1 =
                    o1.getDocInfo().getStats()
                            .getStats(localeId.getId(), StatUnit.WORD);
            TranslationStatistics msgStats2 =
                    o2.getDocInfo().getStats()
                            .getStats(localeId.getId(), StatUnit.WORD);

            if (msgStats1.getRemainingHours() == msgStats2.getRemainingHours()) {
                return 0;
            }
            return msgStats1.getRemainingHours() > msgStats2
                    .getRemainingHours() ? 1 : -1;
        }

        private int compareLastUpload(DocumentNode o1, DocumentNode o2) {
            if (o1.getDocInfo().getLastModified().getDate() == null
                    || o2.getDocInfo().getLastModified().getDate() == null) {
                return (o1.getDocInfo().getLastModified().getDate() == null) ? -1
                        : 1;
            }

            return o1.getDocInfo().getLastModified().getDate()
                    .after(o2.getDocInfo().getLastModified().getDate()) ? 1
                    : -1;
        }

        private int compareLastTranslated(DocumentNode o1, DocumentNode o2) {
            if (o1.getDocInfo().getLastTranslated().getDate() == null
                    || o2.getDocInfo().getLastTranslated().getDate() == null) {
                return (o1.getDocInfo().getLastTranslated().getDate() == null) ? -1
                        : 1;
            }

            return o1.getDocInfo().getLastTranslated().getDate()
                    .after(o2.getDocInfo().getLastTranslated().getDate()) ? 1
                    : -1;
        }
    }

    @Override
    public void pagerValueChanged(Integer value) {
        gotoPage(value);
    }

    public List<DocumentNode> getFilteredNodes() {
        return filteredNodes;
    }

    public void showLoading(boolean showLoading) {
        display.showLoading(showLoading);
    }

    public DocumentInfo getCurrentDocument() {
        return currentDocument;
    }
}
