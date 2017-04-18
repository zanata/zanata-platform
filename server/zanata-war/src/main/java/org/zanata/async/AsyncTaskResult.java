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

import java.util.concurrent.CompletableFuture;

/**
 * Represents the result of an asynchronous task.
 * At this point this is a rename of Java's CompletableFuture class with some
 * utility methods for creating instances.
 * @author Carlos Munoz <a href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 */
public class AsyncTaskResult<V> extends CompletableFuture<V> {

    AsyncTaskResult() {
    }

    public static <T> AsyncTaskResult<T> completed(T value) {
        AsyncTaskResult<T> result = new AsyncTaskResult<T>();
        result.complete(value);
        return result;
    }

    public static <T> AsyncTaskResult<T> completed() {
        return completed(null);
    }
}
