/*
 * Copyright 2015, Red Hat, Inc. and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.zanata.security;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import javax.inject.Inject;

import org.apache.deltaspike.security.api.authorization.AccessDeniedException;
import org.junit.Test;
import org.zanata.ArquillianTest;
import org.zanata.seam.security.AbstractRunAsOperation;
import org.zanata.security.annotations.CheckLoggedIn;
import org.zanata.security.annotations.CheckPermission;
import org.zanata.security.annotations.CheckRole;

/**
 * @author Sean Flanigan <a href="mailto:sflaniga@redhat.com">sflaniga@redhat.com</a>
 */
public class SecurityAnnotationsITCase extends ArquillianTest {

    @Override
    protected void prepareDBUnitOperations() {
    }

    static class SecuredClass {
        boolean calledCheckLoggedIn;
        boolean calledCheckPermission;
        boolean calledCheckRole;
        @CheckLoggedIn
        void checkLoggedIn() {
            calledCheckLoggedIn = true;
        }
        @CheckPermission("some.permission")
        void checkPermission() {
            calledCheckPermission = true;
        }
        @CheckRole("admin")
        void checkRole() {
            calledCheckRole = true;
        }
    }

    @Inject
    private SecuredClass securedClass;

    @Inject
    private ZanataIdentity identity;

    private void login() {
        SimplePrincipal principal = new SimplePrincipal("user");
        identity.acceptExternallyAuthenticatedPrincipal(principal);
    }

    @Test
    public void checkLoggedInAllowed() {
        login();
        securedClass.checkLoggedIn();
        assertTrue(securedClass.calledCheckLoggedIn);
    }

    @Test
    public void checkLoggedInDeniedForAnonymous() {
        try {
            securedClass.checkLoggedIn();
            fail();
        } catch (AccessDeniedException e) {
            // expected exception
            // NB: we can't use @Test(expected = AccessDeniedException.class)
            // because of serialization problems with the exception object
            // in Arquillian.
            assertTrue(true);
        }
    }

    @Test
    public void checkPermissionAllowed() {
        login();
        new AbstractRunAsOperation() {
            public void execute() {
                securedClass.checkPermission();
            }
        }.addRole("admin").run();
        assertTrue(securedClass.calledCheckPermission);
    }

    @Test(expected = AccessDeniedException.class)
    public void checkPermissionDenied() {
        login();
        securedClass.checkPermission();
        fail();
    }

    @Test
    public void checkRoleAllowed() {
        login();
        new AbstractRunAsOperation() {
            public void execute() {
                securedClass.checkRole();
            }
        }.addRole("admin").run();
        assertTrue(securedClass.calledCheckRole);
    }

    @Test(expected = AccessDeniedException.class)
    public void checkRoleDeniedForAnonymous() {
        securedClass.checkRole();
        fail();
    }

    @Test(expected = AccessDeniedException.class)
    public void checkRoleDeniedForNonAdmin() {
        login();
        new AbstractRunAsOperation() {
            public void execute() {
                securedClass.checkRole();
            }
        }.addRole("translator").run();
        fail();
    }

    @Test
    public void checkEverythingAllowedForSystem() {
        new AbstractRunAsOperation(true) {
            public void execute() {
                securedClass.checkLoggedIn();
                securedClass.checkPermission();
                securedClass.checkRole();
            }
        }.run();
        assertTrue(securedClass.calledCheckLoggedIn);
        assertTrue(securedClass.calledCheckPermission);
        assertTrue(securedClass.calledCheckRole);
    }
}
