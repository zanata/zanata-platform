package org.zanata.security;

import javax.faces.event.PhaseEvent;
import javax.servlet.http.HttpServletRequest;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.zanata.ApplicationConfiguration;
import org.zanata.exception.NotLoggedInException;

import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

public class AnonymousUserPermissionCheckerTest {

    private AnonymousUserPermissionChecker checker;
    @Mock private ApplicationConfiguration appConfig;
    @Mock private ZanataIdentity identity;
    @Mock private HttpServletRequest request;
    @Mock private PhaseEvent phaseEvent;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        checker = new AnonymousUserPermissionChecker(appConfig, identity, request);

    }

    @Test
    public void willPassCheckIfItsSignInPage() {
        when(request.getRequestURI()).thenReturn("account/login.xhtml");
        checker.beforePhase(phaseEvent);

        verifyZeroInteractions(identity, appConfig);
    }

    @Test
    public void willPassCheckIfItsRegisterPage() {
        when(request.getRequestURI()).thenReturn("account/register.xhtml");
        checker.beforePhase(phaseEvent);

        verifyZeroInteractions(identity, appConfig);
    }

    @Test
    public void willPassIfAnonymousAccessIsAllowed() {
        when(request.getRequestURI()).thenReturn("home.xhtml");
        when(appConfig.isAnonymousUserAllowed()).thenReturn(true);

        checker.beforePhase(phaseEvent);

        verifyZeroInteractions(identity);
    }

    @Test
    public void willPassIfAnonymousAccessIsDisallowedAndUserIsLoggedIn() {
        when(request.getRequestURI()).thenReturn("home.xhtml");
        when(appConfig.isAnonymousUserAllowed()).thenReturn(false);
        when(identity.isLoggedIn()).thenReturn(true);

        checker.beforePhase(phaseEvent);
        // all good
    }

    @Test(expected = NotLoggedInException.class)
    public void willFailIfAnonymousAccessIsDisallowedAndUserIsNotLoggedIn() {
        when(request.getRequestURI()).thenReturn("home.xhtml");
        when(appConfig.isAnonymousUserAllowed()).thenReturn(false);
        when(identity.isLoggedIn()).thenReturn(false);

        checker.beforePhase(phaseEvent);
    }

}
