package org.zanata.util;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import javax.interceptor.InterceptorBinding;

/**
 * Specifies that the CDI scopes RequestScoped and SessionScoped should be
 * provided for lifecycle methods, ie @PostConstruct and @PreDestroy.
 * @author Sean Flanigan
 */
@Target(TYPE)
@Retention(RUNTIME)
@Documented
@Inherited
@InterceptorBinding
public @interface LifecycleMethodsWithRequestScope {
}
