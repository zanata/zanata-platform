/*
 * Copyright 2014, Red Hat, Inc. and individual contributors as indicated by the
 * @author tags. See the copyright.txt file in the distribution for a full
 * listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this software; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
 * site: http://www.fsf.org.
 */
package org.zanata.async;

import org.zanata.util.ServiceLocator;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;
import java.lang.reflect.InvocationTargetException;
import java.util.Optional;
import java.util.concurrent.Future;

/**
 * @author Carlos Munoz <a href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 */
@Async
@Interceptor
public class AsyncMethodInterceptor {

    private static final ThreadLocal<Boolean> shouldRunAsyncThreadLocal =
            ThreadLocal.withInitial(() -> true);

    @Inject
    private AsyncTaskManager taskManager;

    @Inject
    private AsyncTaskHandleManager taskHandleManager;

    //    AsyncMethodInterceptor() {}

//    @Inject
//    AsyncMethodInterceptor(AsynchronousTaskManager taskManager, AsyncTaskHandleManager taskHandleManager) {
//        this.taskManager = taskManager;
//        this.taskHandleManager = taskHandleManager;
//    }

    @AroundInvoke
    public <V> Object aroundInvoke(final InvocationContext ctx) throws Exception {

        Class<?> methodReturnType = ctx.getMethod().getReturnType();
        if (methodReturnType != void.class
                && !methodReturnType.isAssignableFrom(AsyncTaskResult.class)) {
            throw new RuntimeException("Async method "
                    + ctx.getMethod().getName()
                    + " must return java.lang.Future (or AsyncTaskResult) or void");
        }
        // if this is already an AsyncTask in a worker thread, this will be false:
        boolean shouldRunAsync = shouldRunAsyncThreadLocal.get();
        // reset the state in case this thread triggers *other* Async methods
        shouldRunAsyncThreadLocal.remove();
        if (shouldRunAsync) {
            // If there is a Task handle parameter (only the first one will be
            // registered)
            final Optional<AsyncTaskHandle> handle =
                    Optional.ofNullable(findHandleIfPresent(ctx
                            .getParameters()));

            AsyncTask<Future<V>> asyncTask = () -> {
                // ensure that the next invocation of this interceptor in this
                // thread will skip the AsyncTask:
                shouldRunAsyncThreadLocal.set(false);
                try {
                    handle.ifPresent(AsyncTaskHandle::startTiming);
                    // TODO Handle CDI Qualifiers
                    Object target =
                            ServiceLocator.instance().getInstance(
                                    ctx.getMethod()
                                            .getDeclaringClass());
                    // NB: might be null - if method returns void
                    //noinspection unchecked
                    return (Future<V>) ctx.getMethod().invoke(target,
                            ctx.getParameters());
                } catch (InvocationTargetException itex) {
                    // exception thrown from the invoked method
                    throw itex.getCause();
                } finally {
                    handle.ifPresent(h -> {
                        h.finishTiming();
                        taskHandleManager.taskFinished(h);
                    });
                }
            };

            @SuppressWarnings("RedundantTypeArguments")
            AsyncTaskResult<V> futureResult =
                    handle.map(AsyncTaskHandle<V>::getFutureResult)
                            .orElseGet(AsyncTaskResult::new);
            taskManager.startTask(asyncTask, futureResult);
            return futureResult;
        } else {
            return ctx.proceed();
        }
    }

    private AsyncTaskHandle findHandleIfPresent(Object[] params) {
        for (Object param : params) {
            if (param instanceof AsyncTaskHandle) {
                return (AsyncTaskHandle) param;
            }
        }
        return null;
    }
}
