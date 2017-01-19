package org.zanata.file;

import javax.annotation.Nonnull;

public class GlobalDocumentId {
    @Nonnull
    private final String projectSlug;
    @Nonnull
    private final String versionSlug;
    @Nonnull
    private final String docId;

    @Override
    public String toString() {
        return projectSlug + ":" + versionSlug + ":" + docId;
    }

    @Nonnull
    public String getProjectSlug() {
        return this.projectSlug;
    }

    @Nonnull
    public String getVersionSlug() {
        return this.versionSlug;
    }

    @Nonnull
    public String getDocId() {
        return this.docId;
    }

    @java.beans.ConstructorProperties({ "projectSlug", "versionSlug", "docId" })
    public GlobalDocumentId(@Nonnull final String projectSlug,
            @Nonnull final String versionSlug, @Nonnull final String docId) {
        if (projectSlug == null) {
            throw new NullPointerException("projectSlug");
        }
        if (versionSlug == null) {
            throw new NullPointerException("versionSlug");
        }
        if (docId == null) {
            throw new NullPointerException("docId");
        }
        this.projectSlug = projectSlug;
        this.versionSlug = versionSlug;
        this.docId = docId;
    }

    @Override
    public boolean equals(final Object o) {
        if (o == this)
            return true;
        if (!(o instanceof GlobalDocumentId))
            return false;
        final GlobalDocumentId other = (GlobalDocumentId) o;
        if (!other.canEqual((Object) this))
            return false;
        final Object this$projectSlug = this.getProjectSlug();
        final Object other$projectSlug = other.getProjectSlug();
        if (this$projectSlug == null ? other$projectSlug != null
                : !this$projectSlug.equals(other$projectSlug))
            return false;
        final Object this$versionSlug = this.getVersionSlug();
        final Object other$versionSlug = other.getVersionSlug();
        if (this$versionSlug == null ? other$versionSlug != null
                : !this$versionSlug.equals(other$versionSlug))
            return false;
        final Object this$docId = this.getDocId();
        final Object other$docId = other.getDocId();
        if (this$docId == null ? other$docId != null
                : !this$docId.equals(other$docId))
            return false;
        return true;
    }

    protected boolean canEqual(final Object other) {
        return other instanceof GlobalDocumentId;
    }

    @Override
    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final Object $projectSlug = this.getProjectSlug();
        result = result * PRIME
                + ($projectSlug == null ? 43 : $projectSlug.hashCode());
        final Object $versionSlug = this.getVersionSlug();
        result = result * PRIME
                + ($versionSlug == null ? 43 : $versionSlug.hashCode());
        final Object $docId = this.getDocId();
        result = result * PRIME + ($docId == null ? 43 : $docId.hashCode());
        return result;
    }
}
