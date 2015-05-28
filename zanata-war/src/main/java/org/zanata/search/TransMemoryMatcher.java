package org.zanata.search;

import org.zanata.model.HTextFlow;

/**
 * @author Patrick Huang
 *         <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
public class TransMemoryMatcher {
    public int calculateSimilarityPercent(HTextFlow upcomingSourceContent,
            HTextFlow tranMemory) {
        // TODO pahuang work on plural
        String upcomingSource = upcomingSourceContent.getContents().get(0);
        String tmSource = tranMemory.getContents().get(0);


    }

    public String translationFromTransMemory(HTextFlow tranMemory) {
        throw new UnsupportedOperationException("Implement me!");
    }
}
