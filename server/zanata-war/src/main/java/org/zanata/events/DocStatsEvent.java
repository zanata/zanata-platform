package org.zanata.events;

import org.zanata.common.ContentState;

import javax.annotation.Nonnull;
import java.util.Map;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
public class DocStatsEvent {
    private final DocumentLocaleKey key;

    private final Long projectVersionId;

    /**
     * Updated content states with word counts
     */
    private final Map<ContentState, Long> wordDeltasByState;

    private final Long lastModifiedTargetId;

    @java.beans.ConstructorProperties({ "key", "projectVersionId",
            "wordDeltasByState", "lastModifiedTargetId" })
    public DocStatsEvent(DocumentLocaleKey key, Long projectVersionId,
            Map<ContentState, Long> wordDeltasByState,
            Long lastModifiedTargetId) {
        this.key = key;
        this.projectVersionId = projectVersionId;
        this.wordDeltasByState = wordDeltasByState;
        this.lastModifiedTargetId = lastModifiedTargetId;
    }

    public static void updateContentStateDeltas(
            @Nonnull Map<ContentState, Long> wordDeltasByState,
            ContentState newState, ContentState previousState, long wordCount) {

        long previousStateCount =
                wordDeltasByState.getOrDefault(previousState, 0L);
        previousStateCount -= wordCount;
        wordDeltasByState.put(previousState, previousStateCount);

        long newStateCount = wordDeltasByState.getOrDefault(newState, 0L);
        newStateCount += wordCount;
        wordDeltasByState.put(newState, newStateCount);
    }

    public DocumentLocaleKey getKey() {
        return this.key;
    }

    public Long getProjectVersionId() {
        return this.projectVersionId;
    }

    public Map<ContentState, Long> getWordDeltasByState() {
        return this.wordDeltasByState;
    }

    public Long getLastModifiedTargetId() {
        return this.lastModifiedTargetId;
    }

    public boolean equals(Object o) {
        if (o == this) return true;
        if (!(o instanceof DocStatsEvent)) return false;
        final DocStatsEvent other = (DocStatsEvent) o;
        if (!other.canEqual((Object) this)) return false;
        final Object this$key = this.getKey();
        final Object other$key = other.getKey();
        if (this$key == null ? other$key != null : !this$key.equals(other$key))
            return false;
        final Object this$projectVersionId = this.getProjectVersionId();
        final Object other$projectVersionId = other.getProjectVersionId();
        if (this$projectVersionId == null ? other$projectVersionId != null :
                !this$projectVersionId.equals(other$projectVersionId))
            return false;
        final Object this$wordDeltasByState = this.getWordDeltasByState();
        final Object other$wordDeltasByState = other.getWordDeltasByState();
        if (this$wordDeltasByState == null ? other$wordDeltasByState != null :
                !this$wordDeltasByState.equals(other$wordDeltasByState))
            return false;
        final Object this$lastModifiedTargetId = this.getLastModifiedTargetId();
        final Object other$lastModifiedTargetId =
                other.getLastModifiedTargetId();
        if (this$lastModifiedTargetId == null ?
                other$lastModifiedTargetId != null :
                !this$lastModifiedTargetId.equals(other$lastModifiedTargetId))
            return false;
        return true;
    }

    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final Object $key = this.getKey();
        result = result * PRIME + ($key == null ? 43 : $key.hashCode());
        final Object $projectVersionId = this.getProjectVersionId();
        result = result * PRIME +
                ($projectVersionId == null ? 43 : $projectVersionId.hashCode());
        final Object $wordDeltasByState = this.getWordDeltasByState();
        result = result * PRIME + ($wordDeltasByState == null ? 43 :
                $wordDeltasByState.hashCode());
        final Object $lastModifiedTargetId = this.getLastModifiedTargetId();
        result = result * PRIME + ($lastModifiedTargetId == null ? 43 :
                $lastModifiedTargetId.hashCode());
        return result;
    }

    protected boolean canEqual(Object other) {
        return other instanceof DocStatsEvent;
    }
}
