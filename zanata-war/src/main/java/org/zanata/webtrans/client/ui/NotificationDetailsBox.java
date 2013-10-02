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
    HTMLPanel detailMessages;

    @UiField
    HTMLPanel summary;

    private final WebTransMessages messages;

    @Inject
    public NotificationDetailsBox(WebTransMessages messages,
            KeyShortcutPresenter keyShortcutPresenter) {

        super(true, false, ShortcutContext.TransHistoryPopup,
                keyShortcutPresenter);

        this.messages = messages;

        closeButton = new DialogBoxCloseButton(this);
        HTMLPanel container = uiBinder.createAndBindUi(this);

        setGlassEnabled(true);
        getCaption().setText(messages.notification());

        setWidget(container);
    }

    public void setMessageDetails(NotificationEvent notificationEvent) {

        Severity severity = notificationEvent.getSeverity();
        String message = notificationEvent.getMessage();
        String severityClass = getSeverityClass(severity);

        getCaption().setText(
                messages.notification()
                        + " - "
                        + DateUtil.formatShortDate(notificationEvent
                                .getDate()));
        this.summary.setStyleName(severityClass);
        this.summary.getElement().setInnerHTML(notificationEvent.getSummary());

        detailMessages.setStyleName(severityClass);
        if (!Strings.isNullOrEmpty(message)) {
            if (notificationEvent.isDisplayAsHtml()) {
                SafeHtmlBuilder builder =
                        new SafeHtmlBuilder().appendHtmlConstant(message);
                detailMessages.getElement().setInnerHTML(
                        builder.toSafeHtml().asString());
            } else {
                detailMessages.getElement().setInnerHTML(message);
            }
        }
    }

    private String getSeverityClass(Severity severity) {
        if (severity == Severity.Warning) {
            return "message--warning";
        } else if (severity == Severity.Error) {
            return "message--danger";
        }
        return "message--highlight";
    }

    interface NotificationDetailsBoxUiBinder extends
            UiBinder<HTMLPanel, NotificationDetailsBox> {
    }
}
