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
package org.zanata.cdi;

import org.apache.deltaspike.core.api.exclude.Exclude;
import org.apache.deltaspike.core.api.projectstage.ProjectStage;
import javax.persistence.EntityManager;
import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.InvalidTransactionException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.Status;
import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;
import javax.transaction.UserTransaction;

/**
 * Test transaction to use in CDI tests. This will simulate a real transaction
 * when accesing an entity manager.
 *
 * @author Carlos Munoz
 *         <a href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 */
@Exclude(ifProjectStage = ProjectStage.IntegrationTest.class)
public class TestTransaction implements UserTransaction, TransactionManager {
    private boolean active;
    private EntityManager entityManager;

    public TestTransaction(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public void begin() throws NotSupportedException, SystemException {
        active = true;
    }

    private EntityManager getEntityManager() {
        return entityManager;
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
    public void rollback()
            throws IllegalStateException, SecurityException, SystemException {
        active = false;
    }

    @Override
    public void setRollbackOnly()
            throws IllegalStateException, SystemException {
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
    public void resume(Transaction tobj) throws InvalidTransactionException,
            IllegalStateException, SystemException {
        throw new RuntimeException();
    }

    public boolean isActive() {
        return this.active;
    }
}
