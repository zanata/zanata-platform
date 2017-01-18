package org.zanata.service;

import org.zanata.common.LocaleId;

import java.io.Serializable;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
public class VersionLocaleKey implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long projectIterationId;

    private LocaleId localeId;

    @java.beans.ConstructorProperties({ "projectIterationId", "localeId" })
    public VersionLocaleKey(Long projectIterationId, LocaleId localeId) {
        this.projectIterationId = projectIterationId;
        this.localeId = localeId;
    }

    public boolean equals(Object o) {
        if (o == this) return true;
        if (!(o instanceof VersionLocaleKey)) return false;
        final VersionLocaleKey other = (VersionLocaleKey) o;
        if (!other.canEqual((Object) this)) return false;
        final Object this$projectIterationId = this.projectIterationId;
        final Object other$projectIterationId = other.projectIterationId;
        if (this$projectIterationId == null ? other$projectIterationId != null :
                !this$projectIterationId.equals(other$projectIterationId))
            return false;
        final Object this$localeId = this.localeId;
        final Object other$localeId = other.localeId;
        if (this$localeId == null ? other$localeId != null :
                !this$localeId.equals(other$localeId)) return false;
        return true;
    }

    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final Object $projectIterationId = this.projectIterationId;
        result = result * PRIME + ($projectIterationId == null ? 43 :
                $projectIterationId.hashCode());
        final Object $localeId = this.localeId;
        result = result * PRIME +
                ($localeId == null ? 43 : $localeId.hashCode());
        return result;
    }

    protected boolean canEqual(Object other) {
        return other instanceof VersionLocaleKey;
    }

    public Long getProjectIterationId() {
        return this.projectIterationId;
    }

    public LocaleId getLocaleId() {
        return this.localeId;
    }
}
