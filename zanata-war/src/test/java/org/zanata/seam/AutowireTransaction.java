/*
 * Copyright 2010, Red Hat, Inc. and individual contributors as indicated by the
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

import javax.persistence.EntityManager;
import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.Status;
import javax.transaction.Synchronization;
import javax.transaction.SystemException;

import org.jboss.seam.transaction.UserTransaction;

/**
 * Simulates a seam transaction for use with {@link SeamAutowire}. Since
 * transactions are not created/injected with {@link SeamAutowire}, this class
 * provides support for methods that use transactions explicitly.
 *
 * This class will always represent an active, non-committed, non-rollbacked
 * transaction.
 *
 * @author Carlos Munoz <a
 *         href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 */
public class AutowireTransaction implements UserTransaction {

    private static final AutowireTransaction instance =
            new AutowireTransaction();

    public static UserTransaction instance() {
        return instance;
    }

    @Override
    public boolean isActive() throws SystemException {
        return true;
    }

    @Override
    public boolean isActiveOrMarkedRollback() throws SystemException {
        return true;
    }

    @Override
    public boolean isRolledBackOrMarkedRollback() throws SystemException {
        return false;
    }

    @Override
    public boolean isMarkedRollback() throws SystemException {
        return false;
    }

    @Override
    public boolean isNoTransaction() throws SystemException {
        return false;
    }

    @Override
    public boolean isRolledBack() throws SystemException {
        return false;
    }

    @Override
    public boolean isCommitted() throws SystemException {
        return false;
    }

    @Override
    public boolean isConversationContextRequired() {
        return false;
    }

    @Override
    public void registerSynchronization(Synchronization sync) {
    }

    @Override
    public void enlist(EntityManager entityManager) throws SystemException {
    }

    @Override
    public void begin() throws NotSupportedException, SystemException {
    }

    @Override
    public void commit() throws RollbackException, HeuristicMixedException,
            HeuristicRollbackException, SecurityException,
            IllegalStateException, SystemException {
    }

    @Override
    public void rollback() throws IllegalStateException, SecurityException,
            SystemException {
    }

    @Override
    public void setRollbackOnly() throws IllegalStateException, SystemException {
    }

    @Override
    public int getStatus() throws SystemException {
        return Status.STATUS_ACTIVE;
    }

    @Override
    public void setTransactionTimeout(int seconds) throws SystemException {
    }
}
