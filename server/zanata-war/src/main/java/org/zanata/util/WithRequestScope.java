package org.zanata.util;

import javax.interceptor.InterceptorBinding;
import java.lang.annotation.Documented;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Specifies that the CDI scopes RequestScoped and SessionScoped should be
 * activated for a method (unless already active).
 * @author Sean Flanigan
 */
@Target({METHOD, TYPE})
@Retention(RUNTIME)
@Documented
@Inherited
@InterceptorBinding
public @interface WithRequestScope {
}
