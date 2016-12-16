package org.zanata.webtrans.client.events;

import java.util.Date;

import org.zanata.webtrans.client.ui.InlineLink;

import com.google.gwt.event.shared.GwtEvent;

public class NotificationEvent extends GwtEvent<NotificationEventHandler> {

    /**
     * Handler type.
     */
    private static final Type<NotificationEventHandler> TYPE = new Type<>();

    /**
     * Gets the type associated with this event.
     *
     * @return returns the handler type
     */
    public static Type<NotificationEventHandler> getType() {
        return TYPE;
    }

    public static enum Severity {
        Warning, Error, Info
    }

    private final Severity severity;
    private final String message;
    private final String details;
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

    public NotificationEvent(Severity severity, String message, String details,
            boolean displayAsHtml, InlineLink inlineLink) {
        this.severity = severity;
        this.message = message;
        this.inlineLink = inlineLink;
        this.details = details;
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

    public String getDetails() {
        return details;
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
