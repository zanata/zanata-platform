package org.zanata.webtrans.client.view;

import java.util.Date;

import org.zanata.ui.input.TextInput;
import org.zanata.webtrans.client.Application;
import org.zanata.webtrans.client.resources.Resources;
import org.zanata.webtrans.client.resources.UiMessages;
import org.zanata.webtrans.client.ui.DialogBoxCloseButton;
import org.zanata.webtrans.client.util.DateUtil;

import com.google.common.base.Strings;
import com.google.gwt.core.shared.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.InlineHTML;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class GlossaryDetailsView extends DialogBox
    implements GlossaryDetailsDisplay {

    interface GlossaryDetailsIUiBinder extends
            UiBinder<HTMLPanel, GlossaryDetailsView> {
    }

    private static GlossaryDetailsIUiBinder uiBinder = GWT
            .create(GlossaryDetailsIUiBinder.class);

    @UiField
    TextArea sourceText, targetText;

    @UiField
    InlineHTML description, pos, targetComment;

    @UiField
    InlineLabel lastModified, srcRef;

    @UiField
    Label sourceLabel, targetLabel;

    @UiField
    ListBox entryListBox;

    @UiField(provided = true)
    DialogBoxCloseButton dismissButton;

    @UiField
    Anchor link;

    private Listener listener;

    private final UiMessages messages;

    @Inject
    public GlossaryDetailsView(UiMessages messages) {
        super(true, false);
        setGlassEnabled(true);
        this.messages = messages;

        dismissButton = new DialogBoxCloseButton(this);

        HTMLPanel container = uiBinder.createAndBindUi(this);
        getCaption().setText(messages.glossaryDetails());
        setWidget(container);
        dismissButton.setText(messages.dismiss());
    }

    public void hide() {
        hide(false);
    }

    @Override
    public void setUrl(String url) {
        link.setHref(url);
    }

    @Override
    public void setDescription(String descriptionText) {
        setText(description, descriptionText);
    }

    @Override
    public void setSrcRef(String srcRef) {
        this.srcRef.setText(srcRef);
    }

    @Override
    public void setPos(String posText) {
        setText(pos, posText);
    }

    @Override
    public void setTargetComment(String targetCommentText) {
        setText(targetComment, targetCommentText);
    }

    private void setText(InlineHTML field, String text) {
        if (Strings.isNullOrEmpty(text)) {
            field.setText(messages.noContent());
            field.addStyleName("txt--meta");
        } else {
            field.setText(text);
            field.removeStyleName("txt--meta");
        }
    }

    @Override
    public void setSourceLabel(String label) {
        sourceLabel.setText(label);
    }

    @Override
    public void setTargetLabel(String label) {
        targetLabel.setText(label);
    }

    @Override
    public Widget asWidget() {
        return this;
    }

    @Override
    public void setSourceText(String source) {
        sourceText.setText(source);
    }

    @Override
    public HasText getTargetComment() {
        return targetComment;
    }

    @Override
    public HasText getPos() {
        return pos;
    }

    @Override
    public HasText getDescription() {
        return description;
    }

    @Override
    public HasText getTargetText() {
        return targetText;
    }

    @UiHandler("entryListBox")
    public void onEntryListBoxChange(ChangeEvent event) {
        listener.selectEntry(entryListBox.getSelectedIndex());
    }

    @Override
    public void addEntry(String text) {
        entryListBox.addItem(text);
    }

    @Override
    public void clearEntries() {
        entryListBox.clear();
    }

    @Override
    public void setListener(Listener listener) {
        this.listener = listener;
    }

    @Override
    public void setLastModifiedDate(Date lastModifiedDate) {
        lastModified.setText(messages.lastModifiedOn(DateUtil
                .formatShortDate(lastModifiedDate)));
    }

}
