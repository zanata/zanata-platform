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
import org.zanata.dao.AccountActivationKeyDAO;
import org.zanata.dao.AccountDAO;
import org.zanata.dao.PersonDAO;
import org.zanata.i18n.Messages;
import org.zanata.model.HPerson;
import org.zanata.seam.security.IdentityManager;
import org.zanata.service.EmailService;
import org.zanata.service.UserAccountService;
import org.zanata.test.CdiUnitRunner;
import org.zanata.ui.faces.FacesMessages;

import javax.enterprise.inject.Produces;
import javax.inject.Inject;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

/**
 * @author djansen <a href="mailto:djansen@redhat.com">djansen@redhat.com</a>
 */
@InSessionScope
@InRequestScope
@RunWith(CdiUnitRunner.class)
public class UserActionTest {
    @Produces
    @Mock
    private IdentityManager identityManager;
    @Produces
    @Mock
    private FacesMessages facesMessages;
    @Produces
    @Mock
    private Messages msgs;
    @Produces
    @Mock
    private UserAccountService userAccountServiceImpl;
    @Produces
    @Mock
    private EmailService emailServiceImpl;
    @Produces
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private PersonDAO personDAO;
    @Produces
    @Mock
    private AccountDAO accountDAO;
    @Produces
    @Mock
    private AccountActivationKeyDAO accountActivationKeyDAO;

    @Inject
    private UserAction userAction;

    @Test
    public void changeUserFullName() {
        HPerson person = new HPerson();
        person.setName("Aloy");
        HPerson newPerson = new HPerson();
        newPerson.setName("Aloy S");
        when(personDAO.findByUsername("aloy")).thenReturn(person);

        userAction.setUsername("aloy");
        userAction.loadUser();
        userAction.setName("Aloy S");

        //when(identityManager.isUserEnabled("aloy")).thenReturn(true);

        assertThat(userAction.getOriginalName()).isEqualTo("Aloy");
        assertThat(userAction.save()).isEqualTo("success");
        verify(personDAO, times(1)).makePersistent(newPerson);
    }
}
