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
import org.hibernate.Session;
import org.zanata.seam.security.ZanataJpaIdentityStore;
import org.zanata.dao.PersonDAO;
import org.zanata.dao.ProjectLocaleMemberDAO;
import org.zanata.dao.ProjectMemberDAO;
import org.zanata.model.HAccount;
import org.zanata.model.HAccountRole;
import org.zanata.model.HIterationGroup;
import org.zanata.model.HLocale;
import org.zanata.model.HLocaleMember;
import org.zanata.model.HPerson;
import org.zanata.model.HProject;
import org.zanata.model.HProjectIteration;
import org.zanata.model.LocaleRole;
import org.zanata.model.ProjectRole;
import org.zanata.security.annotations.AuthenticatedLiteral;
import org.zanata.security.permission.GrantsPermission;
import org.zanata.util.HttpUtil;
import org.zanata.util.ServiceLocator;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import lombok.Delegate;
import lombok.extern.slf4j.Slf4j;

import static org.zanata.model.ProjectRole.Maintainer;
import static org.zanata.model.ProjectRole.TranslationMaintainer;

/**
 * Contains static security rules functions used to determine permissions.
 *
 * <b>NOTE</b>:
 * If you intended to use DAO and execute query in security functions, please
 * make sure you use a new session to execute the query. The reason being, if
 * you update an entity, SmartEntitySecurityListener will perform a check at
 * pre-update. Any query executed within the same session will trigger a flush,
 * which triggers another SmartEntitySecurityListener check and this will run
 * into a loop.
 *
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
        return isProjectRole(project, Maintainer);
    }

    public static boolean isProjectTranslationMaintainer(HProject project) {
        return isProjectRole(project, TranslationMaintainer);
    }

    /* Check whether the authenticated person has the given role in the given
     * project.
     */
    private static boolean isProjectRole(HProject project, ProjectRole role) {
        Optional<HAccount> account = getAuthenticatedAccount();

        if (account.isPresent()) {
            HPerson person = account.get().getPerson();

            // see class level javadoc for why we need a new session here
            try (AutoCloseSession autoCloseSession = newSession()) {
                ProjectMemberDAO projectMemberDAO =
                        new ProjectMemberDAO(autoCloseSession.session);
                return projectMemberDAO
                        .hasProjectRole(person, project, role);
            }
        }

        // No authenticated user
        return false;
    }

    /**
     *
     * @return a new AutoClosable wrapper of a NEW session
     */
    private static AutoCloseSession newSession() {
        Session session =
                (Session) ServiceLocator.instance().getEntityManagerFactory()
                        .createEntityManager().getDelegate();
        return new AutoCloseSession(session);
    }

    private static boolean userHasProjectLanguageRole(HProject project,
            HLocale lang,
            LocaleRole role) {
        Optional<HAccount> account = getAuthenticatedAccount();

        if (account.isPresent()) {
            HPerson person = account.get().getPerson();
            return ServiceLocator.instance().getInstance(ProjectLocaleMemberDAO.class)
                    .hasProjectLocaleRole(person, project, lang, role);
        }

        // No authenticated user
        return false;
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

    @GrantsPermission(actions = "merge-trans")
    public static boolean canMergeTrans(HProject project) {
        return isAdmin() || isProjectMaintainer(project);
    }

    /***************************************************************************
     * Project team management rules
     **************************************************************************/

    /* Maintainer can manage all project roles */
    @GrantsPermission(actions = {"manage-members"})
    public static boolean canManageProjectMembers(HProject project) {
        return getIdentity().isLoggedIn() && isProjectMaintainer(project);
    }

    /* Translation Maintainer can manage project translation team */
    @GrantsPermission(actions = {"manage-translation-members"})
    public static boolean canManageProjectTranslationMembers(HProject project) {
        // TODO add a DAO check for multiple project roles at once (single query
        // instead of two)
        return getIdentity().isLoggedIn() &&
                (isProjectTranslationMaintainer(project) ||
                isProjectMaintainer(project));
    }

    /***************************************************************************
     * Translation rules
     **************************************************************************/

    /* Global Language Team members can add a translation for their language
     * teams, unless global translation is restricted. */
    @GrantsPermission(actions = { "add-translation", "modify-translation" })
    public static boolean canTranslate(HProject project, HLocale lang) {
        return project.isAllowGlobalTranslation() &&
                isUserAllowedAccess(project) && isUserTranslatorOfLanguage(lang);
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

    /* Project Language Translators can add a translation for their language
     * regardless of global translation setting.
     */
    @GrantsPermission(actions = { "add-translation", "modify-translation" })
    public static boolean projectTranslatorCanTranslate(HProject project, HLocale lang) {
        Optional<HAccount> authenticatedAccount = getAuthenticatedAccount();
        return authenticatedAccount.isPresent() &&
                userHasProjectLanguageRole(project, lang, LocaleRole.Translator);
    }

    /***************************************************************************
     * Review translation rules
     **************************************************************************/
    /* Global Language Team reviewer can approve/reject translation, unless
     * global translation is restricted. */
    // TODO Unify these two permission actions into a single one
    @GrantsPermission(
            actions = { "review-translation", "translation-review" })
    public static boolean
            canReviewTranslation(HProject project, HLocale locale) {
        return project.isAllowGlobalTranslation() &&
                isUserAllowedAccess(project) && isUserReviewerOfLanguage(locale);
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

    /* Project Translation Maintainers can add, modify or review a translation
     * for their projects.
     */
    @GrantsPermission(actions = { "add-translation", "modify-translation",
            "review-translation", "translation-review" })
    public static boolean translationMaintainerCanTranslate(HProject project,
                                                            HLocale locale) {
        Optional<HAccount> authenticatedAccount = getAuthenticatedAccount();
        return authenticatedAccount.isPresent() && isProjectTranslationMaintainer(
                project);
    }

    /* Project Translation Reviewer can perform translation and review for their
     * language in the project, regardless of global translation permission.
     */
    @GrantsPermission(actions = { "add-translation", "modify-translation",
            "review-translation", "translation-review" })
    public static boolean projectReviewerCanTranslateAndReview(HProject project, HLocale lang) {
        Optional<HAccount> authenticatedAccount = getAuthenticatedAccount();
        return authenticatedAccount.isPresent() &&
                userHasProjectLanguageRole(project, lang, LocaleRole.Reviewer);
    }

    /* Project Maintainer can import translation (merge type is IMPORT) */
    @GrantsPermission(actions = "import-translation")
    public static boolean canImportTranslation(
            HProjectIteration projectIteration) {
        Optional<HAccount> account = getAuthenticatedAccount();
        return account.isPresent() && isProjectMaintainer(projectIteration.getProject());
    }

    /* Project Translation Maintainer can import translation (merge type is IMPORT) */
    @GrantsPermission(actions = "import-translation")
    public static boolean translationMaintainerCanImportTranslation(
            HProjectIteration projectIteration) {
        Optional<HAccount> account = getAuthenticatedAccount();
        return account.isPresent() && isProjectTranslationMaintainer(projectIteration.getProject());
    }

    /* Membership in global language teams. */
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
        return account.isPresent() && isProjectMaintainer(iteration.getProject());
    }

    /*****************************************************************************************
     * Review comment rules
     ******************************************************************************************/

    @GrantsPermission(actions = "review-comment")
    public static boolean canCommentOnReview(HLocale locale, HProject project) {
        return project.isAllowGlobalTranslation() &&
                isUserAllowedAccess(project) && isLanguageTeamMember(locale);
    }

    @GrantsPermission(actions = "review-comment")
    public static boolean canMaintainerCommentOnReview(HLocale locale,
            HProject project) {
        Optional<HAccount> account = getAuthenticatedAccount();
        return account.isPresent() && isProjectMaintainer(project);
    }

    @GrantsPermission(actions = "review-comment")
    public static boolean canTranslationMaintainerCommentOnReview(HLocale locale,
            HProject project) {
        Optional<HAccount> account = getAuthenticatedAccount();
        return account.isPresent() && isProjectTranslationMaintainer(project);
    }

    @GrantsPermission(actions = "review-comment")
    public static boolean canReviewerCommentOnReview(HLocale locale,
            HProject project) {
        Optional<HAccount> account = getAuthenticatedAccount();
        return account.isPresent() &&
                userHasProjectLanguageRole(project, locale, LocaleRole.Reviewer);
    }

    @GrantsPermission(actions = "review-comment")
    public static boolean canTranslatorCommentOnReview(HLocale locale,
            HProject project) {
        Optional<HAccount> account = getAuthenticatedAccount();
        return account.isPresent() &&
                userHasProjectLanguageRole(project, locale, LocaleRole.Translator);
    }

    private static final ZanataIdentity getIdentity() {
        return ServiceLocator.instance().getInstance(ZanataIdentity.class);
    }

    private static final Optional<HAccount> getAuthenticatedAccount() {
        return Optional.fromNullable(ServiceLocator.instance().getInstance(
                HAccount.class, new AuthenticatedLiteral()));
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
        return servicePath != null
                && (
                // when being called in RestLimitingFilter
                servicePath.contains("/rest/test/") ||
                        // when being called in ZanataRestSecurityInterceptor
                servicePath.startsWith("/test")
        );
    }

    private static class AutoCloseSession implements AutoCloseable {
        private final Session session;

        private AutoCloseSession(Session session) {
            this.session = session;
        }

        public void close() {
            session.close();
        }
    }
}
