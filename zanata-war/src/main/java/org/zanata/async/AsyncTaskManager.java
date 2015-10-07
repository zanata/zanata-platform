/*
 * Copyright 2014, Red Hat, Inc. and individual contributors as indicated by the
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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.annotation.Nonnull;
import javax.security.auth.Subject;

import lombok.extern.slf4j.Slf4j;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Destroy;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.contexts.Lifecycle;
import org.zanata.action.AuthenticationEvents;
import org.zanata.config.AsyncConfig;
import org.zanata.dao.AccountDAO;
import org.zanata.model.HAccount;
import org.zanata.seam.security.AbstractRunAsOperation;
import org.zanata.security.ZanataIdentity;
import org.zanata.seam.security.ZanataJpaIdentityStore;
import org.zanata.util.ServiceLocator;

import com.google.common.util.concurrent.ListenableFuture;

/**
 * @author Carlos Munoz <a
 *         href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 */
@Name("asyncTaskManager")
@Scope(ScopeType.APPLICATION)
@AutoCreate
@Slf4j
public class AsyncTaskManager {

    private ExecutorService scheduler;

    @In
    private AsyncConfig asyncConfig;

    @Create
    public void init() {
        scheduler =
                Executors.newFixedThreadPool(asyncConfig.getThreadPoolSize());
    }

    @Destroy
    public void cleanup() {
        scheduler.shutdown();
    }

    /**
     * Starts a task asynchronously.
     * In its present implementation can only run tasks which expect a
     * {@code Future} result.
     * @param task The task to run.
     * @param <V> The type of result expected.
     * @return A listenable future for the expected result.
     */
    public <V> ListenableFuture<V> startTask(
            final @Nonnull AsyncTask<Future<V>> task) {
        HAccount taskOwner = ServiceLocator.instance()
                .getInstance(ZanataJpaIdentityStore.AUTHENTICATED_USER,
                        HAccount.class);
        ZanataIdentity ownerIdentity = ZanataIdentity.instance();

        // Extract security context from current thread
        final String taskOwnerUsername =
                taskOwner != null ? taskOwner.getUsername() : null;
        final Principal runAsPpal = ownerIdentity.getPrincipal();
        final Subject runAsSubject = ownerIdentity.getSubject();

        // final result
        final AsyncTaskResult<V> taskFuture = new AsyncTaskResult<V>();

        final RunnableOperation runnableOp = new RunnableOperation() {

            @Override
            public void execute() {
                try {
                    prepareSecurityContext(taskOwnerUsername);
                    V returnValue = getReturnValue(task.call());
                    taskFuture.set(returnValue);
                } catch (Throwable t) {
                    taskFuture.setException(t);
                    log.error(
                            "Exception when executing an asynchronous task.", t);
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
        scheduler.execute(runnableOp);
        return taskFuture;
    }

    private static <V> V getReturnValue(Future<V> asyncTaskFuture)
            throws Exception {
        // If the async method returns void
        if (asyncTaskFuture == null) {
            return null;
        }
        return asyncTaskFuture.get();
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
        if (username != null) {
            // Only if it's an authenticated task should it try and do this
            // injection
            AccountDAO accountDAO =
                    ServiceLocator.instance().getInstance(AccountDAO.class);
            ZanataJpaIdentityStore idStore =
                    ServiceLocator.instance().getInstance(
                            ZanataJpaIdentityStore.class);
            AuthenticationEvents authEvts =
                    ServiceLocator.instance().getInstance(
                            AuthenticationEvents.class);
            HAccount authenticatedAccount = accountDAO.getByUsername(username);
            idStore.setAuthenticateUser(authenticatedAccount);
        }
    }

    public abstract class RunnableOperation extends AbstractRunAsOperation
            implements Runnable {

        @Override
        public void run() {
            Lifecycle.beginCall(); // Start contexts
            ZanataIdentity.instance().runAs(this);
            Lifecycle.endCall(); // End contexts
        }
    }

}
