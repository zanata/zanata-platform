// based on org.jboss.seam.annotations.Synchronized in Seam 2.3.1

package org.zanata.util;

import javax.interceptor.InterceptorBinding;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Specifies that a stateful component has
 * multiple concurrent clients, and so access
 * to the component must be synchronized.
 *
 * @author Gavin King
 * @author Sean Flanigan
 */
@Target({METHOD, TYPE})
@Retention(RUNTIME)
@Documented
@Inherited
@InterceptorBinding
public @interface Synchronized
{
    long DEFAULT_TIMEOUT = 1000;

    /**
     * How long should we wait for the lock
     * before throwing an exception?
     *
     * @return the timeout in milliseconds
     */
    long timeout() default 0;
}
