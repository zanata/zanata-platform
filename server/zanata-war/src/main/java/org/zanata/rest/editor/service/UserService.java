package org.zanata.rest.editor.service;

import java.util.List;
import java.util.stream.Collectors;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import javax.inject.Named;
import com.google.common.collect.Lists;
import org.apache.deltaspike.jpa.api.transaction.Transactional;
import org.apache.commons.lang.StringUtils;
import org.zanata.ApplicationConfiguration;
import org.zanata.common.LocaleId;
import org.zanata.dao.AccountDAO;
import org.zanata.dao.PersonDAO;
import org.zanata.dao.ProjectDAO;
import org.zanata.model.HAccount;
import org.zanata.model.HLocale;
import org.zanata.model.HPerson;
import org.zanata.model.HProject;
import org.zanata.rest.dto.Account;
import org.zanata.rest.dto.LocaleDetails;
import org.zanata.rest.dto.User;
import org.zanata.rest.editor.dto.Permission;
import org.zanata.rest.editor.service.resource.UserResource;
import org.zanata.rest.service.AccountService;
import org.zanata.rest.service.GlossaryService;
import org.zanata.seam.security.IdentityManager;
import org.zanata.security.ZanataIdentity;
import org.zanata.security.annotations.Authenticated;
import org.zanata.security.annotations.CheckLoggedIn;
import org.zanata.service.GravatarService;
import com.google.common.base.Strings;
import org.zanata.service.impl.LocaleServiceImpl;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
@RequestScoped
@Named("editor.userService")
@Path(UserResource.SERVICE_PATH)
@Transactional(readOnly = true)
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
    private ProjectDAO projectDAO;
    @Inject
    private ZanataIdentity identity;
    @Inject
    private ApplicationConfiguration applicationConfiguration;

    @Inject
    private IdentityManager identityManager;

    @Override
    @CheckLoggedIn
    public Response getMyInfo() {
        if (authenticatedAccount == null) {
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
        if (authenticatedAccount == null) {
            return Response.status(Response.Status.FORBIDDEN).build();
        }
        HAccount account =
                accountDAO.getByUsername(authenticatedAccount.getUsername());
        // we may not need to return apiKey (and generating it
        // without asking) anymore once client switched to OAuth
        if (Strings.isNullOrEmpty(authenticatedAccount.getApiKey())) {
            accountDAO.createApiKey(account);
        }
        Account dto = new Account();
        AccountService.getAccountDetails(account, dto);
        return Response.ok(dto).build();
    }

    @Override
    public Response getGlossaryPermission(
            @DefaultValue(GlossaryService.GLOBAL_QUALIFIED_NAME) String qualifiedName) {
        if (authenticatedAccount == null) {
            return Response.status(Response.Status.FORBIDDEN).build();
        }
        Permission permission = new Permission();
        boolean canUpdate = false;
        boolean canInsert = false;
        boolean canDelete = false;
        boolean canDownload = false;
        if (authenticatedAccount != null) {
            if (GlossaryService.isProjectGlossary(qualifiedName)) {
                HProject project = projectDAO.getBySlug(
                        GlossaryService.getProjectSlug(qualifiedName));
                canUpdate = identity.hasPermission(project, "glossary-update");
                canInsert = identity.hasPermission(project, "glossary-insert");
                canDelete = identity.hasPermission(project, "glossary-delete");
                canDownload =
                        identity.hasPermission(project, "glossary-download");
            } else {
                canUpdate = identity.hasPermission("", "glossary-update");
                canInsert = identity.hasPermission("", "glossary-insert");
                canDelete = identity.hasPermission("", "glossary-delete");
                canDownload = identity.hasPermission("", "glossary-download");
            }
        }
        permission.put("updateGlossary", canUpdate);
        permission.put("insertGlossary", canInsert);
        permission.put("deleteGlossary", canDelete);
        permission.put("downloadGlossary", canDownload);
        return Response.ok(permission).build();
    }

    @Override
    public Response getLocalesPermission() {
        if (authenticatedAccount == null) {
            return Response.status(Response.Status.FORBIDDEN).build();
        }
        Permission permission = new Permission();
        boolean canDelete = identity.hasPermission("", "delete-language");
        boolean canAdd = identity.hasPermission("", "insert-language");
        permission.put("canDeleteLocale", canDelete);
        permission.put("canAddLocale", canAdd);
        return Response.ok(permission).build();
    }

    /**
     * Generate {@link org.zanata.rest.dto.User} object from username
     *
     * @param username
     *            - username in HPerson
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
     * @param account
     *            - HAccount
     * @param includeEmail
     *            - Display user email
     */
    public User getUserInfo(HAccount account, boolean includeEmail) {
        if (account == null) {
            return new User();
        }
        // need to use dao to load entity due to lazy loading of
        // languageMemberships
        HPerson person = personDAO.findById(account.getPerson().getId());
        String email = person.getEmail();
        String userImageUrl = gravatarServiceImpl
                .getUserImageUrl(GravatarService.USER_IMAGE_SIZE, email);
        List<LocaleId> userLanguageTeams = person.getLanguageMemberships()
                .stream().map(hLocale -> hLocale.getLocaleId())
                .collect(Collectors.toList());
        List<String> roles =
                identityManager.getGrantedRoles(account.getUsername());

        return new User(account.getUsername(), includeEmail ? email : null,
                person.getName(), userImageUrl, userLanguageTeams, roles);
    }

    /**
     * Return user's permission for js module. ServiceImpl will do security
     * check again upon execution of any action.
     */
    public Permission getUserPermission() {
        Permission permission = new Permission();
        boolean isAdmin = false;
        if (authenticatedAccount != null) {
            isAdmin = identity.hasRole("admin");
        }
        permission.put("isAdmin", isAdmin);
        permission.put("isLoggedIn", authenticatedAccount != null);
        return permission;
    }

    public UserService() {
    }

    @java.beans.ConstructorProperties({ "authenticatedAccount",
            "gravatarServiceImpl", "accountDAO", "personDAO", "projectDAO",
            "identity", "applicationConfiguration" })
    protected UserService(final HAccount authenticatedAccount,
            final GravatarService gravatarServiceImpl,
            final AccountDAO accountDAO, final PersonDAO personDAO,
            final ProjectDAO projectDAO, final ZanataIdentity identity,
            final ApplicationConfiguration applicationConfiguration,
            final IdentityManager identityManager) {
        this.authenticatedAccount = authenticatedAccount;
        this.gravatarServiceImpl = gravatarServiceImpl;
        this.accountDAO = accountDAO;
        this.personDAO = personDAO;
        this.projectDAO = projectDAO;
        this.identity = identity;
        this.applicationConfiguration = applicationConfiguration;
        this.identityManager = identityManager;
    }
}
