package org.zanata.webtrans.shared.rpc;

import java.util.List;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.collect.Lists;

/**
 * @author Patrick Huang <a
 *         href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
public class RemoteLoggingAction extends AbstractWorkspaceAction<NoOpResult> {
    private String message;
    private List<String> contextInfo = Lists.newArrayList();

    @SuppressWarnings("unused")
    public RemoteLoggingAction() {
    }

    /**
     * We only log as ERROR in the handler.
     *
     * @param message
     *            log message
     * @see org.zanata.webtrans.server.rpc.RemoteLoggingHandler
     */
    public RemoteLoggingAction(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public List<String> getContextInfo() {
        return contextInfo;
    }

    public RemoteLoggingAction addContextInfo(String contextName,
            Object contextValue) {
        contextInfo.add(MoreObjects.toStringHelper("")
                .add(contextName, contextValue).toString());
        return this;
    }
}
