/*
 * Copyright 2015, Red Hat, Inc. and individual contributors
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
package org.zanata.jpa;

import javax.enterprise.inject.Alternative;
import javax.interceptor.InvocationContext;
import javax.persistence.EntityTransaction;
import javax.transaction.Status;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;
import org.apache.deltaspike.jpa.impl.transaction.BeanManagedUserTransactionStrategy;
import org.apache.deltaspike.jpa.impl.transaction.context.EntityManagerEntry;
import com.google.common.base.Throwables;

/**
 * @author Sean Flanigan
 *         <a href="mailto:sflaniga@redhat.com">sflaniga@redhat.com</a>
 */
@Alternative
public class NoNestingTransactionStrategy
        extends BeanManagedUserTransactionStrategy {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory
            .getLogger(NoNestingTransactionStrategy.class);

    @Override
    protected void beforeProceed(InvocationContext invocationContext,
            EntityManagerEntry entityManagerEntry,
            EntityTransaction transaction) {
        try {
            UserTransaction ut = resolveUserTransaction();
            if (ut.getStatus() == Status.STATUS_COMMITTED) {
                RuntimeException e = new RuntimeException(
                        "Nested transactions not supported. @Async may help.");
                // If this happens in an event observer, Weld doesn't log the
                // exception type or stack trace, just
                // "WELD-000401 Failure while notifying an observer of event
                // ..."
                log.warn("Attempted nested transaction", e);
                throw e;
            }
            super.beforeProceed(invocationContext, entityManagerEntry,
                    transaction);
        } catch (SystemException e) {
            Throwables.propagate(e);
        }
    }
}
