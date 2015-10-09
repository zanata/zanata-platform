package org.zanata.rest.editor.service;

import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

import javax.inject.Inject;
import javax.inject.Named;
import org.apache.deltaspike.jpa.api.transaction.Transactional;
import org.zanata.seam.security.ZanataJpaIdentityStore;
import org.zanata.dao.AccountDAO;
import org.zanata.model.HAccount;
import org.zanata.model.HPerson;
import org.zanata.rest.editor.dto.User;
import org.zanata.rest.editor.service.resource.UserResource;
import org.zanata.security.annotations.Authenticated;
import org.zanata.security.annotations.CheckLoggedIn;
import org.zanata.security.annotations.ZanataSecured;
import org.zanata.service.GravatarService;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
@Named("editor.userService")
@Path(UserResource.SERVICE_PATH)
@Transactional
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@ZanataSecured
@CheckLoggedIn
public class UserService implements UserResource {

    @Inject
    @Authenticated
    private HAccount authenticatedAccount;

    @Inject
    private GravatarService gravatarServiceImpl;

    @Inject
    private AccountDAO accountDAO;

    @Override
    public Response getMyInfo() {
        return createUser(authenticatedAccount);
    }

    @Override
    public Response getUserInfo(String username) {
        HAccount account = accountDAO.getByUsername(username);
        return createUser(account);
    }

    private Response createUser(HAccount account) {
        if (account == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        HPerson person = account.getPerson();
        User user =
            new User(account.getUsername(), person.getEmail(),
                person.getName(),
                gravatarServiceImpl.getGravatarHash(person.getEmail()));
        return Response.ok(user).build();
    }
}
