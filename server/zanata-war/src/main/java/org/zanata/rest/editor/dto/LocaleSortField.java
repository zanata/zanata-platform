package org.zanata.rest.editor.dto;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nullable;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
public class LocaleSortField implements Serializable {
    public static final String LOCALE = "localeId";
    public static final String MEMBER = "member";
    private final String entityField;
    private final boolean ascending;

    /**
     * Sign for descending sort of a field. This should be placed in front of
     * the sorting field.
     */
    private static final String DESCENDING_SIGN = "-";
    private static final Map<String, String> fieldMap;
    static {
        fieldMap = new HashMap<String, String>();
        fieldMap.put(LOCALE, "localeId");
        fieldMap.put(MEMBER, "size(members)");
    }

    public LocaleSortField(String entityField, boolean ascending) {
        this.entityField = entityField;
        this.ascending = ascending;
    }

    /**
     * Factory method to create a valid LocaleSortField. returns null if field
     * is not in #fieldMap.
     */
    @Nullable
    public static final LocaleSortField getByField(String field) {
        if (field == null || field.length() <= 0) {
            throw new IllegalArgumentException(field);
        }
        boolean isAscending = !field.startsWith(DESCENDING_SIGN);
        String processedField =
                field.startsWith(DESCENDING_SIGN) ? field.substring(1) : field;
        if (fieldMap.containsKey(processedField)) {
            return new LocaleSortField(fieldMap.get(processedField),
                    isAscending);
        }
        return null;
    }

    public String getEntityField() {
        return this.entityField;
    }

    public boolean isAscending() {
        return this.ascending;
    }
}
