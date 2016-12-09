/*
 * Copyright 2017, Red Hat, Inc. and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.zanata.async;

import javax.annotation.Nonnull;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

import java.util.function.Consumer;

/**
 * @author Sean Flanigan <a href="mailto:sflaniga@redhat.com">sflaniga@redhat.com</a>
 */
public class AsyncUtil {
    private AsyncUtil() {
    }

    public static void onFailure(ListenableFuture<?> future, Consumer<Throwable> consumer) {
        Futures.addCallback(future, onFailure(consumer));
    }

    public static void onFailure(ListenableFuture<?> future, Runnable runnable) {
        Futures.addCallback(future, onFailure(e -> runnable.run()));
    }

    private static <T> FutureCallback<T> onFailure(Consumer<Throwable> consumer) {
        return new FutureCallback<T>() {
            @Override
            public void onSuccess(T result) {
            }

            @Override
            public void onFailure(@Nonnull Throwable e) {
                consumer.accept(e);
            }
        };
    }

}
