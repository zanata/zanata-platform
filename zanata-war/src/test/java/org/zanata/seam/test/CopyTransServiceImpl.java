package org.zanata.seam.test;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

import javax.inject.Inject;

import org.zanata.dao.AccountDAO;

/**
 * @author Patrick Huang
 *         <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
public class CopyTransServiceImpl implements CopyTransService {
    @Inject
    private AccountDAO dao;

//    @Inject
//    private AuthenticationBean authenticationBean;

    @Override
    public Future<Boolean> copy() {
        return new FutureTask<Boolean>(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                return true;
            }
        });
    }
}
