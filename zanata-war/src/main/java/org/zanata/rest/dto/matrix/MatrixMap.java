package org.zanata.rest.dto.matrix;

import org.zanata.model.UserTranslationMatrix;

public interface MatrixMap<K> {
    void putOrCreateIfAbsent(K key, UserTranslationMatrix matrix);
}
