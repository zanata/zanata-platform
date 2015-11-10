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
package org.zanata.seam;

import com.google.common.base.Throwables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zanata.transaction.TransactionalExecutor;

import javax.transaction.UserTransaction;
import java.util.concurrent.Callable;

/**
 * Replaces runInTransaction() which is normally @Transactional to run under
 * AutowireTransaction. TransactionalInterceptor isn't active in Autowire
 * tests, so we have to manage the transaction ourselves.
 * @author Sean Flanigan <a href="mailto:sflaniga@redhat.com">sflaniga@redhat.com</a>
 */
public class AutowireTransactionExecutor extends TransactionalExecutor {
    private static final Logger log =
            LoggerFactory.getLogger(AutowireTransactionExecutor.class);

    private UserTransaction transaction;

    public AutowireTransactionExecutor() {
        this(AutowireTransaction.instance());
    }

    public AutowireTransactionExecutor(UserTransaction transaction) {
        this.transaction = transaction;
    }

    @Override
    public <R> R runInTransaction(Callable<R> function) throws Exception {
        R result = null;
        try {
//            transaction.setTransactionTimeout(30);
            transaction.begin();
            result = function.call();
            transaction.commit();
        } catch (Throwable t) {
            log.error("error running in transaction",
                    Throwables.getRootCause(t));
            transaction.rollback();
        }
        return result;
    }
}
