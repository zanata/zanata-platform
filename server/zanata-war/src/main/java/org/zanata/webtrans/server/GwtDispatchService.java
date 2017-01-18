package org.zanata.webtrans.server;

import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import net.customware.gwt.dispatch.shared.Action;
import net.customware.gwt.dispatch.shared.Result;
import org.slf4j.Logger;
import org.zanata.security.ZanataIdentity;
import org.zanata.webtrans.shared.DispatchService;

import javax.inject.Inject;
import javax.servlet.annotation.WebServlet;

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
@WebServlet(urlPatterns = "/seam/resource/gwt")
@RemoteServiceRelativePath("seam/resource/gwt")
public class GwtDispatchService extends RemoteServiceServlet implements
        DispatchService {
    private static final Logger log =
            org.slf4j.LoggerFactory.getLogger(GwtDispatchService.class);
    @Inject
    private SeamDispatch dispatch;

    @Inject
    private ZanataIdentity identity;

    @Override
    public Result execute(final Action<?> action) throws Exception {
        return dispatch.execute(action);
    }

    @Override
    public void rollback(final Action<Result> action, final Result result)
            throws Exception {
        dispatch.rollback(action, result);
    }
}
