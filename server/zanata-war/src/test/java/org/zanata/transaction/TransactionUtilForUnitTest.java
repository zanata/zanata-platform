package org.zanata.transaction;


import java.util.concurrent.Callable;
import javax.persistence.EntityManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zanata.util.RunnableEx;

/**
 * A TransactionUtil implementation used in tests.
 */
public class TransactionUtilForUnitTest implements TransactionUtil {
    private static final Logger log =
            LoggerFactory.getLogger(TransactionUtilForUnitTest.class);
    private EntityManager em;

    public TransactionUtilForUnitTest(EntityManager em) {
        this.em = em;
    }

    public TransactionUtilForUnitTest() {
    }

    @Override
    public <R> R call(Callable<R> function) throws Exception {
        log.debug("running in TestTransactionUtil");
        R result = function.call();
        em.flush();
        em.clear();
        return result;
    }

    @Override
    public void run(Runnable runnable) throws Exception {
        log.debug("running in TestTransactionUtil");
        runnable.run();
        em.flush();
        em.clear();
    }

    @Override
    public void runEx(RunnableEx runnable) throws Exception {
        log.debug("running in TestTransactionUtil");
        runnable.run();
        em.flush();
        em.clear();
    }
}
