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
 * Event for when a project maintainer is added or removed
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
@Getter
@Setter
@JsonPropertyOrder({"project", "username", "changeType", "role"})
@AllArgsConstructor
@EqualsAndHashCode
public class ProjectMaintainerChangedEvent extends WebhookEventType {
    private static final String EVENT_TYPE =
            WebhookType.ProjectMaintainerChangedEvent.name();

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
     * Username of the maintainer
     */
    private final String username;

    /**
     * Changed role /
     */
    private final ProjectRole role;

    /**
     * Change type
     */
    private final ChangeType changeType;

    @Override
    public String getType() {
        return EVENT_TYPE;
    }
}
