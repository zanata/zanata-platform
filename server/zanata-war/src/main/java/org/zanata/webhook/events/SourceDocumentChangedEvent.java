package org.zanata.webhook.events;

import java.util.Date;
import org.codehaus.jackson.annotate.JsonPropertyOrder;
import org.zanata.events.WebhookEventType;
import org.zanata.model.ProjectRole;
import org.zanata.model.type.WebhookType;

/**
 * Event for when a source document is added or removed
 *
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
@JsonPropertyOrder({ "project", "version", "docId", "changeType" })
public class SourceDocumentChangedEvent extends WebhookEventType {
    private static final String EVENT_TYPE =
            WebhookType.SourceDocumentChangedEvent.name();

    public static enum ChangeType {
        ADD,
        REMOVE;

    }

    /**
     * Target project slug. {@link org.zanata.model.HProject#slug}
     */
    private final String project;

    /**
     * Target version slug. {@link org.zanata.model.HProjectIteration#slug}
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

    /**
     * Target project slug. {@link org.zanata.model.HProject#slug}
     */
    public String getProject() {
        return this.project;
    }

    /**
     * Target version slug. {@link org.zanata.model.HProjectIteration#slug}
     */
    public String getVersion() {
        return this.version;
    }

    /**
     * Document id
     */
    public String getDocId() {
        return this.docId;
    }

    /**
     * Change type
     */
    public ChangeType getChangeType() {
        return this.changeType;
    }

    @java.beans.ConstructorProperties({ "project", "version", "docId",
            "changeType" })
    public SourceDocumentChangedEvent(final String project,
            final String version, final String docId,
            final ChangeType changeType) {
        this.project = project;
        this.version = version;
        this.docId = docId;
        this.changeType = changeType;
    }

    @Override
    public boolean equals(final Object o) {
        if (o == this)
            return true;
        if (!(o instanceof SourceDocumentChangedEvent))
            return false;
        final SourceDocumentChangedEvent other = (SourceDocumentChangedEvent) o;
        if (!other.canEqual((Object) this))
            return false;
        final Object this$project = this.getProject();
        final Object other$project = other.getProject();
        if (this$project == null ? other$project != null
                : !this$project.equals(other$project))
            return false;
        final Object this$version = this.getVersion();
        final Object other$version = other.getVersion();
        if (this$version == null ? other$version != null
                : !this$version.equals(other$version))
            return false;
        final Object this$docId = this.getDocId();
        final Object other$docId = other.getDocId();
        if (this$docId == null ? other$docId != null
                : !this$docId.equals(other$docId))
            return false;
        final Object this$changeType = this.getChangeType();
        final Object other$changeType = other.getChangeType();
        if (this$changeType == null ? other$changeType != null
                : !this$changeType.equals(other$changeType))
            return false;
        return true;
    }

    protected boolean canEqual(final Object other) {
        return other instanceof SourceDocumentChangedEvent;
    }

    @Override
    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final Object $project = this.getProject();
        result = result * PRIME + ($project == null ? 43 : $project.hashCode());
        final Object $version = this.getVersion();
        result = result * PRIME + ($version == null ? 43 : $version.hashCode());
        final Object $docId = this.getDocId();
        result = result * PRIME + ($docId == null ? 43 : $docId.hashCode());
        final Object $changeType = this.getChangeType();
        result = result * PRIME
                + ($changeType == null ? 43 : $changeType.hashCode());
        return result;
    }
}
