package org.zanata.webhook.events;

import java.util.Date;

import org.codehaus.jackson.annotate.JsonPropertyOrder;
import org.zanata.events.WebhookEventType;
import org.zanata.model.ProjectRole;
import org.zanata.model.type.WebhookType;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

/**
 * Event for when a source document is added or removed
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
@Getter
@Setter
@JsonPropertyOrder({"project", "version", "docId", "changeType"})
@AllArgsConstructor
@EqualsAndHashCode
public class SourceDocumentChangedEvent extends WebhookEventType {
    private static final String EVENT_TYPE =
            WebhookType.SourceDocumentChangedEvent.name();

    public static enum ChangeType {
        ADD,
        REMOVE
    }

    /**
     * Target project slug.
     * {@link org.zanata.model.HProject#slug}
     */
    private final String project;

    /**
     * Target version slug.
     * {@link org.zanata.model.HProjectIteration#slug}
     */
    private final String version;

    /**
     * Document id
     */
    private final String docId;

    /**
     * Change type
     */
    private final ChangeType changeType;

    @Override
    public String getType() {
        return EVENT_TYPE;
    }
}
