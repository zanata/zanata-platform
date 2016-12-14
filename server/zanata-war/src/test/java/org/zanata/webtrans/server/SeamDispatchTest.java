package org.zanata.webtrans.server;

import javax.enterprise.inject.Instance;
import javax.inject.Provider;
import javax.servlet.http.HttpServletRequest;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.zanata.exception.NotLoggedInException;
import org.zanata.security.ZanataIdentity;
import org.zanata.webtrans.server.rpc.AbstractActionHandler;
import org.zanata.webtrans.shared.rpc.ActivateWorkspaceAction;
import org.zanata.webtrans.shared.rpc.ActivateWorkspaceResult;
import org.zanata.webtrans.shared.rpc.WrappedAction;
import net.customware.gwt.dispatch.shared.ActionException;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

public class SeamDispatchTest {

    private SeamDispatch dispatch;
    @Mock
    private HttpServletRequest request;
    @Mock
    private Provider<Boolean> allowAnonymous;
    @Mock
    private ZanataIdentity identity;
    @Mock
    private Instance<AbstractActionHandler<?, ?>> actionHandlers;
    @Mock
    private WrappedAction<ActivateWorkspaceResult> wrappedAction;
    @Mock private Instance<AbstractActionHandler<?, ?>> handlerInstance;
    @Mock private ActivateWorkspaceAction action;
    @Mock private AbstractActionHandler handler;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        dispatch = new SeamDispatch(request, allowAnonymous, identity,
                actionHandlers);
        when(actionHandlers.select(any())).thenReturn(handlerInstance);
        when(handlerInstance.isAmbiguous()).thenReturn(false);
        when(handlerInstance.isUnsatisfied()).thenReturn(false);
        when(wrappedAction.getAction()).thenReturn(action);
        when(handlerInstance.get()).thenReturn(handler);
        doThrow(new NotLoggedInException()).when(identity).checkLoggedIn();
    }

    @Test
    public void willCheckLogInOnExecuteWhenAnonymousAccessIsBlocked()
            throws ActionException {
        when(allowAnonymous.get()).thenReturn(false);

        assertThatThrownBy(() -> dispatch.execute(wrappedAction))
                .isInstanceOf(NotLoggedInException.class);

    }

    @Test
    public void willNotCheckLogInOnExecuteWhenAnonymousAccessIsAllowed()
            throws ActionException {
        when(allowAnonymous.get()).thenReturn(true);

        dispatch.execute(wrappedAction);

        // no NotLoggedInException
    }

    @Test
    public void willCheckLogInOnRollBackWhenAnonymousAccessIsBlocked()
            throws ActionException {
        when(allowAnonymous.get()).thenReturn(false);

        assertThatThrownBy(() -> dispatch.rollback(wrappedAction, null))
                .isInstanceOf(NotLoggedInException.class);

    }

    @Test
    public void willNotCheckLogInOnRollBackWhenAnonymousAccessIsAllowed()
            throws ActionException {
        when(allowAnonymous.get()).thenReturn(true);

        dispatch.rollback(wrappedAction, null);

        // no NotLoggedInException
    }

}
