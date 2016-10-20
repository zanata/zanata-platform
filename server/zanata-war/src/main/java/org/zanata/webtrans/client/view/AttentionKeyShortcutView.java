package org.zanata.webtrans.client.view;

import org.zanata.webtrans.client.resources.WebTransMessages;
import com.google.gwt.core.shared.GWT;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class AttentionKeyShortcutView extends PopupPanel implements
        AttentionKeyShortcutDisplay {
    private static AttentionKeyShortcutViewUiBinder uiBinder = GWT
            .create(AttentionKeyShortcutViewUiBinder.class);

    interface AttentionKeyShortcutViewUiBinder extends
            UiBinder<HorizontalPanel, AttentionKeyShortcutView> {
    }

    interface Styles extends CssResource {
        String shortcutHint();
    }

    @UiField
    Styles style;

    @UiField
    FlowPanel shortcutContainer;

    Label cancelLabel, shortcutLabel, toggleLabel;

    @Inject
    public AttentionKeyShortcutView(WebTransMessages messages) {
        setWidget(uiBinder.createAndBindUi(this));
        setStyleName("keyShortcutPanel");

        // TODO get these from KeyShortcutPresenter
        cancelLabel = new Label(messages.attentionKeyCancelLabel());
        cancelLabel.setStyleName(style.shortcutHint());
        shortcutLabel = new Label(messages.attentionKeyCopySourceLabel());
        shortcutLabel.setStyleName(style.shortcutHint());
        toggleLabel =
                new Label(messages.attentionKeyToggleSyntaxHighlightingLabel());
        toggleLabel.setStyleName(style.shortcutHint());
        shortcutContainer.add(cancelLabel);
        shortcutContainer.add(shortcutLabel);
        shortcutContainer.add(toggleLabel);
    }

    @Override
    public Widget asWidget() {
        return this;
    }

    @Override
    public void showOrHide(boolean show) {
        if (show) {
            positionAndShow();
        } else {
            // TODO fade out, or provide a separate method to fade out
            hide();
        }
    }

    private void positionAndShow() {
        this.setPopupPositionAndShow(new PositionCallback() {
            @Override
            public void setPosition(int offsetWidth, int offsetHeight) {
                int left = (Window.getClientWidth() - offsetWidth) / 2;
                setThisPosition(left, 62);
            }
        });
    }

    private void setThisPosition(int left, int top) {
        this.setPopupPosition(left, top);
    }

    @Override
    public void clearShortcuts() {
        shortcutContainer.clear();
    }
}
