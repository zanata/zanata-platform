package org.zanata.transaction;


import java.util.concurrent.Callable;
import javax.annotation.CheckForNull;
import javax.persistence.EntityManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zanata.util.RunnableEx;

/**
 * A TransactionUtil implementation used in unit tests. Arquillian deployment
 * will exclude it from the deployment as the class name ends with Test.
 *
 * @see org.zanata.arquillian.Deployments#notUnitTest(org.jboss.shrinkwrap.api.ArchivePath)
 */
public class TransactionUtilForUnitTest implements TransactionUtil {
    private static final long serialVersionUID = 1L;
    private static final Logger log =
            LoggerFactory.getLogger(TransactionUtilForUnitTest.class);
    private boolean useTransaction = false;
    private @CheckForNull EntityManager em;

    public TransactionUtilForUnitTest(@CheckForNull EntityManager em) {
        this.em = em;
    }

    public TransactionUtilForUnitTest(@CheckForNull EntityManager em, boolean useTransaction) {
        this(em);
        this.useTransaction = useTransaction;
    }

    private void flushAndClear() {
        if (em != null) {
            em.flush();
            em.clear();
        }
        if (em != null && useTransaction) {
            log.debug("committing in TestTransactionUtil");
            em.getTransaction().commit();
        }
    }

    @Override
    public <R> R call(Callable<R> function) throws Exception {
        log.debug("running in TestTransactionUtil");
        startTransactionIfNeeded();
        R result = function.call();
        flushAndClear();
        return result;
    }

    private void startTransactionIfNeeded() {
        if (useTransaction && em != null) {
            em.getTransaction().begin();
        }
    }

    @Override
    public void run(Runnable runnable) throws Exception {
        log.debug("running in TestTransactionUtil");
        startTransactionIfNeeded();
        runnable.run();
        flushAndClear();
    }

    @Override
    public void runEx(RunnableEx runnable) throws Exception {
        log.debug("running in TestTransactionUtil");
        startTransactionIfNeeded();
        runnable.run();
        flushAndClear();
    }
}
