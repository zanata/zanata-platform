package org.zanata.util;

import java.util.Collection;

public interface Introspectable {

    String getIntrospectableId();

    Collection<String> getIntrospectableFieldNames();

    String getFieldValueAsString(String fieldName);
}
