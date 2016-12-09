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

import com.google.common.util.concurrent.AbstractFuture;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * This class allows @Async methods to return a value which will be
 * extracted and then provided to the original thread's Future object.
 * <p>
 *     Currently extends Guava's AbstractFuture (ListenableFuture).
 *     TODO: switch to CompletableFuture (Java 8).
 * </p>
 * @author Carlos Munoz <a href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 * @see javax.ejb.AsyncResult
 * @see AsyncTaskManager
 */
@ParametersAreNonnullByDefault
public class AsyncTaskResult<V> extends AbstractFuture<V> {

    AsyncTaskResult() {
    }

    public static <T> AsyncTaskResult<T> taskResult(@Nullable T value) {
        AsyncTaskResult<T> result = new AsyncTaskResult<T>();
        result.set(value);
        return result;
    }

    public static <T> AsyncTaskResult<T> taskResult() {
        return taskResult(null);
    }

    @Override
    public boolean set(@Nullable V value) {
        return super.set(value);
    }

    @Override
    public boolean setException(Throwable throwable) {
        return super.setException(throwable);
    }
}
