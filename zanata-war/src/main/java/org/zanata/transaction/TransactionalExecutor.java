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
import javax.transaction.TransactionManager;

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

    // using java:comp/UserTransaction will only work for first thread. I am not
    // entirely sure the underlying cause but this will work.
    // see https://home.java.net/node/696522#comment-777077
    @Resource(lookup = "java:jboss/TransactionManager")
    private TransactionManager transactionManager;

    public <R> R runInTransaction(Callable<R> function) throws Exception {
        R result = null;
        try {
//            transactionManager.setTransactionTimeout(30);
            transactionManager.begin();
            result = function.call();
            transactionManager.commit();
        } catch (Throwable t) {
            log.error("error running in transaction",
                    Throwables.getRootCause(t));
            transactionManager.rollback();
        }
        return result;
    }
}
