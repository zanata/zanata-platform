package org.zanata.rest.editor.service;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import javax.ws.rs.core.Response;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.zanata.ApplicationConfiguration;
import org.zanata.dao.AccountDAO;
import org.zanata.dao.PersonDAO;
import org.zanata.dao.ProjectDAO;
import org.zanata.model.HAccount;
import org.zanata.model.HPerson;
import org.zanata.rest.dto.User;
import org.zanata.seam.security.IdentityManager;
import org.zanata.security.ZanataIdentity;
import org.zanata.service.GravatarService;

import com.google.common.collect.Lists;

public class UserServiceTest {
    private UserService service;
    @Mock
    private AccountDAO accountDAO;
    @Mock
    private PersonDAO personDAO;
    @Mock
    private ProjectDAO projectDAO;
    private HAccount authenticatedAccount;
    @Mock
    private GravatarService gravatarService;
    @Mock
    private ZanataIdentity identity;
    @Mock
    private IdentityManager identityManager;
    @Mock
    private ApplicationConfiguration applicationConfiguration;
    private HPerson person;

    private String username = "a";

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        authenticatedAccount = new HAccount();
        authenticatedAccount.setUsername(username);
        person = new HPerson();
        person.setId(1L);
        person.setName("peter");
        person.setEmail("pan@wonderland");
        person.setAccount(authenticatedAccount);
        authenticatedAccount.setPerson(person);
        service =
                new UserService(authenticatedAccount, gravatarService,
                        accountDAO, personDAO, projectDAO, identity,
                        applicationConfiguration, identityManager);
    }

    @Test
    public void getMyInfoWillReturnNotFoundIfNotAuthenticated() {
        service = new UserService(null, gravatarService, accountDAO, personDAO,
                projectDAO, identity, applicationConfiguration, identityManager);
        Response response = service.getMyInfo();
        assertThat(response.getStatus()).isEqualTo(404);
    }

    @Test
    public void getMyInfoWillReturnInfoAboutAuthenticatedPerson() {
        when(personDAO.findById(person.getId())).thenReturn(person);
        when(gravatarService.getUserImageUrl(GravatarService.USER_IMAGE_SIZE,
                person.getEmail())).thenReturn("imageurl");

        Response response = service.getMyInfo();
        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.getEntity()).isInstanceOf(User.class);

        User user = (User) response.getEntity();
        assertThat(user.getEmail()).isEqualTo(person.getEmail());
        assertThat(user.getName()).isEqualTo(person.getName());
    }

    @Test
    public void getUserInfoWillReturnNotFoundIfNotFound() {
        when(accountDAO.getByUsername(username)).thenReturn(null);
        Response response = service.getUserInfo(username);
        assertThat(response.getStatus()).isEqualTo(404);
    }

    @Test
    public void getUserInfoWillReturnInfoAboutThePerson() {
        when(accountDAO.getByUsername(username)).thenReturn(person.getAccount());
        when(personDAO.findById(person.getId())).thenReturn(person);
        when(gravatarService.getUserImageUrl(GravatarService.USER_IMAGE_SIZE,
            person.getEmail())).thenReturn("imageurl");

        testUser(false);
    }

    @Test
    public void getUserInfoWillReturnInfoAboutThePersonWithoutEmail() {
        when(accountDAO.getByUsername(username)).thenReturn(person.getAccount());
        when(personDAO.findById(person.getId())).thenReturn(person);
        when(gravatarService.getUserImageUrl(GravatarService.USER_IMAGE_SIZE,
            person.getEmail())).thenReturn("imageurl");

        testUser(true);
    }

    private void testUser(boolean includeEmail) {
        when(applicationConfiguration.isDisplayUserEmail()).thenReturn(includeEmail);
        List<String> roles = Lists.newArrayList("user", "admin", "project-creator");
        when(identityManager.getGrantedRoles(username)).thenReturn(roles);

        Response response = service.getUserInfo(username);
        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.getEntity()).isInstanceOf(User.class);

        User user = (User) response.getEntity();
        if(includeEmail) {
            assertThat(user.getEmail()).isEqualTo(person.getEmail());
        } else {
            assertThat(user.getEmail()).isEqualTo(null);
        }
        assertThat(user.getName()).isEqualTo(person.getName());
        assertThat(user.getUsername()).isEqualTo(username);
        assertThat(user.getRoles()).isEqualTo(roles);
    }
}
