package org.zanata.rest;

import java.lang.reflect.Method;
import javax.inject.Inject;
import javax.ws.rs.container.DynamicFeature;
import javax.ws.rs.container.PreMatching;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.FeatureContext;
import javax.ws.rs.ext.Provider;

import org.zanata.security.annotations.NoSecurityCheck;

/**
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@Provider
@PreMatching
public class ZanataRestSecurityBinder implements DynamicFeature {
    @Inject
    private ZanataRestSecurityInterceptor securityInterceptor;

    @Override
    public void configure(ResourceInfo resourceInfo,
            FeatureContext featureContext) {
        Class<?> clazz = resourceInfo.getResourceClass();
        Method method = resourceInfo.getResourceMethod();
        if (!method.isAnnotationPresent(NoSecurityCheck.class)
                && !clazz.isAnnotationPresent(NoSecurityCheck.class)) {
            featureContext.register(securityInterceptor);
        }
    }
}

