package org.zanata.rest.editor.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
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
import org.zanata.security.ZanataIdentity;
import org.zanata.service.GravatarService;

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
    private ApplicationConfiguration applicationConfiguration;
    private HPerson person;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        authenticatedAccount = new HAccount();
        person = new HPerson();
        person.setId(1L);
        person.setName("peter");
        person.setEmail("pan@wonderland");
        person.setAccount(authenticatedAccount);
        authenticatedAccount.setPerson(person);
        service =
                new UserService(authenticatedAccount, gravatarService,
                        accountDAO, personDAO, projectDAO, identity,
                        applicationConfiguration);
    }

    @Test
    public void getMyInfoWillReturnNotFoundIfNotAuthenticated() {
        service = new UserService(null, gravatarService, accountDAO, personDAO,
                projectDAO, identity, applicationConfiguration);
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
        when(accountDAO.getByUsername("a")).thenReturn(null);
        Response response = service.getUserInfo("a");
        assertThat(response.getStatus()).isEqualTo(404);
    }

    @Test
    public void getUserInfoWillReturnInfoAboutThePerson() {
        when(accountDAO.getByUsername("a")).thenReturn(person.getAccount());
        when(personDAO.findById(person.getId())).thenReturn(person);
        when(gravatarService.getUserImageUrl(GravatarService.USER_IMAGE_SIZE,
            person.getEmail())).thenReturn("imageurl");

        testUser(false);
    }

    @Test
    public void getUserInfoWillReturnInfoAboutThePersonWithoutEmail() {
        when(accountDAO.getByUsername("a")).thenReturn(person.getAccount());
        when(personDAO.findById(person.getId())).thenReturn(person);
        when(gravatarService.getUserImageUrl(GravatarService.USER_IMAGE_SIZE,
            person.getEmail())).thenReturn("imageurl");

        testUser(true);
    }

    private void testUser(boolean includeEmail) {
        when(applicationConfiguration.isDisplayUserEmail()).thenReturn(includeEmail);
        Response response = service.getUserInfo("a");
        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.getEntity()).isInstanceOf(User.class);

        User user = (User) response.getEntity();
        if(includeEmail) {
            assertThat(user.getEmail()).isEqualTo(person.getEmail());
        } else {
            assertThat(user.getEmail()).isEqualTo(null);
        }
        assertThat(user.getName()).isEqualTo(person.getName());
    }
}
