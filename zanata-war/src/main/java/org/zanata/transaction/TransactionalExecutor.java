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

import javax.annotation.Resource;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import javax.transaction.UserTransaction;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Throwables;

/**
 * Executes callables in transactions.
 *
 * @author Carlos Munoz <a
 *         href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 */
@Dependent
public class TransactionalExecutor {
    private static final Logger log =
            LoggerFactory.getLogger(TransactionalExecutor.class);

    @Inject @Resource
    private UserTransaction transaction;

    public TransactionalExecutor() {
    }

    public TransactionalExecutor(UserTransaction transaction) {
        this.transaction = transaction;
    }

    public <R> R runInTransaction(Callable<R> function) throws Exception {
        assert transaction != null : "must inject UserTransaction";
        try {
//            transaction.setTransactionTimeout(30);
            transaction.begin();
            R result = function.call();
            transaction.commit();
            return result;
        } catch (Exception e) {
            log.warn("error running in transaction",
                    Throwables.getRootCause(e));
            transaction.rollback();
            throw e;
        }
    }
}
