package org.zanata.rest.editor.dto;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import lombok.Getter;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
public class LocaleSortField implements Serializable {
    public static final String LOCALE = "localeId";
    public static final String MEMBER = "member";

    @Getter
    private final String entityField;
    @Getter
    private final boolean ascending;

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

    public static final LocaleSortField getByField(String field) {
        if (field == null || field.length() <= 0) {
            throw new IllegalArgumentException(field);
        }

        boolean isAscending = !field.startsWith("-");
        String processedField =
            field.startsWith("-") ? field.substring(1) : field;

        if (fieldMap.containsKey(processedField)) {
            return new LocaleSortField(fieldMap.get(processedField),
                isAscending);
        }
        return null;
    }
}
