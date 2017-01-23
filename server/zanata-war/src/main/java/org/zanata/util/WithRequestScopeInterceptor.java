package org.zanata.util;

import java.io.Serializable;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.ContextNotActiveException;
import javax.enterprise.context.RequestScoped;
import javax.enterprise.context.SessionScoped;
import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Inject;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;
import org.apache.deltaspike.cdise.api.ContextControl;

/**
 * @author Sean Flanigan
 */
@Interceptor
@WithRequestScope
public class WithRequestScopeInterceptor implements Serializable {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory
            .getLogger(WithRequestScopeInterceptor.class);

    @Inject
    private BeanManager beanManager;
    @Inject
    private ContextControl ctxCtrl;

    @AroundInvoke
    public Object aroundInvoke(InvocationContext invocation) throws Exception {
        return invokeWithScopes(invocation, beanManager, ctxCtrl);
    }
    // there just doesn't seem to be a clean way of checking this

    private static boolean isRequestScopeActive(BeanManager beanManager) {
        try {
            return beanManager.getContext(RequestScoped.class).isActive();
        } catch (ContextNotActiveException e) {
            return false;
        }
    }

    private static boolean isSessionScopeActive(BeanManager beanManager) {
        try {
            return beanManager.getContext(SessionScoped.class).isActive();
        } catch (ContextNotActiveException e) {
            return false;
        }
    }

    static Object invokeWithScopes(InvocationContext invocation,
            BeanManager beanManager, ContextControl ctxCtrl) throws Exception {
        boolean shouldStopSession = false;
        boolean shouldStopRequest = false;
        if (!isSessionScopeActive(beanManager)) {
            shouldStopSession = true;
            log.debug("starting session scope");
            ctxCtrl.startContext(SessionScoped.class);
        }
        // this will implicitly bind a new RequestContext to the current thread
        if (!isRequestScopeActive(beanManager)) {
            shouldStopRequest = true;
            log.debug("starting request scope");
            ctxCtrl.startContext(RequestScoped.class);
        }
        try {
            return invocation.proceed();
        } finally {
            // stop the RequestContext to ensure that all request-scoped beans
            // get cleaned up.
            if (shouldStopRequest) {
                ctxCtrl.stopContext(RequestScoped.class);
                log.debug("stopped request scope");
            }
            if (shouldStopSession) {
                ctxCtrl.stopContext(SessionScoped.class);
                log.debug("stopped session scope");
            }
        }
    }
}
