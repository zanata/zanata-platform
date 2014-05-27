package org.zanata.webtrans.server;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import lombok.extern.slf4j.Slf4j;
import net.customware.gwt.dispatch.shared.Action;
import net.customware.gwt.dispatch.shared.ActionException;
import net.customware.gwt.dispatch.shared.Result;

import org.jboss.seam.servlet.ContextualHttpServletRequest;
import org.zanata.util.ServiceLocator;
import org.zanata.webtrans.shared.DispatchService;

import com.google.common.base.Throwables;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;

/**
 * @author Patrick Huang <a
 *         href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@RemoteServiceRelativePath("seam/resource/gwt")
@Slf4j
public class GwtDispatchService extends RemoteServiceServlet implements
        DispatchService {

    @Override
    public Result execute(final Action<?> action) throws Exception {
        final Result[] result = { null };
        try {
            new ContextualHttpServletRequest(getThreadLocalRequest()) {
                @Override
                public void process() throws Exception {
                    SeamDispatch dispatch = ServiceLocator.instance()
                            .getInstance(SeamDispatch.class);
                    result[0] = dispatch.execute(action);
                }
            }.run();
        }
        catch (ServletException e) {
            Throwable cause = e.getCause();
            if (cause != null && cause instanceof ActionException) {
                throw ActionException.class.cast(cause);
            }
            throw e;
        }
        return result[0];
    }

    @Override
    public void rollback(final Action<Result> action, final Result result)
            throws Exception {
        new ContextualHttpServletRequest(getThreadLocalRequest()) {
            @Override
            public void process() throws Exception {
                SeamDispatch dispatch = ServiceLocator.instance()
                        .getInstance(SeamDispatch.class);
                dispatch.rollback(action, result);
            }
        }.run();
    }
}
