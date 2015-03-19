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
    /**
     * Reason why it has to be native query.
     */
    String value() default "";

    /**
     * If the query is specific to certain database due to built-in function etc.
     */
    String specificTo() default "";
}
