package org.zanata.rest.editor.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import javax.ws.rs.core.Response;

import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.zanata.dao.AccountDAO;
import org.zanata.model.HAccount;
import org.zanata.model.HPerson;
import org.zanata.rest.editor.dto.User;
import org.zanata.service.GravatarService;

@Test(groups = "unit-tests")
public class UserServiceTest {
    private UserService service;
    @Mock
    private AccountDAO accountDAO;
    private HAccount authenticatedAccount;
    @Mock
    private GravatarService gravatarService;
    private HPerson person;

    @BeforeMethod
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        authenticatedAccount = new HAccount();
        person = new HPerson();
        person.setName("peter");
        person.setEmail("pan@wonderland");
        person.setAccount(authenticatedAccount);
        authenticatedAccount.setPerson(person);
        service =
                new UserService(authenticatedAccount, gravatarService,
                        accountDAO);
    }

    @Test
    public void getMyInfoWillReturnNotFoundIfNotAuthenticated() {
        service = new UserService(null, gravatarService, accountDAO);
        Response response = service.getMyInfo();
        assertThat(response.getStatus()).isEqualTo(404);
    }

    @Test
    public void getMyInfoWillReturnInfoAboutAuthenticatedPerson() {
        Response response = service.getMyInfo();
        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.getEntity()).isInstanceOf(User.class);

        User user = (User) response.getEntity();
        assertThat(user.getEmail()).isEqualTo(person.getEmail());
        assertThat(user.getName()).isEqualTo(person.getName());

        verify(gravatarService).getGravatarHash(person.getEmail());
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

        Response response = service.getUserInfo("a");
        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.getEntity()).isInstanceOf(User.class);

        User user = (User) response.getEntity();
        assertThat(user.getEmail()).isEqualTo(person.getEmail());
        assertThat(user.getName()).isEqualTo(person.getName());

        verify(gravatarService).getGravatarHash(person.getEmail());
    }
}
