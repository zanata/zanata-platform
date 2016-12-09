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

import javax.interceptor.InterceptorBinding;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a method as running asynchronously. This means the actual method
 * execution will happen in its own thread. Methods marked with this annotation
 * must return an object of type {@link java.util.concurrent.Future} or void.
 * <p>
 *     If the method returns a Future, an AsyncTaskResult will be created
 *     (unless there is an AsyncTaskHandleParameter with its own
 *     AsyncTaskResult; see below) and returned to the caller immediately.
 *     When the method is invoked in a worker thread, the Future it returns
 *     (eg an AsyncTaskResult) will have its value extracted and passed to
 *     the caller's Future.
 * </p>
 * <p>
 *     If the method has an AsyncTaskHandle parameter, its
 *     Future/AsyncTaskResult will be returned to the client, and the handle
 *     can also be used by the method to provide progress information.
 * </p>
 * <p>
 *     If the method both accepts an AsyncTaskHandle and returns a Future,
 *     it gets really confusing. It's probably better to return void in that
 *     case.
 *     TODO document what happens, or just disallow it.
 * </p>
 * @author Carlos Munoz <a
 *         href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 * @see AsyncMethodInterceptor
 */
@InterceptorBinding
@Target({ ElementType.METHOD, ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Async {
}
