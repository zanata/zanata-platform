package org.zanata.rest.dto.matrix;

import java.util.List;
import java.util.Map;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.zanata.common.LocaleId;
import org.zanata.model.UserTranslationMatrix;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.Delegate;
import lombok.NoArgsConstructor;

/**
 * @see org.zanata.rest.dto.matrix.UserWorkMatrix
 * @author Patrick Huang
 *         <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@NoArgsConstructor
public class PerLocaleMatrix implements Map<LocaleId, List<ContentStateToWordCount>>, MatrixMap<LocaleId> {
    @Delegate
    private Map<LocaleId, List<ContentStateToWordCount>> internal = Maps.newHashMap();

    public PerLocaleMatrix(
            UserTranslationMatrix matrix) {
        this();
        put(matrix.getLocale().getLocaleId(), Lists.newArrayList(new ContentStateToWordCount(matrix.getSavedState(), matrix.getWordCount())));
    }

    @JsonIgnore
    @Override
    public void putOrCreateIfAbsent(LocaleId key, UserTranslationMatrix matrix) {
        if (containsKey(key)) {
            List<ContentStateToWordCount> contentStateToWordCounts = get(key);
            contentStateToWordCounts.add(new ContentStateToWordCount(matrix.getSavedState(), matrix.getWordCount()));
        } else {
            put(key, Lists.newArrayList(new ContentStateToWordCount(matrix.getSavedState(), matrix.getWordCount())));
        }
    }
}
