package org.zanata.webtrans.client.ui;

import org.zanata.webtrans.client.view.TargetContentsDisplay;
import org.zanata.webtrans.shared.model.TransUnitId;
import com.google.gwt.core.shared.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.SimplePanel;

public class EditorButtonsWidget extends Composite {
    private static EditorButtonsWidgetUiBinder ourUiBinder = GWT
            .create(EditorButtonsWidgetUiBinder.class);

    @UiField
    HTMLPanel buttons;
    @UiField
    Anchor saveIcon;
    @UiField
    Anchor fuzzyIcon;
    @UiField
    Anchor cancelIcon;
    @UiField
    Anchor historyIcon;
    @UiField
    SimplePanel undoContainer;
    @UiField
    Anchor acceptIcon;
    @UiField
    Anchor rejectIcon;

    @UiField
    Style style;

    private TargetContentsDisplay.Listener listener;
    private TransUnitId id;

    public EditorButtonsWidget() {
        initWidget(ourUiBinder.createAndBindUi(this));
        setDisplayReviewButtons(listener != null && listener.canReview());
        setDisplayModifyTranslationButtons(listener != null
                && listener.canEditTranslation());
    }

    @Override
    protected void onEnsureDebugId(String baseID) {
        saveIcon.ensureDebugId(baseID + "-save-approve");
        fuzzyIcon.ensureDebugId(baseID + "-save-fuzzy");
        cancelIcon.ensureDebugId(baseID + "-cancel");
        historyIcon.ensureDebugId(baseID + "-history");
        undoContainer.ensureDebugId(baseID + "-undo");
        acceptIcon.ensureDebugId(baseID + "-accept");
        rejectIcon.ensureDebugId(baseID + "-reject");
    }

    private void setDisplayReviewButtons(boolean canReview) {
        acceptIcon.setVisible(canReview);
        rejectIcon.setVisible(canReview);
    }

    private void setDisplayModifyTranslationButtons(boolean canModify) {
        saveIcon.setVisible(canModify);
        fuzzyIcon.setVisible(canModify);
        cancelIcon.setVisible(canModify);
    }

    public void addUndo(final UndoLink undoLink) {
        undoLink.setLinkStyle("icon-undo ");
        undoLink.setUndoCallback(new UndoLink.UndoCallback() {
            @Override
            public void preUndo() {
                undoLink.setLinkStyle("icon-progress ");
            }

            @Override
            public void postUndoSuccess() {
                undoContainer.remove(undoLink);
            }
        });
        undoContainer.setWidget(undoLink);
    }

    @UiHandler("saveIcon")
    public void onSaveAsApproved(ClickEvent event) {
        listener.saveAsApprovedAndMoveNext(id);
        event.stopPropagation();
    }

    @UiHandler("fuzzyIcon")
    public void onSaveAsFuzzy(ClickEvent event) {
        listener.saveAsFuzzy(id);
        event.stopPropagation();
    }

    @UiHandler("cancelIcon")
    public void onCancel(ClickEvent event) {
        listener.onCancel(id);
        event.stopPropagation();
    }

    @UiHandler("historyIcon")
    public void onHistoryClick(ClickEvent event) {
        listener.showHistory(id);
        event.stopPropagation();
    }

    @UiHandler("acceptIcon")
    public void onAccept(ClickEvent event) {
        listener.acceptTranslation(id);
        event.stopPropagation();
    }

    @UiHandler("rejectIcon")
    public void onReject(ClickEvent event) {
        listener.rejectTranslation(id);
        event.stopPropagation();
    }

    public void setListener(TargetContentsDisplay.Listener listener) {
        this.listener = listener;
        setDisplayReviewButtons(listener.canReview());
        setDisplayModifyTranslationButtons(listener.canEditTranslation());
    }

    public void setId(TransUnitId id) {
        this.id = id;
    }

    interface EditorButtonsWidgetUiBinder extends
            UiBinder<HTMLPanel, EditorButtonsWidget> {
    }

    interface Style extends CssResource {
    }
}
