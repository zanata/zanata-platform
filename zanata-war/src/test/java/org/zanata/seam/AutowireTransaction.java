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
import javax.transaction.InvalidTransactionException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.Status;
import javax.transaction.Synchronization;
import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;
import javax.transaction.UserTransaction;

import org.apache.deltaspike.core.api.exclude.Exclude;
import org.apache.deltaspike.core.api.projectstage.ProjectStage;
import lombok.Getter;

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
@Exclude(ifProjectStage = ProjectStage.IntegrationTest.class)
public class AutowireTransaction implements UserTransaction,
        TransactionManager {

    private static final AutowireTransaction instance =
            new AutowireTransaction();
    @Getter
    private boolean active;

    public static UserTransaction instance() {
        return instance;
    }

    @Override
    public void begin() throws NotSupportedException, SystemException {
        active = true;
    }

    private EntityManager getEntityManager() {
        return (EntityManager) SeamAutowire.instance().getComponent(
                "entityManager");
    }

    @Override
    public void commit() throws RollbackException, HeuristicMixedException,
            HeuristicRollbackException, SecurityException,
            IllegalStateException, SystemException {
        EntityManager entityManager = getEntityManager();
        if (entityManager != null) {
            entityManager.flush();
            entityManager.clear();
        }
        active = false;
    }

    @Override
    public void rollback() throws IllegalStateException, SecurityException,
            SystemException {
        active = false;
    }

    @Override
    public void setRollbackOnly() throws IllegalStateException, SystemException {
        throw new RuntimeException();
    }

    @Override
    public int getStatus() throws SystemException {
        if (active) {
            return Status.STATUS_ACTIVE;
        } else {
            return Status.STATUS_NO_TRANSACTION;
        }
    }

    @Override
    public void setTransactionTimeout(int seconds) throws SystemException {
        throw new RuntimeException();
    }

    @Override
    public Transaction getTransaction() throws SystemException {
        throw new RuntimeException();
    }

    @Override
    public Transaction suspend() throws SystemException {
        throw new RuntimeException();
    }

    @Override
    public void resume(Transaction tobj)
            throws InvalidTransactionException, IllegalStateException,
            SystemException {
        throw new RuntimeException();
    }
}
