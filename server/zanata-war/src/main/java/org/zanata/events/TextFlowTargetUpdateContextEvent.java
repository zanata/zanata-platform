package org.zanata.events;

import org.zanata.common.LocaleId;
import org.zanata.webtrans.shared.auth.EditorClientId;
import org.zanata.webtrans.shared.model.TransUnitId;
import org.zanata.webtrans.shared.rpc.TransUnitUpdated;

/**
 * This event is raised by text flow target update initiator before making a
 * change. Hibernate entity listener will be triggered after the change is made
 * and at that point this context information is retrieved from cache.
 *
 * @see org.zanata.webtrans.server.TranslationUpdateListener
 *
 * @author Patrick Huang
 *         <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
public final class TextFlowTargetUpdateContextEvent {
    private final TransUnitId transUnitId;
    private final LocaleId localeId;
    private final EditorClientId editorClientId;
    private final TransUnitUpdated.UpdateType updateType;

    @java.beans.ConstructorProperties({ "transUnitId", "localeId",
            "editorClientId", "updateType" })
    public TextFlowTargetUpdateContextEvent(final TransUnitId transUnitId,
            final LocaleId localeId, final EditorClientId editorClientId,
            final TransUnitUpdated.UpdateType updateType) {
        this.transUnitId = transUnitId;
        this.localeId = localeId;
        this.editorClientId = editorClientId;
        this.updateType = updateType;
    }

    public TransUnitId getTransUnitId() {
        return this.transUnitId;
    }

    public LocaleId getLocaleId() {
        return this.localeId;
    }

    public EditorClientId getEditorClientId() {
        return this.editorClientId;
    }

    public TransUnitUpdated.UpdateType getUpdateType() {
        return this.updateType;
    }

    @Override
    public boolean equals(final Object o) {
        if (o == this)
            return true;
        if (!(o instanceof TextFlowTargetUpdateContextEvent))
            return false;
        final TextFlowTargetUpdateContextEvent other =
                (TextFlowTargetUpdateContextEvent) o;
        final Object this$transUnitId = this.getTransUnitId();
        final Object other$transUnitId = other.getTransUnitId();
        if (this$transUnitId == null ? other$transUnitId != null
                : !this$transUnitId.equals(other$transUnitId))
            return false;
        final Object this$localeId = this.getLocaleId();
        final Object other$localeId = other.getLocaleId();
        if (this$localeId == null ? other$localeId != null
                : !this$localeId.equals(other$localeId))
            return false;
        final Object this$editorClientId = this.getEditorClientId();
        final Object other$editorClientId = other.getEditorClientId();
        if (this$editorClientId == null ? other$editorClientId != null
                : !this$editorClientId.equals(other$editorClientId))
            return false;
        final Object this$updateType = this.getUpdateType();
        final Object other$updateType = other.getUpdateType();
        if (this$updateType == null ? other$updateType != null
                : !this$updateType.equals(other$updateType))
            return false;
        return true;
    }

    @Override
    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final Object $transUnitId = this.getTransUnitId();
        result = result * PRIME
                + ($transUnitId == null ? 43 : $transUnitId.hashCode());
        final Object $localeId = this.getLocaleId();
        result = result * PRIME
                + ($localeId == null ? 43 : $localeId.hashCode());
        final Object $editorClientId = this.getEditorClientId();
        result = result * PRIME
                + ($editorClientId == null ? 43 : $editorClientId.hashCode());
        final Object $updateType = this.getUpdateType();
        result = result * PRIME
                + ($updateType == null ? 43 : $updateType.hashCode());
        return result;
    }

    @Override
    public String toString() {
        return "TextFlowTargetUpdateContextEvent(transUnitId="
                + this.getTransUnitId() + ", localeId=" + this.getLocaleId()
                + ", editorClientId=" + this.getEditorClientId()
                + ", updateType=" + this.getUpdateType() + ")";
    }
}
