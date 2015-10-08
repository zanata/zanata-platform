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

import com.google.common.base.Optional;
import com.google.common.util.concurrent.ListenableFuture;
import org.jboss.seam.annotations.intercept.Interceptor;
import org.jboss.seam.intercept.InvocationContext;
import org.jboss.seam.intercept.JavaBeanInterceptor;
import org.jboss.seam.intercept.OptimizedInterceptor;
import org.zanata.util.ServiceLocator;

import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.Future;

/**
 * @author Carlos Munoz <a href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 */
@Interceptor(stateless = true, around = JavaBeanInterceptor.class)
public class AsyncMethodInterceptor implements OptimizedInterceptor {

    static final ThreadLocal<Boolean> interceptorRan =
            new ThreadLocal<Boolean>();

    @Override
    public Object aroundInvoke(final InvocationContext ctx) throws Exception {
        if (ctx.getMethod().getAnnotation(Async.class) == null) {
            return ctx.proceed();
        }

        final AsyncTaskManager taskManager =
                ServiceLocator.instance().getInstance(
                        AsyncTaskManager.class);

        final AsyncTaskHandleManager taskHandleManager =
                ServiceLocator.instance().getInstance(
                        AsyncTaskHandleManager.class);

        Class<?> methodReturnType = ctx.getMethod().getReturnType();
        if (methodReturnType != void.class
                && !Future.class.isAssignableFrom(methodReturnType)) {
            throw new RuntimeException("Async method "
                    + ctx.getMethod().getName()
                    + " must return java.lang.Future or nothing at all");
        }

        if (interceptorRan.get() == null) {
            // If there is a Task handle parameter (only the first one will be
            // registered)
            final Optional<AsyncTaskHandle> handle =
                    Optional.fromNullable(findHandleIfPresent(ctx
                            .getParameters()));

            AsyncTask asyncTask = () -> {
                interceptorRan.set(true);
                try {
                    if (handle.isPresent()) {
                        handle.get().startTiming();
                    }
                    Object target =
                            ServiceLocator.instance().getInstance(
                                    ctx.getMethod()
                                            .getDeclaringClass());
                    return ctx.getMethod().invoke(target,
                            ctx.getParameters());
                } catch (InvocationTargetException itex) {
                    // exception thrown from the invoked method
                    throw itex.getCause();
                } finally {
                    interceptorRan.remove();
                    if (handle.isPresent()) {
                        handle.get().finishTiming();
                        taskHandleManager.taskFinished(handle.get());
                    }
                }
            };

            ListenableFuture<Object> futureResult =
                    taskManager.startTask(asyncTask);
            if (handle.isPresent()) {
                handle.get().setFutureResult(futureResult);
            }
            return futureResult;
            // Async methods should return ListenableFuture
        } else {
            return ctx.proceed();
        }
    }

    @Override
    public boolean isInterceptorEnabled() {
        return true;
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
