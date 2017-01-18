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
package org.zanata.webtrans.client.view;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.google.common.base.MoreObjects;
import org.zanata.common.ContentState;
import org.zanata.webtrans.client.events.ReviewCommentEvent;
import org.zanata.webtrans.client.ui.Editor;
import org.zanata.webtrans.client.ui.EditorButtonsWidget;
import org.zanata.webtrans.client.ui.ToggleEditor;
import org.zanata.webtrans.client.ui.TranslatorListWidget;
import org.zanata.webtrans.client.ui.UndoLink;
import org.zanata.webtrans.client.ui.ValidationMessagePanelView;
import org.zanata.webtrans.client.util.ContentStateToStyleUtil;
import org.zanata.webtrans.shared.model.TransUnit;
import org.zanata.webtrans.shared.model.TransUnitId;
import org.zanata.webtrans.shared.model.ValidationAction;
import com.google.common.base.Objects;
import com.google.common.collect.Lists;
import com.google.gwt.core.shared.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.google.inject.Provider;

import net.customware.gwt.presenter.client.EventBus;

public class TargetContentsView extends Composite implements
        TargetContentsDisplay {
    private static final int COLUMNS = 1;
    private static Binder binder = GWT.create(Binder.class);
    private final EventBus eventBus;

    @UiField
    Grid editorGrid;

    @UiField
    HTMLPanel container;

    @UiField(provided = true)
    ValidationMessagePanelView validationPanel;

    @UiField
    Styles style;

    @UiField
    Label savingIndicator;

    @UiField(provided = true)
    EditorButtonsWidget buttons;

    @UiField
    Label commentIndicator;

    @UiField
    TranslatorListWidget translatorList;

    private HTMLPanel rootPanel;
    private ArrayList<ToggleEditor> editors;
    private Listener listener;

    private EditingState editingState = EditingState.SAVED;
    private TransUnit cachedValue;

    @Inject
    public TargetContentsView(
            Provider<ValidationMessagePanelView> validationMessagePanelViewProvider,
            EventBus eventBus) {
        this.eventBus = eventBus;
        buttons = new EditorButtonsWidget();
        validationPanel = validationMessagePanelViewProvider.get();
        rootPanel = binder.createAndBindUi(this);
        editorGrid.ensureDebugId("target-contents-grid");
        editors = Lists.newArrayList();
    }

    @UiHandler("commentIndicator")
    public void commentIndicatorClicked(ClickEvent event) {
        eventBus.fireEvent(new ReviewCommentEvent(getId()));
    }

    @Override
    public void showButtons(boolean displayButtons) {
        buttons.setVisible(displayButtons);
        for (ToggleEditor editor : editors) {
            editor.showCopySourceButton(displayButtons);
        }
    }

    @Override
    public void focusEditor(int currentEditorIndex) {
        if (currentEditorIndex >= 0 && currentEditorIndex < editors.size()) {
            editors.get(currentEditorIndex).setFocus();
        }
    }

    @Override
    public void addUndo(final UndoLink undoLink) {

        buttons.addUndo(undoLink);
    }

    @Override
    public void setValueAndCreateNewEditors(TransUnit transUnit) {
        buttons.ensureDebugId("target-" + transUnit.getRowIndex());
        setCachedTU(transUnit);
        updateCommentIndicator(transUnit.getCommentsCount());

        editors.clear();
        List<String> cachedTargets = cachedValue.getTargets();
        if (cachedTargets == null) {
            cachedTargets = Lists.newArrayList("");
        }
        editorGrid.resize(cachedTargets.size(), COLUMNS);
        int rowIndex = 0;
        for (String target : cachedTargets) {
            Editor editor =
                    new Editor(target, rowIndex, listener, transUnit.getId());
            editor.ensureDebugId(transUnit.getRowIndex() + "-");
            editorGrid.setWidget(rowIndex, 0, editor);
            editors.add(editor);
            rowIndex++;
        }
        editingState = EditingState.SAVED;
    }

    private static String resolveStyleName(ContentState status) {
        return ContentStateToStyleUtil.stateToStyle(status, "TableEditorRow ");
    }

    @Override
    public void updateCommentIndicator(int commentsCount) {
        commentIndicator.setVisible(commentsCount > 0);
        commentIndicator.setText(String.valueOf(commentsCount));
    }

    @Override
    public ContentState getCachedState() {
        return cachedValue.getStatus();
    }

    @Override
    public void toggleSyntaxHighlighting() {
        for (ToggleEditor editor : editors) {
            editor.toggleType();
        }
    }

    @Override
    public void setState(EditingState editingState) {
        this.editingState = editingState;
        if (editingState == EditingState.UNSAVED) {
            editorGrid.addStyleName(style.unsaved());
            savingIndicator.setVisible(false);

        } else if (editingState == EditingState.SAVING) {
            savingIndicator.setVisible(true);
            editorGrid.removeStyleName(style.unsaved());
        } else {
            container.setStyleName(resolveStyleName(cachedValue.getStatus()));
            container.addStyleName("list--no-bullets");
            editorGrid.removeStyleName(style.unsaved());
            savingIndicator.setVisible(false);
        }
    }

    @Override
    public EditingState getEditingState() {
        return editingState;
    }

    @Override
    public void updateCachedTargetsAndVersion(List<String> targets,
            Integer verNum, ContentState status) {
        setCachedTU(TransUnit.Builder.from(cachedValue).setTargets(targets)
                .setVerNum(verNum).setStatus(status).build());
    }

    private void setCachedTU(TransUnit newTransUnit) {
        cachedValue = newTransUnit;
        container.setStyleName(resolveStyleName(cachedValue.getStatus()));
        container.addStyleName("list--no-bullets");
        buttons.setId(cachedValue.getId());
    }

    @Override
    public void highlightSearch(String findMessage) {
        for (ToggleEditor editor : editors) {
            editor.highlightSearch(findMessage);
        }
    }

    @Override
    public ArrayList<String> getNewTargets() {
        ArrayList<String> result = Lists.newArrayList();
        for (ToggleEditor editor : editors) {
            result.add(editor.getText());
        }
        return result;
    }

    @Override
    public List<String> getCachedTargets() {
        return cachedValue.getTargets();
    }

    @Override
    public TransUnitId getId() {
        return cachedValue.getId();
    }

    @Override
    public ArrayList<ToggleEditor> getEditors() {
        return editors;
    }

    @Override
    public void setListener(Listener listener) {
        this.listener = listener;
        buttons.setListener(listener);
    }

    @Override
    public void revertEditorContents() {
        List<String> cachedTargets = cachedValue.getTargets();
        for (int i = 0; i < cachedTargets.size(); i++) {
            String target = cachedTargets.get(i);
            editors.get(i).setTextAndValidate(target);
        }
        container.setStyleName(resolveStyleName(cachedValue.getStatus()));
        container.addStyleName("list--no-bullets");
    }

    @Override
    public void refresh() {
        for (ToggleEditor editor : editors) {
            editor.refresh();
        }
    }

    @Override
    public Integer getVerNum() {
        return cachedValue.getVerNum();
    }

    @Override
    public void setToMode(ToggleEditor.ViewMode viewMode) {
        for (ToggleEditor editor : editors) {
            editor.setViewMode(viewMode);
        }
        translatorList.setVisible(viewMode == ToggleEditor.ViewMode.EDIT);
        validationPanel
                .setVisibleIfHasError(viewMode == ToggleEditor.ViewMode.EDIT);
    }

    @Override
    public void addTranslator(String name, String color) {
        translatorList.addTranslator(name, color);
    }

    @Override
    public void clearTranslatorList() {
        translatorList.clearTranslatorList();
    }

    @Override
    public void removeTranslator(String name, String color) {
        translatorList.removeTranslator(name, color);
    }

    @Override
    public void updateValidationMessages(
            Map<ValidationAction, List<String>> messages) {
        validationPanel.updateValidationMessages(messages);
    }

    @Override
    public Widget asWidget() {
        return rootPanel;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("editors", editors).toString();
    }

    interface Styles extends CssResource {

        String unsaved();

        String saving();

        String commentIndicator();
    }

    interface Binder extends UiBinder<HTMLPanel, TargetContentsView> {
    }

    @Override
    public Map<ValidationAction, List<String>> getErrorMessages() {
        return validationPanel.getErrorMessages();
    }
}
