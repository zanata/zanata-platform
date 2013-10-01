package org.zanata.webtrans.client.ui;

import org.zanata.webtrans.client.resources.Resources;
import org.zanata.webtrans.client.resources.WebTransMessages;
import org.zanata.webtrans.client.view.DocumentListDisplay;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PushButton;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
 *
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 *
 */
public class DownloadFilesConfirmationBox extends DialogBox {

    private final WebTransMessages messages;
    private final HorizontalPanel infoPanel;
    private final HorizontalPanel progressPanel;
    private final Label progressMessage;
    private final Anchor downloadLink;
    private final Image progressImage;

    private final HorizontalPanel buttonPanel;
    private final PushButton cancelButton;
    private final PushButton okButton;

    public DownloadFilesConfirmationBox(boolean autoHide,
            WebTransMessages messages, final Resources resources) {
        super(autoHide);
        this.messages = messages;
        setText(messages.downloadAllFiles());
        setGlassEnabled(true);
        setStyleName("gwt-DialogBox-NoFixedSize");

        VerticalPanel panel = new VerticalPanel();

        Label infoMessage = new Label(messages.prepareDownloadConfirmation());
        InlineLabel infoImg = new InlineLabel();
        infoImg.setStyleName("icon-help-circle");

        infoPanel = new HorizontalPanel();
        infoPanel.setStyleName("info");
        infoPanel.add(infoImg);
        infoPanel.add(infoMessage);
        infoPanel.setCellVerticalAlignment(infoMessage,
                HasVerticalAlignment.ALIGN_MIDDLE);

        cancelButton = new PushButton(messages.cancel());
        cancelButton.addStyleName("button");

        okButton = new PushButton(messages.ok());
        okButton.addStyleName("button");

        buttonPanel = new HorizontalPanel();
        buttonPanel.setStyleName("buttonPanel");
        buttonPanel.add(cancelButton);
        buttonPanel.add(okButton);

        progressImage = new Image(resources.progressLoading());
        progressMessage = new Label();

        progressPanel = new HorizontalPanel();
        progressPanel.setSpacing(5);
        progressPanel.setStyleName("progress");
        progressPanel.setVisible(false);
        showDownloadLink(false);

        panel.add(infoPanel);
        panel.add(progressPanel);
        panel.add(buttonPanel);

        panel.setCellHorizontalAlignment(infoPanel,
                HasHorizontalAlignment.ALIGN_CENTER);
        panel.setCellHorizontalAlignment(progressPanel,
                HasHorizontalAlignment.ALIGN_CENTER);
        panel.setCellHorizontalAlignment(buttonPanel,
                HasHorizontalAlignment.ALIGN_RIGHT);

        add(panel);

        downloadLink = new Anchor("Click here to download");
        downloadLink.setTarget("_blank");
    }

    public void registerHandler(final DocumentListDisplay.Listener listener) {
        cancelButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                listener.cancelDownloadAllFiles();
            }
        });

        okButton.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                okButton.setVisible(false);
                listener.downloadAllFiles();
            }
        });
    }

    public void showDownloadLink(boolean show) {
        progressPanel.clear();
        if (show) {
            progressPanel.add(downloadLink);
            cancelButton.setText(messages.close());
        } else {
            progressPanel.add(progressImage);
            progressPanel.add(progressMessage);

            progressPanel.setCellVerticalAlignment(progressImage,
                    HasVerticalAlignment.ALIGN_MIDDLE);
            progressPanel.setCellVerticalAlignment(progressMessage,
                    HasVerticalAlignment.ALIGN_MIDDLE);
        }
    }

    public void setProgressMessage(String text) {
        progressMessage.setText(text);
    }

    public void setInProgress(boolean inProgress) {
        progressPanel.setVisible(inProgress);
    }

    @Override
    public void show() {
        okButton.setVisible(true);
        cancelButton.setText(messages.cancel());
        super.show();
    }

    public void hide() {
        setInProgress(false);
        showDownloadLink(false);
        super.hide();
    }

    public void setDownloadLink(String url) {
        downloadLink.setHref(url);
    }
}
