package org.zanata.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.jboss.seam.annotations.intercept.Interceptors;
import org.zanata.seam.interceptor.RateLimitingInterceptor;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Interceptors(RateLimitingInterceptor.class)
public @interface RateLimitingResource {
}
