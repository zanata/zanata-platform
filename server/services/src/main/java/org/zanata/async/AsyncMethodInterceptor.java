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

import java.lang.reflect.InvocationTargetException;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import javax.inject.Inject;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;

import org.apache.deltaspike.core.api.future.Futureable;

/**
 * A second interceptor for the {@code @Futureable} annotation which adds support for
 * AsyncTaskHandle parameters: setting the start and finish times and passing
 * the result of the method execution as a Future result. It must run after
 * {@code org.apache.deltaspike.core.impl.future.FutureableInterceptor} in beans.xml,
 * not before.
 * @author Carlos Munoz <a href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 * @author Sean Flanigan <a href="mailto:sflaniga@redhat.com">sflaniga@redhat.com</a>
 */
// TODO create a new annotation for AsyncTaskHandle parameter handling? more efficient
@Futureable
@Interceptor
//@Priority(Interceptor.Priority.LIBRARY_BEFORE)
public class AsyncMethodInterceptor {

    private final AsyncTaskHandleManager taskHandleManager;

    @Inject
    public AsyncMethodInterceptor(AsyncTaskHandleManager taskHandleManager) {
        this.taskHandleManager = taskHandleManager;
    }

    @SuppressWarnings("unused")
    AsyncMethodInterceptor() {
        this(null);
    }

    @SuppressWarnings("unchecked")
    @AroundInvoke
    public Object aroundInvoke(final InvocationContext ctx) throws Exception {

        // If there is a Task handle parameter (only the first one will be
        // registered)
        Optional<AsyncTaskHandle<?>> handle = findHandleIfPresent(
                ctx.getParameters());

        // This would only make sense if we used a dedicated annotation
        //.orElseThrow(() -> new RuntimeException("No AsyncTaskHandle parameter found"));

        CompletableFuture future = new CompletableFuture<>();
        try {
            handle.ifPresent(h -> {
                h.startTiming();
                h.setFutureResult(future);
            });
            Object result = ctx.proceed();
            future.complete(result);
            return result;
        } catch (Exception e) {
            future.completeExceptionally(e);
            throw e;
        } catch (Throwable e) {
            future.completeExceptionally(e);
            throw new InvocationTargetException(e);
        } finally {
            handle.ifPresent(h -> {
                h.finishTiming();
                taskHandleManager.taskFinished(h);
            });
        }
    }

    private Optional<AsyncTaskHandle<?>> findHandleIfPresent(Object[] params) {
        for (Object param : params) {
            if (param instanceof AsyncTaskHandle) {
                return Optional.of((AsyncTaskHandle<?>) param);
            }
        }
        return Optional.empty();
    }
}
