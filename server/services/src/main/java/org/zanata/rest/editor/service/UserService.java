package org.zanata.rest.editor.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;
import javax.inject.Named;
import org.apache.deltaspike.jpa.api.transaction.Transactional;
import org.apache.commons.lang3.StringUtils;
import org.zanata.ApplicationConfiguration;
import org.zanata.common.LocaleId;
import org.zanata.dao.AccountDAO;
import org.zanata.dao.AccountOptionDAO;
import org.zanata.dao.PersonDAO;
import org.zanata.dao.ProjectDAO;
import org.zanata.model.HAccount;
import org.zanata.model.HAccountOption;
import org.zanata.model.HLocale;
import org.zanata.model.HPerson;
import org.zanata.model.HProject;
import org.zanata.rest.dto.Account;
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
import org.zanata.service.SecurityService;

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
    private AccountOptionDAO accountOptionDAO;
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
        boolean canUpdate;
        boolean canInsert;
        boolean canDelete;
        boolean canDownload;
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

    @Override
    public Response getUserPermissions(String projectSlug, String localeId) {
        if (authenticatedAccount == null) {
            return Response.status(Response.Status.FORBIDDEN).build();
        }
        Permission permission = new Permission();
        boolean canReview = identity.hasPermissionWithAnyTargets("translation-review",
                projectSlug, localeId);
        boolean canTranslate = identity.hasPermissionWithAnyTargets(
                SecurityService.TranslationAction.MODIFY.action(), projectSlug,
                localeId);
        permission.put("reviewer", canReview);
        permission.put("translator", canTranslate);
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
        HAccount account = accountDAO.getEnabledByUsername(username);
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

    /**
     * Retrieve settings for the current authenticated user that begin with prefix.
     * The prefix is stripped
     *
     * @param prefix only return settings that begin with this prefix, separated
     *               from the name with '.'
     * @return a JSON object with keys of setting name (without prefix) and values
     *         of setting value.
     */
    public Response getSettings(String prefix) {
        String dotPrefix = prefix + ".";
        int trim = dotPrefix.length();
        HAccount account = accountDAO.findById(authenticatedAccount.getId(), true);
        Map<String, String> options = new HashMap<String, String>();
        account.getEditorOptions().values().stream()
            .filter(o -> o.getName().startsWith(dotPrefix))
            .forEach(o -> options.put(o.getName().substring(trim), o.getValue()));
        return Response.ok(options).build();
    }

    /**
     * Add or update some settings for the current authenticated user.
     *
     * @param prefix add prefix then '.' to the front of each setting before persisting.
     * @param settings JSON object with setting names as keys
     */
    @Transactional(readOnly = false)
    public Response postSettings(String prefix, Map<String, String> settings) {
        HAccount account = accountDAO.findById(authenticatedAccount.getId(), true);
        for (Map.Entry<String, String> entry : settings.entrySet()) {
            String name = prefix + "." + entry.getKey();
            // Look up the existing option
            HAccountOption option = account.getEditorOptions().get(name);
            if (option == null) {
                // need a new one
                option = new HAccountOption(name, entry.getValue());
                option.setAccount(account);
                account.getEditorOptions().put(name, option);
            } else {
                option.setValue(entry.getValue());
            }
            accountOptionDAO.makePersistent(option);
        }
        accountOptionDAO.flush();
        return Response.ok().build();
    }

}
