package org.zanata.rest.dto;

import org.zanata.common.ContentState;
import org.zanata.common.LocaleId;

/**
 * @author Patrick Huang
 *         <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
public class TranslationMatrix {
    private String savedDate;
    private String projectSlug;
    private String projectName;
    private String versionSlug;
    private LocaleId localeId;
    private String localeDisplayName;
    private ContentState savedState;
    private long wordCount;

    public String getSavedDate() {
        return this.savedDate;
    }

    public String getProjectSlug() {
        return this.projectSlug;
    }

    public String getProjectName() {
        return this.projectName;
    }

    public String getVersionSlug() {
        return this.versionSlug;
    }

    public LocaleId getLocaleId() {
        return this.localeId;
    }

    public String getLocaleDisplayName() {
        return this.localeDisplayName;
    }

    public ContentState getSavedState() {
        return this.savedState;
    }

    public long getWordCount() {
        return this.wordCount;
    }

    public void setSavedDate(final String savedDate) {
        this.savedDate = savedDate;
    }

    public void setProjectSlug(final String projectSlug) {
        this.projectSlug = projectSlug;
    }

    public void setProjectName(final String projectName) {
        this.projectName = projectName;
    }

    public void setVersionSlug(final String versionSlug) {
        this.versionSlug = versionSlug;
    }

    public void setLocaleId(final LocaleId localeId) {
        this.localeId = localeId;
    }

    public void setLocaleDisplayName(final String localeDisplayName) {
        this.localeDisplayName = localeDisplayName;
    }

    public void setSavedState(final ContentState savedState) {
        this.savedState = savedState;
    }

    public void setWordCount(final long wordCount) {
        this.wordCount = wordCount;
    }

    @Override
    public boolean equals(final Object o) {
        if (o == this)
            return true;
        if (!(o instanceof TranslationMatrix))
            return false;
        final TranslationMatrix other = (TranslationMatrix) o;
        if (!other.canEqual((Object) this))
            return false;
        final Object this$savedDate = this.getSavedDate();
        final Object other$savedDate = other.getSavedDate();
        if (this$savedDate == null ? other$savedDate != null
                : !this$savedDate.equals(other$savedDate))
            return false;
        final Object this$projectSlug = this.getProjectSlug();
        final Object other$projectSlug = other.getProjectSlug();
        if (this$projectSlug == null ? other$projectSlug != null
                : !this$projectSlug.equals(other$projectSlug))
            return false;
        final Object this$projectName = this.getProjectName();
        final Object other$projectName = other.getProjectName();
        if (this$projectName == null ? other$projectName != null
                : !this$projectName.equals(other$projectName))
            return false;
        final Object this$versionSlug = this.getVersionSlug();
        final Object other$versionSlug = other.getVersionSlug();
        if (this$versionSlug == null ? other$versionSlug != null
                : !this$versionSlug.equals(other$versionSlug))
            return false;
        final Object this$localeId = this.getLocaleId();
        final Object other$localeId = other.getLocaleId();
        if (this$localeId == null ? other$localeId != null
                : !this$localeId.equals(other$localeId))
            return false;
        final Object this$localeDisplayName = this.getLocaleDisplayName();
        final Object other$localeDisplayName = other.getLocaleDisplayName();
        if (this$localeDisplayName == null ? other$localeDisplayName != null
                : !this$localeDisplayName.equals(other$localeDisplayName))
            return false;
        final Object this$savedState = this.getSavedState();
        final Object other$savedState = other.getSavedState();
        if (this$savedState == null ? other$savedState != null
                : !this$savedState.equals(other$savedState))
            return false;
        if (this.getWordCount() != other.getWordCount())
            return false;
        return true;
    }

    protected boolean canEqual(final Object other) {
        return other instanceof TranslationMatrix;
    }

    @Override
    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final Object $savedDate = this.getSavedDate();
        result = result * PRIME
                + ($savedDate == null ? 43 : $savedDate.hashCode());
        final Object $projectSlug = this.getProjectSlug();
        result = result * PRIME
                + ($projectSlug == null ? 43 : $projectSlug.hashCode());
        final Object $projectName = this.getProjectName();
        result = result * PRIME
                + ($projectName == null ? 43 : $projectName.hashCode());
        final Object $versionSlug = this.getVersionSlug();
        result = result * PRIME
                + ($versionSlug == null ? 43 : $versionSlug.hashCode());
        final Object $localeId = this.getLocaleId();
        result = result * PRIME
                + ($localeId == null ? 43 : $localeId.hashCode());
        final Object $localeDisplayName = this.getLocaleDisplayName();
        result = result * PRIME + ($localeDisplayName == null ? 43
                : $localeDisplayName.hashCode());
        final Object $savedState = this.getSavedState();
        result = result * PRIME
                + ($savedState == null ? 43 : $savedState.hashCode());
        final long $wordCount = this.getWordCount();
        result = result * PRIME + (int) ($wordCount >>> 32 ^ $wordCount);
        return result;
    }

    @Override
    public String toString() {
        return "TranslationMatrix(savedDate=" + this.getSavedDate()
                + ", projectSlug=" + this.getProjectSlug() + ", projectName="
                + this.getProjectName() + ", versionSlug="
                + this.getVersionSlug() + ", localeId=" + this.getLocaleId()
                + ", localeDisplayName=" + this.getLocaleDisplayName()
                + ", savedState=" + this.getSavedState() + ", wordCount="
                + this.getWordCount() + ")";
    }

    @java.beans.ConstructorProperties({ "savedDate", "projectSlug",
            "projectName", "versionSlug", "localeId", "localeDisplayName",
            "savedState", "wordCount" })
    public TranslationMatrix(final String savedDate, final String projectSlug,
            final String projectName, final String versionSlug,
            final LocaleId localeId, final String localeDisplayName,
            final ContentState savedState, final long wordCount) {
        this.savedDate = savedDate;
        this.projectSlug = projectSlug;
        this.projectName = projectName;
        this.versionSlug = versionSlug;
        this.localeId = localeId;
        this.localeDisplayName = localeDisplayName;
        this.savedState = savedState;
        this.wordCount = wordCount;
    }
}
