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

import org.jboss.weld.exceptions.WeldException;
import org.jglue.cdiunit.InRequestScope;
import org.jglue.cdiunit.InSessionScope;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.zanata.ApplicationConfiguration;
import org.zanata.dao.AccountDAO;
import org.zanata.dao.PersonDAO;
import org.zanata.exception.AuthorizationException;
import org.zanata.exception.ZanataServiceException;
import org.zanata.i18n.Messages;
import org.zanata.model.HAccount;
import org.zanata.model.HAccountActivationKey;
import org.zanata.model.HPerson;
import org.zanata.security.AuthenticationType;
import org.zanata.security.ZanataIdentity;
import org.zanata.security.ZanataOpenId;
import org.zanata.security.annotations.Authenticated;
import org.zanata.service.EmailService;
import org.zanata.service.RegisterService;
import org.zanata.test.CdiUnitRunner;
import org.zanata.ui.faces.FacesMessages;
import org.zanata.util.UrlUtil;

import javax.enterprise.inject.Produces;
import javax.inject.Inject;

import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@InRequestScope
@InSessionScope
@RunWith(CdiUnitRunner.class)
public class NewProfileActionTest {

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    @Produces
    private ZanataOpenId zanataOpenId;

    @Mock
    @Produces
    private EmailService emailServiceImpl;

    @Mock
    @Produces
    private UrlUtil urlUtil;

    @Mock
    @Produces
    Messages msgs;

    @Mock
    @Produces
    RegisterService registerServiceImpl;

    @Mock
    @Produces
    ApplicationConfiguration applicationConfiguration;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    @Produces
    @Authenticated
    HAccount authenticatedAccount;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    @Produces
    ZanataIdentity identity;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    @Produces
    private FacesMessages facesMessages;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    @Produces
    AccountDAO accountDAO;


    @Mock
    @Produces
    PersonDAO personDAO;

    @Inject
    NewProfileAction newProfileAction;

    @Rule
    public ExpectedException expectedException(){
        return ExpectedException.none();
    }

    @Before
    public void before() {
        msgs = new Messages(new Locale("en"));
    }

    @Test
    public void testCreateUserAction() {
        String username = "aloy";
        String name = "Aloy";
        String email = "aloy@example.com";

        HAccountActivationKey key = new HAccountActivationKey();
        key.setKeyHash("0123456789");


        when(identity.isPreAuthenticated()).thenReturn(true);
        when(accountDAO.getByUsername(username)).thenReturn(authenticatedAccount);
        when(zanataOpenId.getAuthResult().getAuthenticatedId()).thenReturn("id");

        when(registerServiceImpl.register(username, "id", AuthenticationType.OPENID, name,
                email)).thenReturn(key.toString());

        newProfileAction.setActivationKey(key.toString());

        when(emailServiceImpl.sendActivationEmail(name, email, newProfileAction.getActivationKey()))
                .thenReturn(msgs.get("jsf.Account.ActivationMessage"));

        newProfileAction.setUsername(username);
        newProfileAction.setName(name);
        newProfileAction.setEmail(email);

        assertThat(newProfileAction.getName()).isEqualTo(name);
        assertThat(newProfileAction.getEmail()).isEqualTo(email);
        assertThat(newProfileAction.getUsername()).isEqualTo(username);
        assertThat(newProfileAction.getUsernameMaxLength())
                .isEqualTo(HasUserDetail.USERNAME_MAX_LENGTH);
        assertThat(newProfileAction.createUser()).isEqualTo("success");

        when(identity.getCredentials().getAuthType()).thenReturn(AuthenticationType.JAAS);
        verify(registerServiceImpl, times(1)).register(username, "id",
                AuthenticationType.OPENID, name, email);
        verify(identity).unAuthenticate();
        verify(facesMessages).addGlobal(msgs.get("jsf.Account.ActivationMessage"));

    }

    @Test
    public void validateEmailTaken() {
        String email = "aloy@redhat.com";
        HPerson person = new HPerson();
        person.setAccount(new HAccount());
        when(identity.isPreAuthenticated()).thenReturn(true);
        when(personDAO.findByEmail(email)).thenReturn(person);

        newProfileAction.validateEmail(email);

        verify(facesMessages).addToControl("email", "This email address is already taken");
    }

    @Test
    public void tryToCreateEmailTaken() {
        String email = "aloy@redhat.com";
        HPerson person = new HPerson();
        person.setAccount(new HAccount());
        when(identity.isPreAuthenticated()).thenReturn(true);
        when(personDAO.findByEmail(email)).thenReturn(person);
        newProfileAction.setEmail(email);

        assertThat(newProfileAction.createUser()).isEqualTo("failure");
        verify(facesMessages, times(2)).addToControl("email", "This email address is already taken");
    }

    @Test
    public void tryToCreateUsernameTaken() {
        String username = "aloy";
        HPerson person = new HPerson();
        person.setAccount(new HAccount());
        when(identity.isPreAuthenticated()).thenReturn(true);
        when(accountDAO.getByUsername(username)).thenReturn(person.getAccount());
        newProfileAction.setUsername(username);

        assertThat(newProfileAction.createUser()).isEqualTo("failure");
        verify(facesMessages, times(2)).addToControl("username", "This username is already taken");
    }

    @Test
    public void kerberosJAASNameIsUsername() {
        String username = "aloy";
        String email = "aloy@example.com";
        when(identity.getCredentials().getAuthType()).thenReturn(AuthenticationType.KERBEROS);

        when(identity.isPreAuthenticated()).thenReturn(true);
        when(accountDAO.getByUsername(username)).thenReturn(authenticatedAccount);

        newProfileAction.setUsername(username);
        newProfileAction.setEmail(email);

        assertThat(newProfileAction.createUser()).isEqualTo("success");

        when(identity.getCredentials().getAuthType()).thenReturn(AuthenticationType.JAAS);
        assertThat(newProfileAction.createUser()).isEqualTo("success");
        verify(registerServiceImpl, times(2)).register(username, username, email);
    }

    @Test
    public void useDetailsFromOpenIDProvider() {
        String username = "aloy";
        String email = "aloy@example.com";
        String name = "Aloy";
        when(identity.getCredentials().getAuthType()).thenReturn(AuthenticationType.OPENID);
        when(zanataOpenId.getAuthResult().getAuthenticatedId()).thenReturn("id");

        when(identity.isPreAuthenticated()).thenReturn(true);
        when(accountDAO.getByUsername(username)).thenReturn(authenticatedAccount);

        newProfileAction.setUsername(username);
        newProfileAction.setEmail(email);
        newProfileAction.setName(name);

        assertThat(newProfileAction.createUser()).isEqualTo("success");

        verify(registerServiceImpl, times(1)).register(username, "id",
                AuthenticationType.OPENID, name, email);
    }

    @Test
    public void blankEnforcedUsernamesFails() {
        when(applicationConfiguration.isEnforceMatchingUsernames()).thenReturn(true);
        when(zanataOpenId.getAuthResult().getUsername()).thenReturn("");
        when(identity.isPreAuthenticated()).thenReturn(true);
        when(identity.getCredentials().getAuthType()).thenReturn(AuthenticationType.OPENID);
        try {
            newProfileAction.setUsername("");
            fail("Weld Exception not thrown");
        } catch (WeldException we) {
            Throwable exception = we.getCause().getCause();
            assertThat(exception).isInstanceOf(ZanataServiceException.class);
            assertThat(exception.getMessage()).isEqualTo(
                    msgs.get("jsf.register.EnforcedUserIsBlank"));
        }
    }

    @Test
    public void takenEnforcedUsernamesFails() {
        String username = "aloy";
        HPerson person = new HPerson();
        person.setAccount(new HAccount());
        when(identity.isPreAuthenticated()).thenReturn(true);
        when(accountDAO.getByUsername(username)).thenReturn(person.getAccount());
        when(identity.getCredentials().getAuthType()).thenReturn(AuthenticationType.OPENID);
        when(applicationConfiguration.isEnforceMatchingUsernames()).thenReturn(true);
        when(zanataOpenId.getAuthResult().getUsername()).thenReturn(username);

        assertThatThrownBy(() -> newProfileAction.setUsername(username))
                .isInstanceOf(WeldException.class)
                .hasRootCauseExactlyInstanceOf(ZanataServiceException.class)
                .hasStackTraceContaining(
                        msgs.format("jsf.register.EnforcedUserIsTaken", username));
    }

    @Test
    public void invalidEnforcedUsernamesFails() {
        String username = "not$valid";
        when(identity.isPreAuthenticated()).thenReturn(true);
        when(accountDAO.getByUsername(username)).thenReturn(authenticatedAccount);
        when(identity.getCredentials().getAuthType()).thenReturn(AuthenticationType.OPENID);
        when(applicationConfiguration.isEnforceMatchingUsernames()).thenReturn(true);
        when(zanataOpenId.getAuthResult().getUsername()).thenReturn(username);

        assertThatThrownBy(() -> newProfileAction.setUsername(username))
                .isInstanceOf(WeldException.class)
                .hasRootCauseExactlyInstanceOf(ZanataServiceException.class)
                .hasStackTraceContaining(msgs
                        .format("jsf.register.EnforcedUserNotValid", username,
                                NewProfileAction.USERNAME_REGEX));
    }

    @Test
    public void isReadOnlyUsername() {
        when(applicationConfiguration.isEnforceMatchingUsernames()).thenReturn(true);
        when(identity.isPreAuthenticated()).thenReturn(true);
        assertThat(newProfileAction.isReadOnlyUsername()).isTrue();
    }

    @Test
    public void preAuthenticationIsRequired() {
        assertThatThrownBy(() -> newProfileAction.setUsername("test"))
                .isInstanceOf(WeldException.class)
                .hasRootCauseExactlyInstanceOf(AuthorizationException.class)
                .hasStackTraceContaining("Need to be in pre authenticated state");
    }
}
