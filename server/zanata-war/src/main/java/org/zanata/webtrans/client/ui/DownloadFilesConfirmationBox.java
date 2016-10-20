package org.zanata.webtrans.client.ui;

import org.zanata.webtrans.client.resources.Resources;
import org.zanata.webtrans.client.resources.WebTransMessages;
import org.zanata.webtrans.client.view.DocumentListDisplay;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
 *
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 *
 */
public class DownloadFilesConfirmationBox extends DialogBox {

    private final WebTransMessages messages;
    private final UnorderedListWidget progressPanel;
    private final Label progressMessage;
    private final Anchor downloadLink;
    private final Image progressImage;

    private final UnorderedListWidget buttonPanel;
    private final Button cancelButton;
    private final Button okButton;

    public DownloadFilesConfirmationBox(boolean autoHide,
            WebTransMessages messages, final Resources resources) {
        super(autoHide);
        this.messages = messages;
        setText(messages.downloadAllFiles());
        setGlassEnabled(true);
        setStyleName("gwt-DialogBox");

        VerticalPanel panel = new VerticalPanel();
        panel.setStyleName("new-zanata");

        UnorderedListWidget infoPanel = new UnorderedListWidget();
        infoPanel.setStyleName("list--horizontal");

        ListItemWidget infoImg = new ListItemWidget();
        infoImg.setStyleName("i i--info txt--highlight");

        infoPanel.add(infoImg);
        infoPanel
                .add(new ListItemWidget(messages.prepareDownloadConfirmation()));

        cancelButton = new Button(messages.cancel());
        okButton = new Button(messages.ok());

        buttonPanel = new UnorderedListWidget();
        buttonPanel.setStyleName("list--horizontal l--float-right");
        buttonPanel.add(new ListItemWidget(cancelButton));
        buttonPanel.add(new ListItemWidget(okButton));

        progressImage = new Image(resources.progressLoading());
        progressMessage = new Label();

        progressPanel = new UnorderedListWidget();
        progressPanel.setStyleName("list--horizontal");
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
            progressPanel.add(new ListItemWidget(downloadLink));
            cancelButton.setText(messages.close());
        } else {
            progressPanel.add(new ListItemWidget(progressImage));
            progressPanel.add(new ListItemWidget(progressMessage));
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
