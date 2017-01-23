package org.zanata.service;

import java.io.Serializable;
import org.zanata.common.LocaleId;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
public class VersionLocaleKey implements Serializable {
    private static final long serialVersionUID = 1L;
    private Long projectIterationId;
    private LocaleId localeId;

    @java.beans.ConstructorProperties({ "projectIterationId", "localeId" })
    public VersionLocaleKey(final Long projectIterationId,
            final LocaleId localeId) {
        this.projectIterationId = projectIterationId;
        this.localeId = localeId;
    }

    @Override
    public boolean equals(final Object o) {
        if (o == this)
            return true;
        if (!(o instanceof VersionLocaleKey))
            return false;
        final VersionLocaleKey other = (VersionLocaleKey) o;
        if (!other.canEqual((Object) this))
            return false;
        final Object this$projectIterationId = this.getProjectIterationId();
        final Object other$projectIterationId = other.getProjectIterationId();
        if (this$projectIterationId == null ? other$projectIterationId != null
                : !this$projectIterationId.equals(other$projectIterationId))
            return false;
        final Object this$localeId = this.getLocaleId();
        final Object other$localeId = other.getLocaleId();
        if (this$localeId == null ? other$localeId != null
                : !this$localeId.equals(other$localeId))
            return false;
        return true;
    }

    protected boolean canEqual(final Object other) {
        return other instanceof VersionLocaleKey;
    }

    @Override
    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final Object $projectIterationId = this.getProjectIterationId();
        result = result * PRIME + ($projectIterationId == null ? 43
                : $projectIterationId.hashCode());
        final Object $localeId = this.getLocaleId();
        result = result * PRIME
                + ($localeId == null ? 43 : $localeId.hashCode());
        return result;
    }

    public Long getProjectIterationId() {
        return this.projectIterationId;
    }

    public LocaleId getLocaleId() {
        return this.localeId;
    }
}
