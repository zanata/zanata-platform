package org.zanata.webhook.events;

import java.util.Date;
import org.codehaus.jackson.annotate.JsonPropertyOrder;
import org.zanata.events.WebhookEventType;
import org.zanata.model.ProjectRole;
import org.zanata.model.type.WebhookType;

/**
 * Event for when a project maintainer is added or removed
 *
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
@JsonPropertyOrder({ "project", "username", "changeType", "role" })
public class ProjectMaintainerChangedEvent extends WebhookEventType {
    private static final String EVENT_TYPE =
            WebhookType.ProjectMaintainerChangedEvent.name();

    public static enum ChangeType {
        ADD,
        REMOVE;

    }

    /**
     * Target project slug. {@link org.zanata.model.HProject#slug}
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

    /**
     * Target project slug. {@link org.zanata.model.HProject#slug}
     */
    public String getProject() {
        return this.project;
    }

    /**
     * Username of the maintainer
     */
    public String getUsername() {
        return this.username;
    }

    /**
     * Changed role /
     */
    public ProjectRole getRole() {
        return this.role;
    }

    /**
     * Change type
     */
    public ChangeType getChangeType() {
        return this.changeType;
    }

    @java.beans.ConstructorProperties({ "project", "username", "role",
            "changeType" })
    public ProjectMaintainerChangedEvent(final String project,
            final String username, final ProjectRole role,
            final ChangeType changeType) {
        this.project = project;
        this.username = username;
        this.role = role;
        this.changeType = changeType;
    }

    @Override
    public boolean equals(final Object o) {
        if (o == this)
            return true;
        if (!(o instanceof ProjectMaintainerChangedEvent))
            return false;
        final ProjectMaintainerChangedEvent other =
                (ProjectMaintainerChangedEvent) o;
        if (!other.canEqual((Object) this))
            return false;
        final Object this$project = this.getProject();
        final Object other$project = other.getProject();
        if (this$project == null ? other$project != null
                : !this$project.equals(other$project))
            return false;
        final Object this$username = this.getUsername();
        final Object other$username = other.getUsername();
        if (this$username == null ? other$username != null
                : !this$username.equals(other$username))
            return false;
        final Object this$role = this.getRole();
        final Object other$role = other.getRole();
        if (this$role == null ? other$role != null
                : !this$role.equals(other$role))
            return false;
        final Object this$changeType = this.getChangeType();
        final Object other$changeType = other.getChangeType();
        if (this$changeType == null ? other$changeType != null
                : !this$changeType.equals(other$changeType))
            return false;
        return true;
    }

    protected boolean canEqual(final Object other) {
        return other instanceof ProjectMaintainerChangedEvent;
    }

    @Override
    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final Object $project = this.getProject();
        result = result * PRIME + ($project == null ? 43 : $project.hashCode());
        final Object $username = this.getUsername();
        result = result * PRIME
                + ($username == null ? 43 : $username.hashCode());
        final Object $role = this.getRole();
        result = result * PRIME + ($role == null ? 43 : $role.hashCode());
        final Object $changeType = this.getChangeType();
        result = result * PRIME
                + ($changeType == null ? 43 : $changeType.hashCode());
        return result;
    }
}
