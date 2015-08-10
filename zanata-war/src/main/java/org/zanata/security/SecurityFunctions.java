/*
 * Copyright 2010, Red Hat, Inc. and individual contributors as indicated by the
 * @author tags. See the copyright.txt file in the distribution for a full
 * listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this software; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
 * site: http://www.fsf.org.
 */
package org.zanata.security;

import com.google.common.base.Optional;
import org.jboss.seam.ScopeType;
import org.zanata.seam.security.ZanataJpaIdentityStore;
import org.zanata.dao.PersonDAO;
import org.zanata.model.HAccount;
import org.zanata.model.HAccountRole;
import org.zanata.model.HIterationGroup;
import org.zanata.model.HLocale;
import org.zanata.model.HLocaleMember;
import org.zanata.model.HProject;
import org.zanata.model.HProjectIteration;
import org.zanata.security.permission.GrantsPermission;
import org.zanata.util.HttpUtil;
import org.zanata.util.ServiceLocator;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import lombok.extern.slf4j.Slf4j;

/**
 * Contains static helper functions used inside the rules files.
 *
 * @author Carlos Munoz <a
 *         href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 */
@Slf4j
public class SecurityFunctions {
    protected SecurityFunctions() {
    }

    /* admin can do anything */
    @GrantsPermission
    public static boolean isAdmin() {
        return getIdentity().hasRole("admin");
    }

    public static boolean isProjectMaintainer(HProject project) {
        Optional<HAccount> account = getAuthenticatedAccount();
        return account.isPresent() && account.get().getPerson().isMaintainer(project);
    }

    /***************************************************************************
     * The Following Rules are for Identity Management
     **************************************************************************/

    @GrantsPermission(actions = "create")
    public static boolean canCreateAccount(String target) {
        return target.equals("seam.account") && isAdmin();
    }

    @GrantsPermission
    public static boolean canManageUsers(String target) {
        return target.equals("seam.user") && isAdmin();
    }

    @GrantsPermission
    public static boolean canManageRoles(String target) {
        return target.equals("seam.role") && isAdmin();
    }

    /***************************************************************************
     * Project ownership rules
     **************************************************************************/

    /* 'project-creator' can create project */
    @GrantsPermission(actions = "insert")
    public static boolean canCreateProject(HProject target) {
        return getIdentity().hasRole("project-creator");
    }

    /* anyone can read a project */
    @GrantsPermission(actions = "read")
    public static boolean canReadProject(HProject target) {
        return true;
    }

    /* anyone can read a project iteration */
    @GrantsPermission(actions = "read")
    public static boolean canReadProjectIteration(HProjectIteration target) {
        return true;
    }

    /*
     * Project maintainers may edit (but not delete) a project, or add an
     * iteration. Note that 'add-iteration' (on a project) should be granted in
     * the same circumstances that 'insert' is granted (on an iteration). In
     * other words, make sure the rules agree with each other. (NB:
     * 'add-iteration' is used in the UI to enable buttons etc, without
     * requiring the construction of HProjectIteration just to do a permission
     * check.)
     */
    @GrantsPermission(actions = { "update",
            "add-iteration" })
    public static boolean canUpdateProjectOrAddIteration(HProject project) {
        if (!getIdentity().isLoggedIn()) {
            return false;
        }

        return isProjectMaintainer(project);
    }

    /*
     * Project maintainers may create or edit (but not delete) a project
     * iteration
     */
    @GrantsPermission(actions = {
            "insert", "update", "import-template" })
    public static boolean canInsertOrUpdateProjectIteration(
            HProjectIteration iteration) {
        return isProjectMaintainer(iteration.getProject());
    }

    /***************************************************************************
     * Translation rules
     **************************************************************************/

    /* Language Team members can add a translation for their language teams */
    @GrantsPermission(actions = { "add-translation", "modify-translation" })
    public static boolean canTranslate(HProject project, HLocale lang) {
        return isUserAllowedAccess(project) && isUserTranslatorOfLanguage(lang);
    }

    public static boolean isUserTranslatorOfLanguage(HLocale lang) {
        Optional<HAccount> authenticatedAccount = getAuthenticatedAccount();
        PersonDAO personDAO =
                ServiceLocator.instance().getInstance(PersonDAO.class);

        if (authenticatedAccount.isPresent()) {
            return personDAO.isUserInLanguageTeamWithRoles(
                    authenticatedAccount.get().getPerson(), lang, true, null, null);
        }

        return false; // No authenticated user
    }

    public static boolean isUserAllowedAccess(HProject project) {
        if (project.isRestrictedByRoles()) {
            ZanataIdentity identity = getIdentity();

            for (HAccountRole role : project.getAllowedRoles()) {
                if (identity.hasRole(role.getName())) {
                    return true;
                }
            }

            // no access
            return false;
        } else {
            return true;
        }
    }

    /***************************************************************************
     * Review translation rules
     **************************************************************************/
    /* Language Team reviewer can approve/reject translation */
    // TODO Unify these two permission actions into a single one
    @GrantsPermission(
            actions = { "review-translation", "translation-review" })
    public static boolean
            canReviewTranslation(HProject project, HLocale locale) {
        return isUserAllowedAccess(project) && isUserReviewerOfLanguage(locale);
    }

    public static boolean isUserReviewerOfLanguage(HLocale lang) {
        Optional<HAccount> authenticatedAccount = getAuthenticatedAccount();
        PersonDAO personDAO =
                ServiceLocator.instance().getInstance(PersonDAO.class);

        if (authenticatedAccount.isPresent()) {
            return personDAO.isUserInLanguageTeamWithRoles(
                    authenticatedAccount.get().getPerson(), lang, null, true, null);
        } else {
            return false;
        }

    }

    /*
     * Project Maintainers can add, modify or review a translation for their
     * projects
     */
    @GrantsPermission(actions = { "add-translation", "modify-translation",
            "review-translation", "translation-review" })
    public static boolean canAddOrReviewTranslation(
            HProject project, HLocale locale) {
        Optional<HAccount> authenticatedAccount = getAuthenticatedAccount();
        return authenticatedAccount.isPresent() && isProjectMaintainer(project);
    }

    /* Project Maintainer can import translation (merge type is IMPORT) */
    @GrantsPermission(actions = "import-translation")
    public static boolean canImportTranslation(
            HProjectIteration projectIteration) {
        Optional<HAccount> account = getAuthenticatedAccount();
        return account.isPresent() && account.get().getPerson().isMaintainer(projectIteration.getProject());
    }

    public static boolean isLanguageTeamMember(HLocale lang) {
        Optional<HAccount> authenticatedAccount = getAuthenticatedAccount();
        PersonDAO personDAO =
                ServiceLocator.instance().getInstance(PersonDAO.class);

        if (authenticatedAccount.isPresent()) {
            return personDAO.isUserInLanguageTeamWithRoles(
                    authenticatedAccount.get().getPerson(), lang, null, null, null);
        } else {
            return false;
        }

    }

    /***************************************************************************
     * Glossary rules
     **************************************************************************/

    /* 'glossarist' can push and update glossaries */
    @GrantsPermission(actions = { "glossary-insert", "glossary-update" })
    public static boolean canPushGlossary() {
        return getIdentity().hasRole("glossarist");
    }

    /* 'glossarist-admin' can also delete */
    @GrantsPermission(actions = { "glossary-insert", "glossary-update",
            "glossary-delete" })
    public static boolean canAdminGlossary() {
        return getIdentity().hasRole("glossary-admin");
    }

    /***************************************************************************
     * Language Team Coordinator rules
     **************************************************************************/

    /* Anyone can read Locale members */
    @GrantsPermission(actions = "read")
    public static boolean canSeeLocaleMembers(HLocaleMember localeMember) {
        return true;
    }

    /* 'team coordinator' can manage language teams */
    @GrantsPermission(actions = "manage-language-team")
    public static boolean isUserCoordinatorOfLanguage(HLocale lang) {
        Optional<HAccount> authenticatedAccount = getAuthenticatedAccount();
        PersonDAO personDAO =
                ServiceLocator.instance().getInstance(PersonDAO.class);

        if (authenticatedAccount.isPresent()) {
            return personDAO.isUserInLanguageTeamWithRoles(
                    authenticatedAccount.get().getPerson(), lang, null, null, true);
        } else {
            return false;
        }

    }

    /* 'team coordinator' can insert/update/delete language team members */
    @GrantsPermission(actions = { "insert", "update", "delete" })
    public static boolean canModifyLanguageTeamMembers(
            HLocaleMember localeMember) {
        return isUserCoordinatorOfLanguage(localeMember.getSupportedLanguage());
    }

    /***************************************************************************
     * View Obsolete Project and Project Iteration rules
     **************************************************************************/

    // Only admin can view obsolete projects
    @GrantsPermission(actions = "view-obsolete")
    public static boolean canViewObsoleteProject(HProject project) {
        return getIdentity().hasRole("admin");
    }

    // Only admin can view obsolete project iterations
    @GrantsPermission(actions = "view-obsolete")
    public static boolean canViewObsoleteProjectIteration(
            HProjectIteration projectIteration) {
        return getIdentity().hasRole("admin");
    }

    /***************************************************************************
     * Mark Project and Project Iteration obsolete rules
     **************************************************************************/

    // Project maintainer can archive/delete projects
    @GrantsPermission(actions = "mark-obsolete")
    public static boolean canArchiveProject(HProject project) {
        return isProjectMaintainer(project);
    }

    // Project maintainer can archive/delete project iterations
    @GrantsPermission(actions = "mark-obsolete")
    public static boolean canArchiveProjectIteration(
            HProjectIteration projectIteration) {
        return isProjectMaintainer(projectIteration.getProject());
    }

    /***************************************************************************
     * File Download rules
     **************************************************************************/

    /*
     * Permissions to download files. NOTE: Currently any authenticated user can
     * download files
     */
    @GrantsPermission(actions = { "download-single", "download-all" })
    public static boolean canDownloadFiles(HProjectIteration projectIteration) {
        return getIdentity().isLoggedIn();
    }

    /***************************************************************************
     * Version Group rules
     **************************************************************************/
    @GrantsPermission(actions = "update")
    public static boolean canUpdateVersionGroup(HIterationGroup group) {
        Optional<HAccount> account = getAuthenticatedAccount();
        return account.isPresent() && account.get().getPerson().isMaintainer(group);
    }

    @GrantsPermission(actions = "insert")
    public static boolean canInsertVersionGroup(HIterationGroup group) {
        return isAdmin();
    }

    @GrantsPermission(actions = "view-obsolete")
    public static boolean canViewObsoleteVersionGroup(HIterationGroup group) {
        return isAdmin();
    }

    /***************************************************************************
     * Copy Trans rules
     **************************************************************************/

    /** Admins and Project maintainers can perform copy-trans */
    @GrantsPermission(actions = "copy-trans")
    public static boolean canRunCopyTrans(HProjectIteration iteration) {
        Optional<HAccount> account = getAuthenticatedAccount();
        return account.isPresent() && account.get().getPerson().isMaintainer(iteration.getProject());
    }

    /*****************************************************************************************
     * Review comment rules
     ******************************************************************************************/

    @GrantsPermission(actions = "review-comment")
    public static boolean canCommentOnReview(HLocale locale, HProject project) {
        return isUserAllowedAccess(project) && isLanguageTeamMember(locale);
    }

    @GrantsPermission(actions = "review-comment")
    public static boolean canMaintainerCommentOnReview(HLocale locale,
            HProject project) {
        Optional<HAccount> account = getAuthenticatedAccount();
        return account.isPresent() && account.get().getPerson().isMaintainer(project);
    }

    private static final ZanataIdentity getIdentity() {
        return ServiceLocator.instance().getInstance(ZanataIdentity.class);
    }

    private static final Optional<HAccount> getAuthenticatedAccount() {
        return Optional.fromNullable(ServiceLocator.instance().getInstance(
                ZanataJpaIdentityStore.AUTHENTICATED_USER, HAccount.class));
    }

    private static final <T> T extractTarget(Object[] array, Class<T> type) {
        for (int i = 0; i < array.length; i++) {
            Object arrayElement = array[i];
            if (type.isAssignableFrom(arrayElement.getClass())) {
                return (T) arrayElement;
            }
        }
        return null;
    }

    /*****************************************************************************************
     * TMX rules
     ******************************************************************************************/

    @GrantsPermission(actions = "download-tmx")
    public static boolean canDownloadTMX() {
        Optional<HAccount> account = getAuthenticatedAccount();
        return account.isPresent();
    }


    /*****************************************************************************************
     * HTTP request rules
     ******************************************************************************************/

    /**
     * Check if user can access to REST URL with httpMethod.
     * 1) Check if request can communicate to with rest service path,
     * 2) then check if request can perform the specific API action.
     *
     * If request is from anonymous user(account == null),
     * only 'Read' action are allowed. Additionally, role-based check will be
     * performed in the REST service class.
     *
     * This rule apply to all REST endpoint.
     *
     * @param account - Authenticated account
     * @param httpMethod - {@link javax.ws.rs.HttpMethod}
     * @param restServicePath - service path of rest request.
     *                        See annotation @Path in REST service class.
     */
    public static final boolean canAccessRestPath(@Nullable HAccount account,
            String httpMethod, String restServicePath) {
        //This is to allow data injection for function-test/rest-test
        if(isTestServicePath(restServicePath)) {
            log.debug("Allow rest access for Zanata test");
            return true;
        }
        if (account != null) {
            return true;
        }
        if (HttpUtil.isReadMethod(httpMethod)) {
            return true;
        }
        return false;
    }

    public static final boolean canAccessRestPath(
            @Nonnull ZanataIdentity identity,
            String httpMethod, String restServicePath) {
        // This is to allow data injection for function-test/rest-test
        if (isTestServicePath(restServicePath)) {
            log.debug("Allow rest access for Zanata test");
            return true;
        }
        if (identity.isLoggedIn()) {
            return true;
        }
        return false;
    }

    /**
     * Check if request path are from functional test or RestTest
     *
     * @param servicePath - service path of rest request.
     *                        See annotation @Path in REST service class.
     */
    private static boolean isTestServicePath(String servicePath) {
        return servicePath != null && servicePath.startsWith("/test");
    }
}
