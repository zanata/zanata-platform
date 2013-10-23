package org.zanata.webtrans.client.ui;

import static org.zanata.webtrans.client.view.TargetContentsDisplay.EditingState.UNSAVED;

import java.util.List;
import java.util.Map;

import org.zanata.webtrans.client.view.TargetContentsDisplay;
import org.zanata.webtrans.shared.model.TransUnitId;
import org.zanata.webtrans.shared.model.ValidationAction;

import com.allen_sauer.gwt.log.client.Log;
import com.google.common.base.Objects;
import com.google.common.base.Strings;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.Widget;

public class Editor extends Composite implements ToggleEditor {
    private static EditorUiBinder uiBinder = GWT.create(EditorUiBinder.class);

    private final int index;

    private final TransUnitId id;

    private TargetContentsDisplay.Listener listener;

    @UiField
    Styles style;

    @UiField
    FocusPanel rootContainer;

    @UiField
    HorizontalPanel topContainer, textAreaTable;

    @UiField
    TranslatorListWidget translatorList;

    @UiField
    InlineLabel copyIcon;

    @UiField
    HTMLPanel targetWrapper;

    @UiField(provided = true)
    TextAreaWrapper textArea;

    // Timer period, in ms
    private final int TYPING_TIMER_INTERVAL = 200;

    // Validation will be forced after this many periods
    private final int TYPING_TIMER_INTERVALS_UNTIL_VALIDATION = 5;

    // Has a key been pressed since the timer was started or the last firing
    private boolean keyPressedSinceTimer;

    // Has a timer been started
    private boolean timerStarted;

    // The number of timer cycles since the last keydown
    private int typingCycles;

    // NB: In some cases, the idle detection may take almost 2 cycles
    // 1. Key pressed at time = 0
    // 2. Key pressed at time = 1ms (keyPressedSinceTimer = true)
    // 3. Timer goes off at 200ms without validating
    // 4. Timer goes off at 400ms and runs validation
    // This could be fixed by using 2 separate timers
    final Timer typingTimer = new Timer() {
        @Override
        public void run() {
            if (keyPressedSinceTimer) {
                // still typing, validate periodically
                keyPressedSinceTimer = false;
                typingCycles++;
                if (typingCycles % TYPING_TIMER_INTERVALS_UNTIL_VALIDATION == 0) {
                    fireValidationEvent();
                }
            } else {
                // finished, validate immediately
                this.cancel();
                timerStarted = false;
                fireValidationEvent();
            }
        }
    };

    public Editor(String displayString, final int index,
            final TargetContentsDisplay.Listener listener, final TransUnitId id) {
        this.listener = listener;
        this.index = index;
        this.id = id;
        if (listener.getConfigState().isUseCodeMirrorEditor()) {
            Command onCodeMirrorFocusCallback = new Command() {

                @Override
                public void execute() {
                    listener.onEditorClicked(id, index);
                }
            };
            textArea = new CodeMirrorEditor(onCodeMirrorFocusCallback);
        } else {
            textArea = new EditorTextArea(displayString);
        }

        initWidget(uiBinder.createAndBindUi(this));
        // determine whether to show or hide buttons
        showCopySourceButton(listener.isDisplayButtons());

        if (!listener.canEditTranslation()) {
            setViewMode(ViewMode.VIEW);
        } else {
            setViewMode(ViewMode.EDIT);
        }

        setText(displayString);
    }

    @Override
    public void setEnableSpellCheck(Boolean enabled) {
        targetWrapper.getElement().setAttribute("contenteditable",
                enabled.toString());
        targetWrapper.getElement().setAttribute("spellcheck",
                enabled.toString());
    }

    private void fireValidationEvent() {
        if (listener.canEditTranslation()) {
            listener.validate(this);
        }
    }

    /**
     * This gets triggered on each keydown event from both codemirror and plain
     * text area.
     */
    @UiHandler("textArea")
    public void onValueChange(ValueChangeEvent<String> event) {
        if (timerStarted) {
            keyPressedSinceTimer = true;
        } else {
            // set false so that next keypress is detectable
            keyPressedSinceTimer = false;
            timerStarted = true;
            typingCycles = 0;
            typingTimer.scheduleRepeating(TYPING_TIMER_INTERVAL);
        }
        listener.setEditingState(id, UNSAVED);
    }

    @UiHandler("textArea")
    public void onTextAreaFocus(FocusEvent event) {
        listener.onEditorClicked(id, index);
        textArea.setEditing(true);
        fireValidationEvent();
    }

    @UiHandler("textArea")
    public void onTextAreaBlur(BlurEvent event) {
        textArea.setEditing(false);
    }

    @UiHandler("copyIcon")
    public void onCopySource(ClickEvent event) {
        listener.copySource(this, id);
    }

    @Override
    public ViewMode getViewMode() {
        if (textArea.isReadOnly()) {
            return ViewMode.VIEW;
        } else {
            return ViewMode.EDIT;
        }
    }

    @Override
    public void setViewMode(ViewMode viewMode) {
        textArea.setReadOnly(viewMode == ViewMode.VIEW);
        translatorList.setVisible(viewMode == ViewMode.EDIT);
        toggleTranslatorList();
    }

    public void toggleTranslatorList() {
        if (translatorList.isVisible() && !translatorList.isEmpty()) {
            textAreaTable.setCellWidth(translatorList, "60px");
        } else {
            textAreaTable.setCellWidth(translatorList, "0");
        }
    }

    @Override
    public void setTextAndValidate(String text) {
        setText(text);
        fireValidationEvent();
    }

    @Override
    public void setText(String text) {
        if (!Strings.isNullOrEmpty(text)) {
            textArea.setText(text);
        } else {
            textArea.setText("");
        }
    }

    @Override
    public String getText() {
        return textArea.getText();
    }

    @Override
    public void setFocus() {
        textArea.setEditing(true);
    }

    @Override
    public void insertTextInCursorPosition(String suggestion) {
        String preCursor =
                textArea.getText().substring(0, textArea.getCursorPos());
        String postCursor =
                textArea.getText().substring(textArea.getCursorPos(),
                        textArea.getText().length());

        setTextAndValidate(preCursor + suggestion + postCursor);
        textArea.setCursorPos(textArea.getText().indexOf(suggestion)
                + suggestion.length());
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this).add("id", id)
        // .add("label", label.getText())
        // .add("textArea", textArea.getText())
                .add("isFocused", isFocused()).toString();
    }

    @Override
    public int getIndex() {
        return index;
    }

    @Override
    public TransUnitId getId() {
        return id;
    }

    @Override
    public void showCopySourceButton(boolean displayButtons) {
        copyIcon.setVisible(displayButtons);
    }

    @Override
    public void updateValidationMessages(
            Map<ValidationAction, List<String>> messages) {
        if (messages.isEmpty()) {
            targetWrapper.removeStyleName(style.hasValidationError());
        } else {
            Log.info(id + " id has error: " + messages.values());
            targetWrapper.addStyleName(style.hasValidationError());
        }
    }

    @Override
    public void addTranslator(String name, String color) {
        translatorList.addTranslator(name, color);
        toggleTranslatorList();
    }

    @Override
    public void clearTranslatorList() {
        translatorList.clearTranslatorList();
        toggleTranslatorList();
    }

    @Override
    public void highlightSearch(String findMessage) {
        textArea.highlight(findMessage);
    }

    @Override
    public void refresh() {
        textArea.refresh();
    }

    @Override
    public void removeTranslator(String name, String color) {
        translatorList.removeTranslator(name, color);
        toggleTranslatorList();
    }

    @Override
    public boolean isFocused() {
        return textArea.isEditing();
    }

    interface EditorUiBinder extends UiBinder<Widget, Editor> {
    }

    interface Styles extends CssResource {

        String rootContainer();

        String hasValidationError();

        String copyButton();

        String targetContainer();
    }
}
