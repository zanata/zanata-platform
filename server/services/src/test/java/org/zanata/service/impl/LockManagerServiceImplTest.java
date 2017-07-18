/*
 * Copyright 2010, Red Hat, Inc. and individual contributors as indicated by the
 * @author tags. See the copyright.txt file in the distribution for a full
 * listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this software; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
 * site: http://www.fsf.org.
 */
package org.zanata.service.impl;

import org.jglue.cdiunit.deltaspike.SupportDeltaspikeCore;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.zanata.ZanataTest;
import org.zanata.lock.Lock;
import org.zanata.model.HAccount;
import org.zanata.security.annotations.Authenticated;
import org.zanata.test.CdiUnitRunner;
import org.zanata.util.IServiceLocator;
import org.zanata.util.ServiceLocator;

import javax.enterprise.inject.Produces;
import javax.inject.Inject;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

/**
 * This service does not inject other components, so it can be tested without a
 * Seam environment.
 *
 * @author Carlos Munoz <a
 *         href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 */
@RunWith(CdiUnitRunner.class)
@SupportDeltaspikeCore
public class LockManagerServiceImplTest extends ZanataTest {

    @Inject
    private LockManagerServiceImpl lockManagerService;

    @Produces
    IServiceLocator serviceLocator = ServiceLocator.instance();

    @Produces @Authenticated
    HAccount getAuthenticatedAccount() {
        HAccount account = new HAccount();
        account.setUsername("admin");
        return account;
    }

    @Test
    public void simpleLock() {
        Lock l1 = new Lock("prop1", "prop2", "prop3");

        boolean lockAquired = lockManagerService.checkAndAttain(l1);

        assertThat(lockAquired, is(true));

        lockManagerService.release(l1);
        lockAquired = lockManagerService.checkAndAttain(l1);

        assertThat(lockAquired, is(true));
        lockManagerService.release(l1);
    }

    @Test
    public void deniedLock() {
        Lock l1 = new Lock("prop1", "prop2", "prop3");
        Lock l1Eq = new Lock("prop1", "prop2", "prop3");
        Lock l2 = new Lock("prop1", "prop2");

        assertThat(l1, equalTo(l1Eq));

        boolean lockAquired = lockManagerService.checkAndAttain(l1);
        assertThat(lockAquired, is(true));

        // Same lock, should not aquire
        lockAquired = lockManagerService.checkAndAttain(l1Eq);
        assertThat(lockAquired, is(false));

        // different lock
        lockAquired = lockManagerService.checkAndAttain(l2);
        assertThat(lockAquired, is(true));

        lockManagerService.release(l1Eq);
        lockAquired = lockManagerService.checkAndAttain(l1Eq);
        assertThat(lockAquired, is(true));

        lockManagerService.release(l1Eq);
        lockManagerService.release(l2);
    }
}
