package org.zanata.webtrans.client.ui;

import org.zanata.webtrans.client.resources.WebTransMessages;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.PushButton;

/**
 * @author Patrick Huang <a
 *         href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
public class DialogBoxCloseButton extends PushButton {
    private static final WebTransMessages MESSAGES = GWT
            .create(WebTransMessages.class);

    public DialogBoxCloseButton(final DialogBox dialogBox) {
        super(MESSAGES.close());
        addStyleName("gwt-DialogBox-CloseButton");
        addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                dialogBox.hide();
            }
        });
    }
}
