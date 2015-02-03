package org.zanata.rest.dto.matrix;

import java.util.Map;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.zanata.model.UserTranslationMatrix;
import com.google.common.collect.Maps;
import lombok.Delegate;
import lombok.NoArgsConstructor;

/**
 * @see org.zanata.rest.dto.matrix.UserWorkMatrix
 * @author Patrick Huang
 *         <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@NoArgsConstructor
public class PerProjectMatrix implements Map<String, PerProjectVersionMatrix>, MatrixMap<String> {
    @Delegate
    private Map<String, PerProjectVersionMatrix> internal = Maps.newHashMap();

    public PerProjectMatrix(UserTranslationMatrix matrixRecord) {
        this();
        put(matrixRecord.getProjectIteration().getProject().getSlug(), new PerProjectVersionMatrix(matrixRecord));
    }

    @JsonIgnore
    @Override
    public void putOrCreateIfAbsent(String projectSlug,
            UserTranslationMatrix matrixRecord) {
        if (containsKey(projectSlug)) {
            PerProjectVersionMatrix perProjectVersionMatrix = get(projectSlug);
            perProjectVersionMatrix.putOrCreateIfAbsent(
                    matrixRecord.getProjectIteration().getSlug(), matrixRecord);
        } else {
            put(projectSlug, new PerProjectVersionMatrix(matrixRecord));
        }
    }
}
