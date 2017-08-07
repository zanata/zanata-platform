package org.zanata.async;

import java.io.Serializable;

import com.google.common.base.Joiner;

/**
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
public interface AsyncTaskKey extends Serializable {
    /**
     * When converting multiple fields to form id string, we
     * should use this as separator (URL friendly).
     */
    String SEPARATOR = "-";

    /**
     * @return a unique identifier for the key
     */
    String id();

    /**
     * Helper method to convert list of fields to a String as key id.
     *
     * @param keyName
     *         the name for this key
     * @param fields
     *         key instance field values
     * @return String representation of the key which can be used as id
     */
    static String joinFields(String keyName, Object... fields) {
        return keyName + SEPARATOR
                + Joiner.on(SEPARATOR).useForNull("").join(fields);
    }
}
