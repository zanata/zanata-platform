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

import static org.jboss.seam.util.EJB.APPLICATION_EXCEPTION;
import static org.jboss.seam.util.EJB.rollback;

import java.util.concurrent.Callable;

import javax.transaction.Status;

import lombok.extern.slf4j.Slf4j;

import org.jboss.seam.annotations.ApplicationException;
import org.jboss.seam.transaction.Transaction;
import org.jboss.seam.transaction.UserTransaction;
import org.jboss.seam.util.JSF;

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
        UserTransaction transaction = null;
        boolean transactionActive = false;
        boolean newTransactionRequired = false;
        UserTransaction userTransaction = null;

        try {
            transaction = Transaction.instance();

            transactionActive = transaction.isActiveOrMarkedRollback()
                    || transaction.isRolledBack(); // TODO: temp workaround,
                                                   // what should we really do
                                                   // in this case??
            newTransactionRequired =
                    isNewTransactionRequired(transactionActive);
            userTransaction = newTransactionRequired ? transaction : null;
        } catch (IllegalStateException e) {
            // for shutdown case, when we can't get the tx object because the
            // event context is gone
            // but we should still check if a tx is required and fail
            // accordingly if it is
            newTransactionRequired = isNewTransactionRequired(false);
            if (newTransactionRequired) {
                throw e;
            }
        }

        try {
            if (newTransactionRequired) {
                log.debug("beginning transaction");
                userTransaction.begin();
            }

            R result = function.call();
            if (newTransactionRequired) {
                if (transaction.isMarkedRollback()) {
                    log.debug("rolling back transaction");
                    userTransaction.rollback();
                } else {
                    log.debug("committing transaction");
                    userTransaction.commit();
                }
            }
            return result;
        } catch (Exception e) {
            if (newTransactionRequired
                    && userTransaction.getStatus() != Status.STATUS_NO_TRANSACTION) {
                if (isRollbackRequired(e, true)) {
                    log.debug("rolling back transaction");
                    userTransaction.rollback();
                } else {
                    log.debug("committing transaction after ApplicationException(rollback=false):"
                            + e.getMessage());
                    userTransaction.commit();
                }
            }
            throw e;
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

    protected static boolean
            isNewTransactionRequired(boolean transactionActive) {
        return !transactionActive;
    }

    public static boolean isRollbackRequired(Exception e, boolean isJavaBean) {
        Class<? extends Exception> clazz = e.getClass();
        return (isSystemException(e, isJavaBean, clazz))
                ||
                (isJavaBean && clazz.isAnnotationPresent(APPLICATION_EXCEPTION) && rollback(clazz
                        .getAnnotation(APPLICATION_EXCEPTION)))
                ||
                (clazz.isAnnotationPresent(ApplicationException.class) && clazz
                        .getAnnotation(ApplicationException.class).rollback());
    }

    private static boolean isSystemException(Exception e, boolean isJavaBean,
            Class<? extends Exception> clazz) {
        return isJavaBean &&
                (e instanceof RuntimeException) &&
                !clazz.isAnnotationPresent(APPLICATION_EXCEPTION) &&
                !clazz.isAnnotationPresent(ApplicationException.class) &&
                // TODO: this is hackish, maybe just turn off RollackInterceptor
                // for @Converter/@Validator components
                !JSF.VALIDATOR_EXCEPTION.isInstance(e) &&
                !JSF.CONVERTER_EXCEPTION.isInstance(e);
    }
}
