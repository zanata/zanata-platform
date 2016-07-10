package org.zanata.rest.editor.service;

import java.util.List;
import java.util.stream.Collectors;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

import javax.inject.Named;
import org.apache.deltaspike.jpa.api.transaction.Transactional;
import org.apache.commons.lang.StringUtils;
import org.zanata.ApplicationConfiguration;
import org.zanata.dao.AccountDAO;
import org.zanata.dao.PersonDAO;
import org.zanata.model.HAccount;
import org.zanata.model.HLocale;
import org.zanata.model.HPerson;
import org.zanata.rest.dto.Account;
import org.zanata.rest.dto.User;
import org.zanata.rest.editor.dto.Permission;
import org.zanata.rest.editor.service.resource.UserResource;
import org.zanata.rest.service.AccountService;
import org.zanata.security.ZanataIdentity;
import org.zanata.security.annotations.Authenticated;
import org.zanata.security.annotations.CheckLoggedIn;
import org.zanata.service.GravatarService;

import com.google.common.base.Strings;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
@RequestScoped
@Named("editor.userService")
@Path(UserResource.SERVICE_PATH)
@Transactional(readOnly = true)
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class UserService implements UserResource {

    @Inject
    @Authenticated
    private HAccount authenticatedAccount;

    @Inject
    private GravatarService gravatarServiceImpl;

    @Inject
    private AccountDAO accountDAO;

    @Inject
    private PersonDAO personDAO;

    @Inject
    private ZanataIdentity identity;

    @Inject
    private ApplicationConfiguration applicationConfiguration;

    @Override
    @CheckLoggedIn
    public Response getMyInfo() {
        if(authenticatedAccount == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        User user = getUserInfo(authenticatedAccount, true);
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
    public Response getAccountDetails() {
        if(authenticatedAccount == null) {
            return Response.status(Response.Status.FORBIDDEN).build();
        }
        HAccount account = accountDAO.getByUsername(authenticatedAccount.getUsername());
        // we may not need to return apiKey (and generating it
        // without asking) anymore once client switched to OAuth
        if (Strings.isNullOrEmpty(authenticatedAccount.getApiKey())) {
            accountDAO.createApiKey(account);
        }
        Account dto = new Account();
        AccountService.getAccountDetails(account, dto);
        return Response.ok(dto).build();
    }

    /**
     * Generate {@link org.zanata.rest.dto.User} object from username
     *
     * @param username - username in HPerson
     */
    private User generateUser(String username) {
        if (StringUtils.isBlank(username)) {
            return null;
        }
        HAccount account = accountDAO.getByUsername(username);
        if (account == null) {
            return null;
        }
        return getUserInfo(account,
            applicationConfiguration.isDisplayUserEmail());
    }

    /**
     * Generate {@link org.zanata.rest.dto.User} object from HAccount
     *
     * @param account - HAccount
     * @param includeEmail - Display user email
     */
    public User getUserInfo(HAccount account, boolean includeEmail) {
        if(account == null) {
            return new User();
        }
        //need to use dao to load entity due to lazy loading of languageMemberships
        HPerson person = personDAO.findById(account.getPerson().getId());

        String email = person.getEmail();

        String userImageUrl = gravatarServiceImpl
            .getUserImageUrl(GravatarService.USER_IMAGE_SIZE, email);

        List<String> userLanguageTeams =
                person.getLanguageMemberships().stream()
                        .map(HLocale::retrieveDisplayName)
                        .collect(Collectors.toList());

        if(!includeEmail) {
            email = null;
        }
        return new User(account.getUsername(), email, person.getName(),
            userImageUrl, userLanguageTeams);
    }

    /**
     * Return user's permission for js module.
     * ServiceImpl will do security check again upon execution of any action.
     */
    public Permission getUserPermission() {
        Permission permission = new Permission();
        boolean canUpdate = false;
        boolean canInsert = false;
        boolean canDelete = false;
        boolean canDownload = false;
        boolean isAdmin = false;
        boolean isLoggedIn = authenticatedAccount != null;

        if(authenticatedAccount != null) {
            canUpdate = identity.hasPermission("", "glossary-update");
            canInsert = identity.hasPermission("", "glossary-insert");
            canDelete = identity.hasPermission("", "glossary-delete");
            canDownload = identity.hasPermission("", "glossary-download");
            isAdmin = identity.hasRole("admin");
        }

        permission.put("updateGlossary", canUpdate);
        permission.put("insertGlossary", canInsert);
        permission.put("deleteGlossary", canDelete);
        permission.put("downloadGlossary", canDownload);
        permission.put("isAdmin", isAdmin);
        permission.put("isLoggedIn", isLoggedIn);

        return permission;
    }
}
