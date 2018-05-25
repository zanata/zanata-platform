/*
 * Copyright 2017, Red Hat, Inc. and individual contributors
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

import org.jglue.cdiunit.InRequestScope;
import org.jglue.cdiunit.InSessionScope;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.zanata.email.VelocityEmailStrategy;
import org.zanata.i18n.Messages;
import org.zanata.model.HAccount;
import org.zanata.security.annotations.Authenticated;
import org.zanata.service.EmailService;
import org.zanata.test.CdiUnitRunner;
import org.zanata.ui.faces.FacesMessages;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;


/**
 * @author spathare <a href="mailto:spathare@redhat.com">spathare@redhat.com</a>
 */

@InSessionScope
@InRequestScope
@RunWith(CdiUnitRunner.class)
public class ContactAdminActionTest {

    @Mock
    @Produces
    private FacesMessages facesMessages;

    @Mock
    @Produces
    private Messages msgs;

    @Mock
    @Produces
    private EmailService emailServiceImpl;

    @Produces
    @Authenticated
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private HAccount authenticatedAccount;

    @Inject
    ContactAdminAction contactAdminAction;

    @Test
    public void testSendByRegisterUser() {
        String sub = "Contact admin subject";
        when(authenticatedAccount.getUsername()).thenReturn("Joy");
        when(emailServiceImpl.sendToAdmins(any(VelocityEmailStrategy.class), eq(null))).thenReturn(sub);
        String mesg = "Hello this is message for admin";
        contactAdminAction.setSubject(sub);
        contactAdminAction.setMessage(mesg);
        authenticatedAccount.setUsername("tester");
        contactAdminAction.send();
        assertThat(contactAdminAction.getMessage()).isEqualTo(mesg);
        verify(facesMessages, times(1)).addGlobal(sub);
    }
}
