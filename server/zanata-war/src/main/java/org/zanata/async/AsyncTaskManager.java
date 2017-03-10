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
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.RequestScoped;
import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.security.auth.Subject;
import org.apache.deltaspike.cdise.api.ContextControl;
import org.zanata.config.AsyncConfig;
import org.zanata.dao.AccountDAO;
import org.zanata.model.HAccount;
import org.zanata.seam.security.ZanataJpaIdentityStore;
import org.zanata.security.ZanataIdentity;
import org.zanata.security.annotations.Authenticated;
import org.zanata.security.annotations.AuthenticatedLiteral;
import org.zanata.util.ServiceLocator;
import com.google.common.util.concurrent.ListenableFuture;
// TODO consider switching from Guava's ListenableFuture to Java 8's CompletableFuture

/**
 * @author Carlos Munoz
 *         <a href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 */
@Named("asyncTaskManager")
@javax.enterprise.context.ApplicationScoped
public class AsyncTaskManager {
    private static final org.slf4j.Logger log =
            org.slf4j.LoggerFactory.getLogger(AsyncTaskManager.class);

    // TODO use ManagedExecutorService on Java EE 7, so that we can eg inject
    // UserTransaction
    private ExecutorService scheduler;
    @Inject
    private AsyncConfig asyncConfig;

    @PostConstruct
    public void init() {
        scheduler =
                Executors.newFixedThreadPool(asyncConfig.getThreadPoolSize());
    }

    @PreDestroy
    public void cleanup() {
        scheduler.shutdown();
    }

    /**
     * Starts a task asynchronously. In its present implementation can only run
     * tasks which expect a {@code Future} result.
     *
     * @param task
     *            The task to run.
     * @param <V>
     *            The type of result expected.
     * @return A listenable future for the expected result.
     */
    public <V> ListenableFuture<V>
            startTask(@Nonnull final AsyncTask<Future<V>> task) {
        HAccount taskOwner = ServiceLocator.instance()
                .getInstance(HAccount.class, new AuthenticatedLiteral());
        ZanataIdentity ownerIdentity = ZanataIdentity.instance();
        // Extract security context from current thread
        final String taskOwnerUsername =
                taskOwner != null ? taskOwner.getUsername() : null;
        final Principal runAsPpal = ownerIdentity.getPrincipal();
        final Subject runAsSubject = ownerIdentity.getSubject();
        // final result
        final AsyncTaskResult<V> taskFuture = new AsyncTaskResult<V>();
        // The logic to run to setup all necessary contexts and specific logic
        final Runnable executableCommand = () -> {
            ContextControl ctxCtrl = null;
            try {
                // Start CDI contexts
                ctxCtrl = ServiceLocator.instance()
                        .getInstance(ContextControl.class);
                ctxCtrl.startContext(RequestScoped.class);
                ctxCtrl.startContext(SessionScoped.class);
                // Prepare the security context
                prepareSecurityContext(taskOwnerUsername, runAsPpal,
                        runAsSubject);
                // run the task and capture the result
                V returnValue = getReturnValue(task.call());
                taskFuture.set(returnValue);
            } catch (Throwable t) {
                taskFuture.setException(t);
                log.error("Exception when executing an asynchronous task.", t);
            } finally {
                // stop the contexts to make sure all beans are cleaned up
                if (ctxCtrl != null) {
                    ctxCtrl.stopContext(RequestScoped.class);
                    ctxCtrl.stopContext(SessionScoped.class);
                }
            }
        };
        scheduler.execute(executableCommand);
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
     * Prepares the security context so that it contains all the necessary facts
     * for security checking.
     */
    private static void prepareSecurityContext(String username, Principal ppal,
            Subject subject) {
        /*
         * TODO This should be changed to not need any parameters. There should
         * be a way to simulate a login for async tasks, or at least to inherit
         * the caller's context
         */
        if (username != null) {
            // Only if it's an authenticated task should it try and do this
            // injection
            AccountDAO accountDAO =
                    ServiceLocator.instance().getInstance(AccountDAO.class);
            ZanataJpaIdentityStore idStore = ServiceLocator.instance()
                    .getInstance(ZanataJpaIdentityStore.class);
            HAccount authenticatedAccount = accountDAO.getByUsername(username);
            idStore.setAuthenticateUser(authenticatedAccount);
        }
        ZanataIdentity.instance().acceptExternalSubjectAndPpal(subject, ppal);
    }
}
