package org.zanata.webtrans.client.events;

import java.util.Date;

import org.zanata.webtrans.client.ui.InlineLink;

import com.google.gwt.event.shared.GwtEvent;

public class NotificationEvent extends GwtEvent<NotificationEventHandler> {

    /**
     * Handler type.
     */
    private static Type<NotificationEventHandler> TYPE;

    /**
     * Gets the type associated with this event.
     *
     * @return returns the handler type
     */
    public static Type<NotificationEventHandler> getType() {
        if (TYPE == null) {
            TYPE = new Type<NotificationEventHandler>();
        }
        return TYPE;
    }

    public static enum Severity {
        Warning, Error, Info
    }

    private final Severity severity;
    private final String message;
    private final String summary;
    private boolean displayAsHtml;
    private InlineLink inlineLink;
    private Date date;

    public NotificationEvent(Severity severity, String message) {
        this(severity, message, null);
    }

    public NotificationEvent(Severity severity, String message,
            InlineLink inlineLink) {
        this(severity, message, null, false, inlineLink);
    }

    public NotificationEvent(Severity severity, String summary, String message,
            boolean displayAsHtml, InlineLink inlineLink) {
        this.severity = severity;
        this.message = message;
        this.inlineLink = inlineLink;
        this.summary = summary;
        this.displayAsHtml = displayAsHtml;
        this.date = new Date();
    }

    public Severity getSeverity() {
        return severity;
    }

    public String getMessage() {
        return message;
    }

    public InlineLink getInlineLink() {
        return inlineLink;
    }

    public String getSummary() {
        return summary;
    }

    public boolean isDisplayAsHtml() {
        return displayAsHtml;
    }

    @Override
    protected void dispatch(NotificationEventHandler handler) {
        handler.onNotification(this);
    }

    @Override
    public Type<NotificationEventHandler> getAssociatedType() {
        return getType();
    }

    public Date getDate() {
        return date;
    }
}
