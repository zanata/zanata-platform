package org.zanata.rest.dto.matrix;

import java.util.Map;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.zanata.model.UserTranslationMatrix;
import lombok.Delegate;
import lombok.NoArgsConstructor;

import com.google.common.collect.Maps;

/**
 * @see org.zanata.rest.dto.matrix.UserWorkMatrix
 * @author Patrick Huang
 *         <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@NoArgsConstructor
public class PerProjectVersionMatrix implements Map<String, PerLocaleMatrix>, MatrixMap<String> {
    @Delegate
    private Map<String, PerLocaleMatrix> internal = Maps.newHashMap();

    public PerProjectVersionMatrix(UserTranslationMatrix matrixRecord) {
        this();
        put(matrixRecord.getProjectIteration().getSlug(), new PerLocaleMatrix(matrixRecord));
    }

    @JsonIgnore
    @Override
    public void putOrCreateIfAbsent(String versionSlug,
            UserTranslationMatrix matrix) {
        if (containsKey(versionSlug)) {
            PerLocaleMatrix perLocaleMatrix = get(versionSlug);
            perLocaleMatrix.putOrCreateIfAbsent(
                    matrix.getLocale().getLocaleId(), matrix);
        } else {
            put(versionSlug, new PerLocaleMatrix(matrix));
        }
    }
}
