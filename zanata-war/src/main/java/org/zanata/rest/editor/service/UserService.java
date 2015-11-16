package org.zanata.rest.editor.service;

import java.util.Set;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

import org.apache.commons.lang.StringUtils;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Transactional;
import org.zanata.seam.security.ZanataJpaIdentityStore;
import org.zanata.dao.AccountDAO;
import org.zanata.dao.PersonDAO;
import org.zanata.model.HAccount;
import org.zanata.model.HLocale;
import org.zanata.model.HPerson;
import org.zanata.rest.editor.dto.User;
import org.zanata.rest.editor.service.resource.UserResource;
import org.zanata.security.annotations.CheckLoggedIn;
import org.zanata.security.annotations.ZanataSecured;
import org.zanata.service.GravatarService;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.Collections2;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
@Name("editor.userService")
@Path(UserResource.SERVICE_PATH)
@Transactional
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@ZanataSecured
@CheckLoggedIn
public class UserService implements UserResource {

    @In(value = ZanataJpaIdentityStore.AUTHENTICATED_USER, required = false)
    private HAccount authenticatedAccount;

    @In
    private GravatarService gravatarServiceImpl;

    @In
    private AccountDAO accountDAO;

    @In
    private PersonDAO personDAO;

    @Override
    public Response getMyInfo() {
        if(authenticatedAccount == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        User user = transferToUser(authenticatedAccount);
        return Response.ok(user).build();
    }

    @Override
    public Response getUserInfo(String username) {
        User user = generateUser(username);
        if (user == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.ok(user).build();
    }

    @Override
    public User generateUser(String username) {
        if(StringUtils.isBlank(username)) {
            return null;
        }
        HAccount account = accountDAO.getByUsername(username);
        if(account == null) {
            return null;
        }
        return transferToUser(account);
    }

    @Override
    public User transferToUser(HAccount account) {
        if(account == null) {
            return new User();
        }
        //need to use dao to load entity due to lazy loading of languageMemberships
        HPerson person = personDAO.findById(account.getPerson().getId());

        String email = person.getEmail();

        String userImageUrl = gravatarServiceImpl
            .getUserImageUrl(GravatarService.USER_IMAGE_SIZE, email);

        String userLanguageTeams =
            getUserLanguageTeams(person.getLanguageMemberships());

        return new User(account.getUsername(), email, person.getName(),
                gravatarServiceImpl.getGravatarHash(email), userImageUrl,
                userLanguageTeams, true);
    }

    private String getUserLanguageTeams(Set<HLocale> languageMemberships) {
        return Joiner.on(", ").skipNulls().join(
                Collections2.transform(languageMemberships, languageNameFn));
    }

    private final Function<HLocale, String> languageNameFn =
            new Function<HLocale, String>() {
                @Override
                public String apply(HLocale locale) {
                    return locale.retrieveDisplayName();
                }
            };
}
