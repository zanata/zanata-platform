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
package org.zanata.async;

import java.security.Principal;

import javax.security.auth.Subject;

import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.async.Asynchronous;
import org.jboss.seam.security.RunAsOperation;

import lombok.extern.slf4j.Slf4j;
import org.zanata.action.AuthenticationEvents;
import org.zanata.dao.AccountDAO;
import org.zanata.model.HAccount;
import org.zanata.security.ZanataJpaIdentityStore;

/**
 * This class executes a Runnable Process asynchronously. Do not use this class
 * directly. Use {@link org.zanata.async.TaskExecutor} instead as this is just a
 * wrapper to make sure Seam can run the task in the background.
 * {@link TaskExecutor} is able to do this as well as return an instance of the
 * task handle to keep track of the task's progress.
 * @author Carlos Munoz <a
 *         href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 */
@Name("asynchronousTaskExecutor")
@Scope(ScopeType.STATELESS)
@AutoCreate
@Slf4j
public class AsynchronousTaskExecutor {

    /**
     * Runs the provided task asynchronously with the given security
     * constraints.
     *
     * @param task Task to run asynchronously.
     * @param runAsPpal Security Principal to tun the task.
     * @param runAsSubject Security Subject to run the task.
     * @param username The username to run the task.
     */
    @Asynchronous
    public <V, H extends AsyncTaskHandle<V>> void runAsynchronously(
            final AsyncTask<V, H> task, final Principal runAsPpal,
            final Subject runAsSubject, final String username) {
        AsyncUtils.outject(task.getHandle(), ScopeType.EVENT);

        RunAsOperation runAsOp = new RunAsOperation() {
            @Override
            public void execute() {
                try {
                    prepareSecurityContext(username);
                    V returnValue = task.call();
                    task.getHandle().set(returnValue);
                } catch (Exception t) {
                    task.getHandle().setException(t);
                    AsynchronousTaskExecutor.log
                            .debug("Exception when executing an asynchronous task.",
                                    t);
                }
            }

            @Override
            public Principal getPrincipal() {
                return runAsPpal;
            }

            @Override
            public Subject getSubject() {
                return runAsSubject;
            }
        };

        runAsOp.run();
    }

    /**
     * Prepares the Drools security context so that it contains all the
     * necessary facts for security checking.
     */
    private static void prepareSecurityContext(String username) {
        /*
         * TODO This should be changed to not need the username. There should be
         * a way to simulate a login for asyn tasks, or at least to inherit the
         * caller's context
         */
        if( username != null ) {
            // Only if it's an authenticated task should it try and do this
            // injection
            AccountDAO accountDAO =
                (AccountDAO) Component.getInstance(AccountDAO.class);
            ZanataJpaIdentityStore idStore =
                    (ZanataJpaIdentityStore) Component
                            .getInstance(ZanataJpaIdentityStore.class);
            AuthenticationEvents authEvts =
                (AuthenticationEvents) Component
                    .getInstance(AuthenticationEvents.class);
            HAccount authenticatedAccount = accountDAO.getByUsername(username);
            authEvts.injectAuthenticatedPersonIntoWorkingMemory(authenticatedAccount);
            idStore.setAuthenticateUser(authenticatedAccount);
        }
    }
}
