package org.zanata.model.type;

import java.io.Serializable;

/**
 * Type of Webhook event. See {@link org.zanata.model.WebHook} for usage.
 *
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
public enum WebhookType implements Serializable {
    DocumentMilestoneEvent("Translation milestone"),
    DocumentStatsEvent("Translation update"),
    VersionChangedEvent("Project version"),
    ProjectMaintainerChangedEvent("Project maintainer update"),
    SourceDocumentChangedEvent("Document"),
    ManuallyTriggeredEvent("Manual event");
    private String displayName;

    WebhookType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return this.displayName;
    }
}
