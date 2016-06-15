package org.zanata.events;

import java.util.Map;

import org.zanata.common.ContentState;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import javax.annotation.Nonnull;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
@AllArgsConstructor
@Getter
@EqualsAndHashCode
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
}
