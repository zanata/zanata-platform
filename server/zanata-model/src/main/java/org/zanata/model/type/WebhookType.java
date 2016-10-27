package org.zanata.model.type;

/**
 * Type of Webhook event. See {@link org.zanata.model.WebHook} for usage.
 *
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
public enum WebhookType {
    DocumentMilestoneEvent,
    DocumentStatsEvent;
}
