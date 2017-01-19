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
import javax.ejb.ApplicationException;
import javax.enterprise.context.ApplicationScoped;
import javax.faces.convert.ConverterException;
import javax.faces.validator.ValidatorException;
import javax.inject.Inject;
import javax.naming.NamingException;
import javax.persistence.EntityManager;
import javax.transaction.NotSupportedException;
import javax.transaction.Status;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;
import org.zanata.util.IServiceLocator;
import org.zanata.util.RunnableEx;
import org.zanata.util.ServiceLocator;
import static javax.transaction.Status.STATUS_ACTIVE;
import static javax.transaction.Status.STATUS_MARKED_ROLLBACK;
import static javax.transaction.Status.STATUS_ROLLEDBACK;
//@Stateless
//@TransactionManagement(TransactionManagementType.BEAN)

/**
 * Utility class to help with transactions. Based on Seam 2 code including
 * org.jboss.seam.util.Work and org.jboss.seam.transaction.*.
 *
 * @author Carlos Munoz
 *         <a href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 * @author Sean Flanigan
 *         <a href="mailto:sflaniga@redhat.com">sflaniga@redhat.com</a>
 */
@ApplicationScoped
public class TransactionUtilImpl implements TransactionUtil {
    private static final org.slf4j.Logger log =
            org.slf4j.LoggerFactory.getLogger(TransactionUtilImpl.class);

    public static TransactionUtilImpl get() {
        return ServiceLocator.instance().getInstance(TransactionUtilImpl.class);
    }

    @Inject
    private EntityManager entityManager;
    @Inject
    private IServiceLocator serviceLocator;

    /**
     * Runs the provided function (in the shape of a Callable) in its own
     * transaction.
     *
     * @param function
     *            the function to call
     * @param <R>
     *            Return type expected. The provided function must return this
     *            type.
     * @return Whatever the function returns.
     * @throws Exception
     *             Exception (if any) thrown by the given function.
     */
    public static <R> R runInTransaction(Callable<R> function)
            throws Exception {
        return get().call(function);
    }

    @Override
    public <R> R call(Callable<R> function) throws Exception {
        UserTransaction transaction = null;
        // these values used to come from Seam's Transactional annotation
        boolean transactionActive = false;
        boolean newTransactionRequired = false;
        UserTransaction userTransaction = null;
        transaction = getUserTransaction();
        if (transaction.getStatus() == Status.STATUS_COMMITTED) {
            throw new RuntimeException(
                    "Nested transactions not supported. @Async may help.");
        }
        transactionActive = isActiveOrMarkedRollback(transaction)
                || isRolledBack(transaction); // TODO: temp workaround,
        // what should we really do
        // in this case??
        newTransactionRequired = isNewTransactionRequired(transactionActive);
        userTransaction = newTransactionRequired ? transaction : null;
        try {
            if (newTransactionRequired) {
                log.debug("beginning transaction");
                userTransaction.begin();
            }
            entityManager.joinTransaction();
            R result = function.call();
            if (newTransactionRequired) {
                if (isMarkedRollback(transaction)) {
                    log.debug("rolling back transaction");
                    userTransaction.rollback();
                } else {
                    log.debug("committing transaction");
                    userTransaction.commit();
                }
            }
            return result;
        } catch (IllegalStateException | NotSupportedException
                | SystemException e) {
            throw e;
        } catch (Exception e) {
            if (newTransactionRequired && userTransaction
                    .getStatus() != Status.STATUS_NO_TRANSACTION) {
                if (isRollbackRequired(e, true)) {
                    log.debug("rolling back transaction");
                    userTransaction.rollback();
                } else {
                    log.debug(
                            "committing transaction after ApplicationException(rollback=false):"
                                    + e.getMessage());
                    userTransaction.commit();
                }
            }
            throw e;
        }
    }

    /**
     * Same as {@link TransactionUtilImpl#runInTransaction(Callable)} but for
     * Runnables (functions that don't return anything)
     *
     * @param runnable
     *            The function (in the form of a Runnable) to execute.
     * @throws Exception
     *             Exception (if any) thrown by the given function.
     * @see TransactionUtilImpl#runInTransaction(Callable)
     */
    public static void runInTransaction(Runnable runnable) throws Exception {
        get().run(runnable);
    }

    @Override
    public void run(Runnable runnable) throws Exception {
        runInTransaction(() -> {
            runnable.run();
            return null;
        });
    }

    @Override
    public void runEx(RunnableEx runnable) throws Exception {
        runInTransaction(() -> {
            runnable.run();
            return null;
        });
    }
    // Adapted from org.jboss.seam.transaction.Transaction.getUserTransaction()

    private UserTransaction getUserTransaction() throws NamingException {
        return serviceLocator.getJndiComponent("java:jboss/UserTransaction",
                UserTransaction.class);
        // try {
        // return serviceLocator.getJndiComponent("java:comp/UserTransaction",
        // UserTransaction.class);
        // } catch (NamingException ne) {
        // try {
        // // Embedded JBoss has no java:comp/UserTransaction
        // UserTransaction ut = serviceLocator.getJndiComponent(
        // "UserTransaction", UserTransaction.class);
        // ut.getStatus(); // for glassfish, which can return an unusable
        // // UT
        // return ut;
        // } catch (NamingException nnfe2) {
        // // Try the other JBoss location in JBoss AS7
        // return serviceLocator.getJndiComponent(
        // "java:jboss/UserTransaction",
        // UserTransaction.class);
        // } catch (Exception e) {
        // throw ne;
        // }
        // }
    }

    private static boolean isActiveOrMarkedRollback(UserTransaction transaction)
            throws SystemException {
        int status = transaction.getStatus();
        return status == STATUS_ACTIVE || status == STATUS_MARKED_ROLLBACK;
    }

    private static boolean isMarkedRollback(UserTransaction transaction)
            throws SystemException {
        return transaction.getStatus() == STATUS_MARKED_ROLLBACK;
    }

    private static boolean isRolledBack(UserTransaction transaction)
            throws SystemException {
        return transaction.getStatus() == STATUS_ROLLEDBACK;
    }

    private static boolean isNewTransactionRequired(boolean transactionActive) {
        return !transactionActive;
    }

    private static boolean isRollbackRequired(Exception e, boolean isJavaBean) {
        Class<? extends Exception> clazz = e.getClass();
        return (isSystemException(e, isJavaBean, clazz)) || (clazz
                .isAnnotationPresent(ApplicationException.class)
                && clazz.getAnnotation(ApplicationException.class).rollback());
    }

    private static boolean isSystemException(Exception e, boolean isJavaBean,
            Class<? extends Exception> clazz) {
        return
        // TODO: this is hackish, maybe just turn off RollbackInterceptor
        // for @Converter/@Validator components
        isJavaBean && (e instanceof RuntimeException)
                && !clazz.isAnnotationPresent(ApplicationException.class)
                && !(e instanceof ValidatorException)
                && !(e instanceof ConverterException);
    }
}
