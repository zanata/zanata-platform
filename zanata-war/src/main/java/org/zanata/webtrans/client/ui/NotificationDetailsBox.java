package org.zanata.webtrans.client.ui;

import org.zanata.webtrans.client.events.NotificationEvent;
import org.zanata.webtrans.client.events.NotificationEvent.Severity;
import org.zanata.webtrans.client.keys.ShortcutContext;
import org.zanata.webtrans.client.presenter.KeyShortcutPresenter;
import org.zanata.webtrans.client.resources.WebTransMessages;
import org.zanata.webtrans.client.util.DateUtil;

import com.google.common.base.Strings;
import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.inject.Inject;

/**
 *
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 *
 */
public class NotificationDetailsBox extends ShortcutContextAwareDialogBox {

    private static NotificationDetailsBoxUiBinder uiBinder = GWT
            .create(NotificationDetailsBoxUiBinder.class);

    @UiField(provided = true)
    DialogBoxCloseButton closeButton;

    @UiField
    HTMLPanel messageWrapper, message, details;

    private final WebTransMessages messages;

    @Inject
    public NotificationDetailsBox(WebTransMessages messages,
            KeyShortcutPresenter keyShortcutPresenter) {

        super(true, false, ShortcutContext.NotificationDetailsPopup,
                keyShortcutPresenter);

        this.messages = messages;

        closeButton = new DialogBoxCloseButton(this);
        HTMLPanel container = uiBinder.createAndBindUi(this);

        setGlassEnabled(true);
        getCaption().setText(messages.notification());

        setWidget(container);
    }

    public void setMessage(NotificationEvent notificationEvent) {
        String notificationMessage = notificationEvent.getMessage();
        String notificationDetails = notificationEvent.getDetails();

        getCaption().setText(
                messages.notification()
                        + " - "
                        + DateUtil.formatLongDateTime(notificationEvent
                                .getDate()));
        messageWrapper.setStyleName(getSeverityClass(notificationEvent
                .getSeverity()));

        if (notificationEvent.isDisplayAsHtml()) {
            SafeHtmlBuilder builder =
                    new SafeHtmlBuilder()
                            .appendHtmlConstant(notificationMessage);
            message.getElement().setInnerHTML(builder.toSafeHtml().asString());
        } else {
            message.getElement().setInnerHTML(notificationMessage);
        }

        if (notificationEvent.isDisplayAsHtml()) {
            if (!Strings.isNullOrEmpty(notificationDetails)) {
                SafeHtmlBuilder builder =
                        new SafeHtmlBuilder()
                                .appendHtmlConstant(notificationDetails);
                details.getElement().setInnerHTML(
                        builder.toSafeHtml().asString());
                details.setVisible(true);
            } else {
                details.setVisible(false);
            }
        } else {
            if (!Strings.isNullOrEmpty(notificationDetails)) {
                details.getElement().setInnerHTML(notificationDetails);
                details.setVisible(true);
            } else {
                details.setVisible(false);
            }
        }
    }

    private String getSeverityClass(Severity severity) {
        if (severity == Severity.Warning) {
            return "message--warning l__wrapper l--pad-all-half";
        } else if (severity == Severity.Error) {
            return "message--danger l__wrapper l--pad-all-half";
        }
        return "message--highlight l__wrapper l--pad-all-half";
    }

    interface NotificationDetailsBoxUiBinder extends
            UiBinder<HTMLPanel, NotificationDetailsBox> {
    }
}
