/*
 * Copyright 2018, Red Hat, Inc. and individual contributors
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
package org.zanata.action;

import org.apache.deltaspike.core.api.scope.GroupedConversation;
import org.jglue.cdiunit.InRequestScope;
import org.jglue.cdiunit.InSessionScope;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.zanata.dao.AccountActivationKeyDAO;
import org.zanata.exception.ActivationLinkExpiredException;
import org.zanata.exception.KeyNotFoundException;
import org.zanata.model.HAccount;
import org.zanata.model.HAccountActivationKey;
import org.zanata.seam.security.IdentityManager;
import org.zanata.test.CdiUnitRunner;
import org.zanata.ui.faces.FacesMessages;
import org.zanata.util.UrlUtil;

import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import java.util.Date;

import static org.mockito.Mockito.when;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author djansen <a href="mailto:djansen@redhat.com">djansen@redhat.com</a>
 */
@InSessionScope
@InRequestScope
@RunWith(CdiUnitRunner.class)
public class ActivateActionTest {

    @Produces
    @Mock
    private GroupedConversation conversation;
    @Produces
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private AccountActivationKeyDAO accountActivationKeyDAO;
    @Produces
    @Mock
    private IdentityManager identityManager;
    @Produces
    @Mock
    private UrlUtil urlUtil;
    @Produces
    @Mock
    private FacesMessages facesMessages;
    @Inject
    private ActivateAction action;

    @Before
    public void before() {
        action = new ActivateAction(conversation, accountActivationKeyDAO,
                identityManager, urlUtil, facesMessages);
    }

    @Test
    public void nullKeyTest() {
        action.setActivationKey(null);
        try {
            action.validateActivationKey();
            Assert.fail("Expected KeyNotFoundException");
        } catch (KeyNotFoundException knfe) {
            assertThat(knfe.getMessage()).contains("null activation key");
        }
    }

    @Test
    public void keyNoLongerExists() {
        action.setActivationKey("1234567890");
        when(accountActivationKeyDAO.findById(action.getActivationKey(), false))
                .thenReturn(null);
        try {
            action.validateActivationKey();
            Assert.fail("Expected KeyNotFoundException");
        } catch (KeyNotFoundException knfe) {
            assertThat(knfe.getMessage()).contains("activation key: 1234567890");
        }
    }

    @Test
    public void expiredKeyTest() {
        HAccountActivationKey key = new HAccountActivationKey();
        key.setCreationDate(new Date(0L));
        action.setActivationKey("1234567890");
        when(accountActivationKeyDAO.findById(action.getActivationKey(), false))
                .thenReturn(key);
        try {
            action.validateActivationKey();
            Assert.fail("Expected ActivationLinkExpiredException");
        } catch (ActivationLinkExpiredException alee) {
            assertThat(alee.getMessage())
                    .contains("Activation link expired:1234567890");
        }
    }

    @Test
    public void activateTest() {
        HAccountActivationKey key = new HAccountActivationKey();
        key.setCreationDate(new Date());
        HAccount hAccount = new HAccount();
        hAccount.setUsername("Aloy");
        action.setActivationKey("1234567890");
        when(accountActivationKeyDAO.findById(action.getActivationKey(), false))
                .thenReturn(key);
        action.validateActivationKey();
        // TODO ZNTA-2365 action.activate();
    }
}
