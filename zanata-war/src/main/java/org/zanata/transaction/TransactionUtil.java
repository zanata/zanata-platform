/*
 * Copyright 2015, Red Hat, Inc. and individual contributors as indicated by the
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
package org.zanata.transaction;

import java.util.concurrent.Callable;

import lombok.extern.slf4j.Slf4j;

import org.apache.deltaspike.jpa.api.transaction.Transactional;
import org.zanata.util.BeanHolder;
import org.zanata.util.ServiceLocator;

/**
 * Utility class to help with transactions.
 *
 * @author Carlos Munoz <a
 *         href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 */
@Slf4j
public class TransactionUtil {

    /**
     * Runs the provided function (in the shape of a Callable) in its own
     * transaction.
     *
     * @param function
     * @param <R>
     *            Return type expected. The provided function must return this
     *            type.
     * @return Whatever the function returns.
     * @throws Exception
     *             Exception (if any) thrown by the given function.
     */
    public static <R> R runInTransaction(Callable<R> function) throws Exception {
        try (BeanHolder<TransactionalExecutor> txExecutor =
                ServiceLocator.instance()
                        .getDependent(TransactionalExecutor.class)) {
            return txExecutor.get()
                    .runInTransaction(function);
        }
    }

    /**
     * Same as {@link TransactionUtil#runInTransaction(Callable)} but for
     * Runnables (functions that don't return anything)
     *
     * @param function
     *            The function (in the form of a Runnable) to execute.
     * @throws Exception
     *             Exception (if any) thrown by the given function.
     * @see TransactionUtil#runInTransaction(Callable)
     */
    public static void runInTransaction(Runnable function) throws Exception {
        runInTransaction(() -> {
            function.run();
            return null;
        });
    }

    public static class TransactionalExecutor {
        @Transactional
        public <R> R runInTransaction(Callable<R> function) throws Exception {
            return function.call();
        }
    }
}
