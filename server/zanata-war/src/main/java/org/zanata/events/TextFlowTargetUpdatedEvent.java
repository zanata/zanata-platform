package org.zanata.events;

import org.zanata.webtrans.server.TranslationWorkspace;
import org.zanata.webtrans.shared.rpc.TransUnitUpdated;

/**
 * This event is raised in Hibernate entity listener after change has been made.
 *
 * We then gather all relevant information required for TransUnitUpdated object
 * creation.
 *
 * @see org.zanata.webtrans.server.HibernateIntegrator
 * @see org.zanata.webtrans.shared.rpc.TransUnitUpdated
 * @see org.zanata.webtrans.server.rpc.TransUnitUpdateHelper
 * @author Patrick Huang
 *         <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
public final class TextFlowTargetUpdatedEvent {
    private final TranslationWorkspace workspace;
    private final Long textFlowTargetId;
    private final TransUnitUpdated transUnitUpdated;

    @java.beans.ConstructorProperties({ "workspace", "textFlowTargetId",
            "transUnitUpdated" })
    public TextFlowTargetUpdatedEvent(final TranslationWorkspace workspace,
            final Long textFlowTargetId,
            final TransUnitUpdated transUnitUpdated) {
        this.workspace = workspace;
        this.textFlowTargetId = textFlowTargetId;
        this.transUnitUpdated = transUnitUpdated;
    }

    public TranslationWorkspace getWorkspace() {
        return this.workspace;
    }

    public Long getTextFlowTargetId() {
        return this.textFlowTargetId;
    }

    public TransUnitUpdated getTransUnitUpdated() {
        return this.transUnitUpdated;
    }

    @Override
    public boolean equals(final Object o) {
        if (o == this)
            return true;
        if (!(o instanceof TextFlowTargetUpdatedEvent))
            return false;
        final TextFlowTargetUpdatedEvent other = (TextFlowTargetUpdatedEvent) o;
        final Object this$workspace = this.getWorkspace();
        final Object other$workspace = other.getWorkspace();
        if (this$workspace == null ? other$workspace != null
                : !this$workspace.equals(other$workspace))
            return false;
        final Object this$textFlowTargetId = this.getTextFlowTargetId();
        final Object other$textFlowTargetId = other.getTextFlowTargetId();
        if (this$textFlowTargetId == null ? other$textFlowTargetId != null
                : !this$textFlowTargetId.equals(other$textFlowTargetId))
            return false;
        final Object this$transUnitUpdated = this.getTransUnitUpdated();
        final Object other$transUnitUpdated = other.getTransUnitUpdated();
        if (this$transUnitUpdated == null ? other$transUnitUpdated != null
                : !this$transUnitUpdated.equals(other$transUnitUpdated))
            return false;
        return true;
    }

    @Override
    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final Object $workspace = this.getWorkspace();
        result = result * PRIME
                + ($workspace == null ? 43 : $workspace.hashCode());
        final Object $textFlowTargetId = this.getTextFlowTargetId();
        result = result * PRIME + ($textFlowTargetId == null ? 43
                : $textFlowTargetId.hashCode());
        final Object $transUnitUpdated = this.getTransUnitUpdated();
        result = result * PRIME + ($transUnitUpdated == null ? 43
                : $transUnitUpdated.hashCode());
        return result;
    }

    @Override
    public String toString() {
        return "TextFlowTargetUpdatedEvent(workspace=" + this.getWorkspace()
                + ", textFlowTargetId=" + this.getTextFlowTargetId()
                + ", transUnitUpdated=" + this.getTransUnitUpdated() + ")";
    }
}
