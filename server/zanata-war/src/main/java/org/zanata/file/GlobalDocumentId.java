package org.zanata.file;

import edu.umd.cs.findbugs.annotations.NonNull;

public class GlobalDocumentId {
    @NonNull
    private final String projectSlug;
    @NonNull
    private final String versionSlug;
    @NonNull
    private final String docId;

    @java.beans.ConstructorProperties({ "projectSlug", "versionSlug", "docId" })
    public GlobalDocumentId(String projectSlug, String versionSlug,
            String docId) {
        this.projectSlug = projectSlug;
        this.versionSlug = versionSlug;
        this.docId = docId;
    }

    @Override
    public String toString() {
        return projectSlug + ":" + versionSlug + ":" + docId;
    }

    @NonNull
    public String getProjectSlug() {
        return this.projectSlug;
    }

    @NonNull
    public String getVersionSlug() {
        return this.versionSlug;
    }

    @NonNull
    public String getDocId() {
        return this.docId;
    }

    public boolean equals(Object o) {
        if (o == this) return true;
        if (!(o instanceof GlobalDocumentId)) return false;
        final GlobalDocumentId other = (GlobalDocumentId) o;
        if (!other.canEqual((Object) this)) return false;
        final Object this$projectSlug = this.getProjectSlug();
        final Object other$projectSlug = other.getProjectSlug();
        if (this$projectSlug == null ? other$projectSlug != null :
                !this$projectSlug.equals(other$projectSlug)) return false;
        final Object this$versionSlug = this.getVersionSlug();
        final Object other$versionSlug = other.getVersionSlug();
        if (this$versionSlug == null ? other$versionSlug != null :
                !this$versionSlug.equals(other$versionSlug)) return false;
        final Object this$docId = this.getDocId();
        final Object other$docId = other.getDocId();
        if (this$docId == null ? other$docId != null :
                !this$docId.equals(other$docId)) return false;
        return true;
    }

    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final Object $projectSlug = this.getProjectSlug();
        result = result * PRIME +
                ($projectSlug == null ? 43 : $projectSlug.hashCode());
        final Object $versionSlug = this.getVersionSlug();
        result = result * PRIME +
                ($versionSlug == null ? 43 : $versionSlug.hashCode());
        final Object $docId = this.getDocId();
        result = result * PRIME + ($docId == null ? 43 : $docId.hashCode());
        return result;
    }

    protected boolean canEqual(Object other) {
        return other instanceof GlobalDocumentId;
    }
}
