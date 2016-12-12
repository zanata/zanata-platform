package org.zanata.security;

import javax.faces.event.PhaseEvent;
import javax.servlet.http.HttpServletRequest;

import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.zanata.ApplicationConfiguration;
import org.zanata.exception.NotLoggedInException;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

public class AnonymousAccessControlPhaseListenerTest {

    private AnonymousAccessControlPhaseListener checker;
    @Mock private ApplicationConfiguration appConfig;
    @Mock private ZanataIdentity identity;
    @Mock private HttpServletRequest request;
    @Mock private PhaseEvent phaseEvent;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        checker = new AnonymousAccessControlPhaseListener(appConfig, identity, request);

    }

    @Test
    public void anonymousAccessToLoginPageIsAllowed() {
        when(request.getRequestURI()).thenReturn("account/login.xhtml");
        checker.beforePhase(phaseEvent);

        verifyZeroInteractions(identity, appConfig);
    }

    @Test
    public void anonymousAccessToRegisterPageIsAllowed() {
        when(request.getRequestURI()).thenReturn("account/register.xhtml");
        checker.beforePhase(phaseEvent);

        verifyZeroInteractions(identity, appConfig);
    }

    @Test
    public void anonymousAccessToUnprotectedPageIsAllowed() {
        when(request.getRequestURI()).thenReturn("home.xhtml");
        when(appConfig.isAnonymousUserAllowed()).thenReturn(true);

        checker.beforePhase(phaseEvent);

        verifyZeroInteractions(identity);
    }

    @Test
    public void loggedInAccessToProtectedPageIsAllowed() {
        when(request.getRequestURI()).thenReturn("home.xhtml");
        when(appConfig.isAnonymousUserAllowed()).thenReturn(false);
        when(identity.isLoggedIn()).thenReturn(true);

        checker.beforePhase(phaseEvent);
        // all good
    }

    @Test
    public void anonymousAccessToProtectedPageIsDenied() {
        when(request.getRequestURI()).thenReturn("home.xhtml");
        when(appConfig.isAnonymousUserAllowed()).thenReturn(false);
        when(identity.isLoggedIn()).thenReturn(false);

        assertThatThrownBy(() -> checker.beforePhase(phaseEvent))
                .isInstanceOf(NotLoggedInException.class);

    }

}
