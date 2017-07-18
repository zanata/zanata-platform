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
    private static final Logger log =
            LoggerFactory.getLogger(TransactionUtilForUnitTest.class);
    private @CheckForNull EntityManager em;

    public TransactionUtilForUnitTest(@CheckForNull EntityManager em) {
        this.em = em;
    }

    private void flushAndClear() {
        if (em != null) {
            em.flush();
            em.clear();
        }
    }

    @Override
    public <R> R call(Callable<R> function) throws Exception {
        log.debug("running in TestTransactionUtil");
        R result = function.call();
        flushAndClear();
        return result;
    }

    @Override
    public void run(Runnable runnable) throws Exception {
        log.debug("running in TestTransactionUtil");
        runnable.run();
        flushAndClear();
    }

    @Override
    public void runEx(RunnableEx runnable) throws Exception {
        log.debug("running in TestTransactionUtil");
        runnable.run();
        flushAndClear();
    }
}
