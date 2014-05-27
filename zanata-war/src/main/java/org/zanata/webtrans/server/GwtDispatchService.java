package org.zanata.webtrans.server;

import javax.servlet.ServletException;

import lombok.extern.slf4j.Slf4j;
import net.customware.gwt.dispatch.shared.Action;
import net.customware.gwt.dispatch.shared.ActionException;
import net.customware.gwt.dispatch.shared.Result;

import org.jboss.seam.servlet.ContextualHttpServletRequest;
import org.zanata.util.ServiceLocator;
import org.zanata.webtrans.shared.DispatchService;

import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;

/**
 * This is the GWT RPC endpoint for all GWT RPC calls. GWT will create client
 * stub with interface name XXXAsync and bind server class that implements XXX.
 * In our case the XXX is DispatchService.
 *
 * This class extends GWT provided RemoteServiceServlet which handles
 * serialization of request and response of RPC calls. It will delegate the
 * actual call to SeamDispatch based on generic type information.
 *
 * @see org.zanata.webtrans.client.rpc.SeamDispatchAsync
 * @see org.zanata.webtrans.shared.DispatchServiceAsync
 * @see com.google.gwt.user.server.rpc.RemoteServiceServlet
 * @see SeamDispatch
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
        } catch (ServletException e) {
            Throwable cause = e.getCause();
            // ActionException is under shared package which is serializable by
            // GWT.
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
