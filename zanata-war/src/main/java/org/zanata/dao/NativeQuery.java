package org.zanata.dao;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marker annotation to mark methods that uses native query.
 */
@Target({ElementType.METHOD, ElementType.LOCAL_VARIABLE, ElementType.TYPE, ElementType.PARAMETER})
@Retention(RetentionPolicy.SOURCE)
public @interface NativeQuery {
}
