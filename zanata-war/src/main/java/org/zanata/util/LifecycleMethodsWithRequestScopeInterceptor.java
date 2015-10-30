package org.zanata.util;

import java.io.Serializable;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Inject;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;

import lombok.extern.slf4j.Slf4j;

import org.apache.deltaspike.cdise.api.ContextControl;

import static org.zanata.util.WithRequestScopeInterceptor.invokeWithScopes;

/**
 *
 * @author Sean Flanigan
 */
@Interceptor
@LifecycleMethodsWithRequestScope
@Slf4j
// NB we can't inject ContextControl because then we couldn't passivate
// any bean which uses this interceptor
public class LifecycleMethodsWithRequestScopeInterceptor implements Serializable {

    @Inject
    private BeanManager beanManager;

    @PostConstruct
    @PreDestroy
    public Object aroundInvoke(InvocationContext invocation) throws Exception {
        try (BeanHolder<ContextControl> holder =
                ServiceLocator.instance().getDependent(ContextControl.class)) {
            return invokeWithScopes(invocation, beanManager, holder.get());
        }
    }
}
