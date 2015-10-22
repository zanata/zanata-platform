package org.zanata.util;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import javax.interceptor.InterceptorBinding;

/**
 * Specifies that the CDI scope RequestScoped should be provided for this method
 * @author Sean Flanigan
 */
@Target({METHOD, TYPE})
@Retention(RUNTIME)
@Documented
@Inherited
@InterceptorBinding
public @interface WithRequestScope {
}
