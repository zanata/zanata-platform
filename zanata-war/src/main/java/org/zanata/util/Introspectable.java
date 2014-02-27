package org.zanata.util;

import java.util.Collection;

public interface Introspectable {

    String getId();

    Collection<String> getFieldNames();

    String get(String fieldName);
}
