/*
 * Copyright 2013, Red Hat, Inc. and individual contributors as indicated by the
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
package org.zanata.webtrans.client.ui;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.zanata.rest.dto.stats.ContainerTranslationStatistics;
import org.zanata.rest.dto.stats.TranslationStatistics;
import org.zanata.rest.dto.stats.TranslationStatistics.StatUnit;
import org.zanata.webtrans.client.Application;
import org.zanata.webtrans.client.resources.Resources;
import org.zanata.webtrans.client.resources.WebTransMessages;
import org.zanata.webtrans.client.ui.HasTranslationStats.LabelFormat;
import org.zanata.webtrans.client.util.DateUtil;
import org.zanata.webtrans.client.util.TextFormatUtil;
import org.zanata.webtrans.client.view.DocumentListDisplay;
import org.zanata.webtrans.shared.model.AuditInfo;
import org.zanata.webtrans.shared.model.DocumentId;
import org.zanata.webtrans.shared.model.DocumentInfo;
import org.zanata.webtrans.shared.model.UserWorkspaceContext;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.Widget;

/**
 *
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 *
 */
public class DocumentListTable extends FlexTable {
    public static enum DocValidationStatus {
        HasError, NoError, Unknown
    }

    private static int PATH_COLUMN = 0;
    private static int DOC_COLUMN = 1;
    private static int STATS_COLUMN = 2;
    private static int COMPLETE_COLUMN = 3;
    private static int INCOMPLETE_COLUMN = 4;
    private static int REMAINING_COLUMN = 5;
    private static int LAST_UPLOAD_COLUMN = 6;
    private static int LAST_TRANSLATED_COLUMN = 7;
    private static int ACTION_COLUMN = 8;

    private final UserWorkspaceContext userWorkspaceContext;
    private DocumentListDisplay.Listener listener;
    private final WebTransMessages messages;
    private final Resources resources;

    public DocumentListTable(final UserWorkspaceContext userWorkspaceContext,
            final WebTransMessages messages, final Resources resources) {
        super();
        setStylePrimaryName("DocumentListTable");
        setCellPadding(0);
        setCellSpacing(0);

        this.userWorkspaceContext = userWorkspaceContext;

        this.messages = messages;
        this.resources = resources;

        buildHeader();

        ensureDebugId("documentListTable");
    }

    public void setListener(final DocumentListDisplay.Listener listener) {
        this.listener = listener;
    }

    public void clearContent() {
        while (this.getRowCount() > 1) {
            this.removeRow(this.getRowCount() - 1);
        }
    }

    private class HeaderClickHandler implements ClickHandler {
        private String header;
        private boolean asc = true;

        public HeaderClickHandler(String header) {
            this.header = header;
        }

        @Override
        public void onClick(ClickEvent event) {
            asc = !asc;
            listener.sortList(header, asc);
        }
    }

    private void buildHeader() {
        InlineLabel pathHeader = new InlineLabel(messages.columnHeaderPath());
        pathHeader.addClickHandler(new HeaderClickHandler(
                DocumentListDisplay.PATH_HEADER));

        InlineLabel docHeader =
                new InlineLabel(messages.columnHeaderDocument());
        docHeader.addClickHandler(new HeaderClickHandler(
                DocumentListDisplay.DOC_HEADER));

        InlineLabel statsHeader =
                new InlineLabel(messages.columnHeaderStatistic());
        statsHeader.addClickHandler(new HeaderClickHandler(
                DocumentListDisplay.STATS_HEADER));

        InlineLabel completeHeader =
                new InlineLabel(messages.columnHeaderComplete());
        completeHeader.addClickHandler(new HeaderClickHandler(
                DocumentListDisplay.COMPLETE_HEADER));

        InlineLabel incompletedHeader =
                new InlineLabel(messages.columnHeaderIncomplete());
        incompletedHeader.addClickHandler(new HeaderClickHandler(
                DocumentListDisplay.INCOMPLETE_HEADER));

        InlineLabel remainingHeader =
                new InlineLabel(messages.columnHeaderRemaining());
        remainingHeader.addClickHandler(new HeaderClickHandler(
                DocumentListDisplay.REMAINING_HEADER));

        InlineLabel lastUploadHeader =
                new InlineLabel(messages.columnHeaderLastUpload());
        lastUploadHeader.addClickHandler(new HeaderClickHandler(
                DocumentListDisplay.LAST_UPLOAD_HEADER));

        InlineLabel lastTranslatedHeader =
                new InlineLabel(messages.columnHeaderLastTranslated());
        lastTranslatedHeader.addClickHandler(new HeaderClickHandler(
                DocumentListDisplay.LAST_TRANSLATED_HEADER));

        InlineLabel actionHeader =
                new InlineLabel(messages.columnHeaderAction());

        this.setWidget(0, PATH_COLUMN, pathHeader);
        this.setWidget(0, DOC_COLUMN, docHeader);
        this.setWidget(0, STATS_COLUMN, statsHeader);
        this.setWidget(0, COMPLETE_COLUMN, completeHeader);
        this.setWidget(0, INCOMPLETE_COLUMN, incompletedHeader);
        this.setWidget(0, REMAINING_COLUMN, remainingHeader);
        this.setWidget(0, LAST_UPLOAD_COLUMN, lastUploadHeader);
        this.setWidget(0, LAST_TRANSLATED_COLUMN, lastTranslatedHeader);
        this.setWidget(0, ACTION_COLUMN, actionHeader);

        this.getCellFormatter().setStyleName(0, PATH_COLUMN,
                "docListHeader sortable");
        this.getCellFormatter().setStyleName(0, DOC_COLUMN,
                "docListHeader sortable");
        this.getCellFormatter().setStyleName(0, STATS_COLUMN,
                "docListHeader sortable");
        this.getCellFormatter().setStyleName(0, COMPLETE_COLUMN,
                "docListHeader sortable");
        this.getCellFormatter().setStyleName(0, INCOMPLETE_COLUMN,
                "docListHeader sortable");
        this.getCellFormatter().setStyleName(0, REMAINING_COLUMN,
                "docListHeader sortable");
        this.getCellFormatter().setStyleName(0, LAST_UPLOAD_COLUMN,
                "docListHeader sortable");
        this.getCellFormatter().setStyleName(0, LAST_TRANSLATED_COLUMN,
                "docListHeader sortable");
        this.getCellFormatter().setStyleName(0, ACTION_COLUMN, "docListHeader");
    }

    public HashMap<DocumentId, Integer> buildContent(List<DocumentNode> nodes,
            boolean statsByWords) {
        clearContent();

        HashMap<DocumentId, Integer> pageRows = Maps.newHashMap();

        for (int i = 0; i < nodes.size(); i++) {
            DocumentNode node = nodes.get(i);
            pageRows.put(node.getDocInfo().getId(), i + 1);

            this.setWidget(i + 1, PATH_COLUMN, getPathWidget(node.getDocInfo()));
            this.setWidget(i + 1, DOC_COLUMN, new DocWidget(node.getDocInfo()));

            this.setWidget(i + 1, STATS_COLUMN,
                    getStatsWidget(node.getDocInfo(), statsByWords));
            this.setWidget(i + 1, COMPLETE_COLUMN,
                    getCompleteWidget(node.getDocInfo(), statsByWords));
            this.setWidget(i + 1, INCOMPLETE_COLUMN,
                    getIncompleteWidget(node.getDocInfo(), statsByWords));
            this.setWidget(i + 1, REMAINING_COLUMN,
                    getRemainingWidget(node.getDocInfo()));

            this.setWidget(i + 1, LAST_UPLOAD_COLUMN,
                    getAuditInfo(node.getDocInfo().getLastModified()));
            this.setWidget(i + 1, LAST_TRANSLATED_COLUMN,
                    getAuditInfo(node.getDocInfo().getLastTranslated()));

            this.setWidget(i + 1, ACTION_COLUMN,
                    getActionWidget(node.getDocInfo()));

            this.getCellFormatter().setStyleName(i + 1, PATH_COLUMN, "pathCol");
            this.getCellFormatter().setStyleName(i + 1, DOC_COLUMN,
                    "documentCol");
            this.getCellFormatter().setStyleName(i + 1, STATS_COLUMN,
                    "statisticCol");
            this.getCellFormatter().setStyleName(i + 1, COMPLETE_COLUMN,
                    "translatedCol");
            this.getCellFormatter().setStyleName(i + 1, INCOMPLETE_COLUMN,
                    "untranslatedCol");
            this.getCellFormatter().setStyleName(i + 1, REMAINING_COLUMN,
                    "remainingCol");
            this.getCellFormatter().setStyleName(i + 1, LAST_UPLOAD_COLUMN,
                    "txt--understated auditCol");
            this.getCellFormatter().setStyleName(i + 1, LAST_TRANSLATED_COLUMN,
                    "txt--understated auditCol");
            this.getCellFormatter().setStyleName(i + 1, ACTION_COLUMN,
                    "actionCol");

            if (node.getDocInfo().hasError() == null) {
                updateRowHasError(i + 1, DocValidationStatus.Unknown);
            } else if (node.getDocInfo().hasError()) {
                updateRowHasError(i + 1, DocValidationStatus.HasError);
            } else if (!node.getDocInfo().hasError()) {
                updateRowHasError(i + 1, DocValidationStatus.NoError);
            }
        }

        return pageRows;
    }

    private Widget getPathWidget(final DocumentInfo docInfo) {
        InlineLabel pathLabel = new InlineLabel(docInfo.getPath());
        pathLabel.setTitle(docInfo.getPath());

        return pathLabel;
    }

    private interface HasValidationResult {
        void showLoading();

        void setValidationResult(DocValidationStatus status);
    }

    private class DocWidget extends FlowPanel implements HasValidationResult {
        private final InlineLabel docLabel;
        private final Image loading;
        private final InlineLabel noError;
        private final InlineLabel hasError;

        public DocWidget(final DocumentInfo docInfo) {
            super();
            loading = new Image(resources.spinner());
            loading.setVisible(false);
            this.add(loading);

            noError = new InlineLabel();
            noError.setStyleName("i--checkmark i");
            noError.setVisible(false);
            this.add(noError);

            hasError = new InlineLabel();
            hasError.setStyleName("i i--cancel txt--danger");
            hasError.setVisible(false);
            this.add(hasError);

            docLabel = new InlineLabel(docInfo.getName());
            docLabel.ensureDebugId("docLabel-" + docInfo.getId().getDocId());
            docLabel.setTitle(docInfo.getName());
            docLabel.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    listener.fireDocumentSelection(docInfo);
                }
            });
            this.add(docLabel);
        }

        @Override
        public void showLoading() {
            loading.setVisible(true);

            noError.setVisible(false);
            hasError.setVisible(false);
        }

        @Override
        public void setValidationResult(DocValidationStatus status) {
            loading.setVisible(false);
            noError.setVisible(false);
            hasError.setVisible(false);

            if (status == DocValidationStatus.HasError) {
                hasError.setVisible(true);
                docLabel.setTitle(messages.hasValidationErrors(docLabel
                        .getText()));
                docLabel.addStyleName("hasError");
            } else if (status == DocValidationStatus.NoError) {
                noError.setVisible(true);
                docLabel.setTitle(docLabel.getText());
                docLabel.removeStyleName("hasError");
            } else if (status == DocValidationStatus.Unknown) {
                docLabel.setTitle(docLabel.getText());
                docLabel.removeStyleName("hasError");
            }
        }
    }

    private Widget getStatsWidget(DocumentInfo docInfo, boolean statsByWords) {
        FlowPanel panel = new FlowPanel();
        final TransUnitCountBar graph =
                new TransUnitCountBar(userWorkspaceContext, messages,
                        LabelFormat.PERCENT_COMPLETE, false);
        Image loading = new Image(resources.spinner());
        panel.add(graph);
        panel.add(loading);

        if (docInfo.getStats() == null) {
            loading.setVisible(true);
            graph.setVisible(false);
        } else {
            loading.setVisible(false);
            graph.setVisible(true);
            graph.setStats(docInfo.getStats(), statsByWords);
        }

        return panel;
    }

    private Widget
            getCompleteWidget(DocumentInfo docInfo, boolean statsByWords) {
        String text = "0";
        if (docInfo.getStats() != null) {
            String locale =
                    userWorkspaceContext.getWorkspaceContext().getWorkspaceId()
                            .getLocaleId().getId();
            TranslationStatistics stats;
            if (statsByWords) {
                stats = docInfo.getStats().getStats(locale, StatUnit.WORD);
            } else {
                stats = docInfo.getStats().getStats(locale, StatUnit.MESSAGE);
            }
            text = String.valueOf(stats.getTranslatedAndApproved());
        }
        return new InlineLabel(text);
    }

    private Widget getIncompleteWidget(DocumentInfo docInfo,
            boolean statsByWords) {
        String text = "0";
        if (docInfo.getStats() != null) {
            String locale =
                    userWorkspaceContext.getWorkspaceContext().getWorkspaceId()
                            .getLocaleId().getId();
            TranslationStatistics stats;
            if (statsByWords) {
                stats = docInfo.getStats().getStats(locale, StatUnit.WORD);
            } else {
                stats = docInfo.getStats().getStats(locale, StatUnit.MESSAGE);
            }
            text = String.valueOf(stats.getIncomplete());
        }
        return new InlineLabel(text);
    }

    private Widget getRemainingWidget(DocumentInfo docInfo) {
        String text = "0";
        if (docInfo.getStats() != null) {
            String locale =
                    userWorkspaceContext.getWorkspaceContext().getWorkspaceId()
                            .getLocaleId().getId();
            text =
                    messages.statusBarLabelHours(TextFormatUtil
                            .formatHours(docInfo.getStats()
                                .getStats(locale, StatUnit.WORD)
                                .getRemainingHours()));
        }

        return new InlineLabel(text);
    }

    private Widget getActionWidget(final DocumentInfo docInfo) {
        FlowPanel panel = new FlowPanel();

        for (Map.Entry<String, String> entry : docInfo.getDownloadExtensions()
                .entrySet()) {
            Anchor anchor = new Anchor(entry.getKey());
            anchor.addStyleName("l--push-right-half");
            anchor.setTitle(messages.downloadFileTitle(entry.getKey()));
            anchor.setHref(Application.getFileDownloadURL(userWorkspaceContext
                    .getWorkspaceContext().getWorkspaceId(), entry.getValue()));
            anchor.setTarget("_blank");
            panel.add(anchor);
        }
        return panel;
    }

    private Widget getAuditInfo(AuditInfo lastTranslatedInfo) {
        FlowPanel panel = new FlowPanel();
        if (lastTranslatedInfo != null) {
            if (lastTranslatedInfo.getDate() != null) {
                panel.add(new InlineLabel(DateUtil
                        .formatShortDate(lastTranslatedInfo.getDate())));
            }
            String username = lastTranslatedInfo.getUsername();
            if (!Strings.isNullOrEmpty(username)) {
                Anchor anchor = new Anchor(" by " + username);
                anchor.setTitle(username);
                anchor.setHref(Application.getUserProfileURL(username));
                anchor.setTarget("_blank");

                panel.add(anchor);
            }
        }
        return panel;
    }

    public void updateRowHasError(int row, DocValidationStatus status) {
        HasValidationResult panel =
                (HasValidationResult) this.getWidget(row, DOC_COLUMN);
        panel.setValidationResult(status);
    }

    public void updateLastTranslatedInfo(int row, AuditInfo lastTranslated) {
        this.setWidget(row, LAST_TRANSLATED_COLUMN,
                getAuditInfo(lastTranslated));
    }

    public void updateStats(int row, ContainerTranslationStatistics stats,
            boolean statsByWords) {
        if (stats != null) {
            FlowPanel panel = (FlowPanel) this.getWidget(row, STATS_COLUMN);

            TransUnitCountBar graph = (TransUnitCountBar) panel.getWidget(0);
            graph.setStats(stats, true);
            graph.setVisible(true);

            Image loading = (Image) panel.getWidget(1);
            loading.setVisible(false);

            HasText translated = (HasText) this.getWidget(row, COMPLETE_COLUMN);
            HasText untranslated =
                    (HasText) this.getWidget(row, INCOMPLETE_COLUMN);

            graph.setStatOption(statsByWords);

            String locale =
                    userWorkspaceContext.getWorkspaceContext().getWorkspaceId()
                            .getLocaleId().toString();
            TranslationStatistics wordStats =
                    stats.getStats(locale, StatUnit.WORD);

            if (statsByWords) {
                translated.setText(String.valueOf(wordStats
                        .getTranslatedAndApproved()));
                untranslated.setText(String.valueOf(wordStats.getIncomplete()));
            } else {

                TranslationStatistics msgStats =
                        stats.getStats(locale, StatUnit.MESSAGE);

                translated.setText(String.valueOf(msgStats
                        .getTranslatedAndApproved()));
                untranslated.setText(String.valueOf(msgStats.getIncomplete()));
            }

            HasText remainingHour =
                    (HasText) this.getWidget(row, REMAINING_COLUMN);
            remainingHour.setText(messages.statusBarLabelHours(TextFormatUtil
                    .formatHours(wordStats.getRemainingHours())));
        }
    }

    public void setStatsFilter(boolean statsByWords, Integer row) {
        FlowPanel panel = (FlowPanel) this.getWidget(row, STATS_COLUMN);

        TransUnitCountBar graph = (TransUnitCountBar) panel.getWidget(0);
        graph.setStatOption(statsByWords);

        HasText completed = (HasText) this.getWidget(row, COMPLETE_COLUMN);
        HasText incomplete = (HasText) this.getWidget(row, INCOMPLETE_COLUMN);

        if (statsByWords) {
            completed.setText(String.valueOf(graph.getWordsApproved()
                    + graph.getWordsTranslated()));
            incomplete.setText(String.valueOf(graph.getWordsUntranslated()
                    + graph.getWordsDraft()));
        } else {
            completed.setText(String.valueOf(graph.getUnitApproved()
                    + graph.getUnitTranslated()));
            incomplete.setText(String.valueOf(graph.getUnitUntranslated()
                    + graph.getUnitDraft()));
        }
    }

    public void showRowLoading(int row) {
        HasValidationResult panel =
                (HasValidationResult) this.getWidget(row, DOC_COLUMN);
        panel.showLoading();
    }
}
