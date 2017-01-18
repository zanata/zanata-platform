// derived from org.jboss.seam.core.SynchronizationInterceptor in Seam 2.3.1

package org.zanata.util;

import org.slf4j.Logger;
import org.zanata.exception.LockTimeoutException;

import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;
import java.io.Serializable;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Serializes calls to a component.
 *
 * @author Gavin King
 * @author Sean Flanigan
 */
@Interceptor
@Synchronized
public class SynchronizationInterceptor implements Serializable {
    private static final long defaultTimeout = SysProperties.getLong(
            SysProperties.LOCK_TIMEOUT, Synchronized.DEFAULT_TIMEOUT);
    private static final Logger log =
            org.slf4j.LoggerFactory.getLogger(SynchronizationInterceptor.class);

    private final ReentrantLock lock = new ReentrantLock(true);

    @AroundInvoke
    public Object aroundInvoke(InvocationContext invocation) throws Exception {
        Synchronized sa = invocation.getTarget().getClass()
                .getAnnotation(Synchronized.class);
        long timeout = sa.timeout() != 0 ? sa.timeout() : defaultTimeout;
        log.debug("trying the lock");
        if (lock.tryLock(timeout, TimeUnit.MILLISECONDS)) {
            try {
                log.debug("got the lock");
                return invocation.proceed();
            } finally {
                lock.unlock();
                log.debug("released the lock");
            }
        } else {
            throw new LockTimeoutException(
                    "could not acquire lock on @Synchronized component with class " +
                            invocation.getTarget().getClass().getName() +
                            " before executing method " +
                            invocation.getMethod());
        }
    }

}
