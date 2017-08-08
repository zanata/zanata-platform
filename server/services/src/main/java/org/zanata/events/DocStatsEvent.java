package org.zanata.events;

import java.util.Map;
import org.zanata.common.ContentState;
import javax.annotation.Nonnull;

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

    @java.beans.ConstructorProperties({ "key", "projectVersionId",
            "wordDeltasByState", "lastModifiedTargetId" })
    public DocStatsEvent(final DocumentLocaleKey key,
            final Long projectVersionId,
            final Map<ContentState, Long> wordDeltasByState,
            final Long lastModifiedTargetId) {
        this.key = key;
        this.projectVersionId = projectVersionId;
        this.wordDeltasByState = wordDeltasByState;
        this.lastModifiedTargetId = lastModifiedTargetId;
    }

    public DocumentLocaleKey getKey() {
        return this.key;
    }

    public Long getProjectVersionId() {
        return this.projectVersionId;
    }

    /**
     * Updated content states with word counts
     */
    public Map<ContentState, Long> getWordDeltasByState() {
        return this.wordDeltasByState;
    }

    public Long getLastModifiedTargetId() {
        return this.lastModifiedTargetId;
    }

    @Override
    public boolean equals(final Object o) {
        if (o == this)
            return true;
        if (!(o instanceof DocStatsEvent))
            return false;
        final DocStatsEvent other = (DocStatsEvent) o;
        if (!other.canEqual((Object) this))
            return false;
        final Object this$key = this.getKey();
        final Object other$key = other.getKey();
        if (this$key == null ? other$key != null : !this$key.equals(other$key))
            return false;
        final Object this$projectVersionId = this.getProjectVersionId();
        final Object other$projectVersionId = other.getProjectVersionId();
        if (this$projectVersionId == null ? other$projectVersionId != null
                : !this$projectVersionId.equals(other$projectVersionId))
            return false;
        final Object this$wordDeltasByState = this.getWordDeltasByState();
        final Object other$wordDeltasByState = other.getWordDeltasByState();
        if (this$wordDeltasByState == null ? other$wordDeltasByState != null
                : !this$wordDeltasByState.equals(other$wordDeltasByState))
            return false;
        final Object this$lastModifiedTargetId = this.getLastModifiedTargetId();
        final Object other$lastModifiedTargetId =
                other.getLastModifiedTargetId();
        if (this$lastModifiedTargetId == null
                ? other$lastModifiedTargetId != null
                : !this$lastModifiedTargetId.equals(other$lastModifiedTargetId))
            return false;
        return true;
    }

    protected boolean canEqual(final Object other) {
        return other instanceof DocStatsEvent;
    }

    @Override
    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final Object $key = this.getKey();
        result = result * PRIME + ($key == null ? 43 : $key.hashCode());
        final Object $projectVersionId = this.getProjectVersionId();
        result = result * PRIME + ($projectVersionId == null ? 43
                : $projectVersionId.hashCode());
        final Object $wordDeltasByState = this.getWordDeltasByState();
        result = result * PRIME + ($wordDeltasByState == null ? 43
                : $wordDeltasByState.hashCode());
        final Object $lastModifiedTargetId = this.getLastModifiedTargetId();
        result = result * PRIME + ($lastModifiedTargetId == null ? 43
                : $lastModifiedTargetId.hashCode());
        return result;
    }
}
