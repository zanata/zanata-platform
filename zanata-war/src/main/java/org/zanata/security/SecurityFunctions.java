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

import lombok.extern.slf4j.Slf4j;
import org.hibernate.Session;
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
import org.zanata.security.annotations.Authenticated;
import org.zanata.security.permission.GrantsPermission;
import org.zanata.security.permission.PermissionProvider;
import org.zanata.util.HttpUtil;
import org.zanata.util.ServiceLocator;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;

import java.util.Optional;

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
public class SecurityFunctions extends PermissionProvider {

    @Inject
    private ZanataIdentity identity;

    @Inject
    @Authenticated
    private Optional<HAccount> authenticatedAccount;

    /* admin can do anything */
    @GrantsPermission
    public boolean isAdmin() {
        return identity.hasRole("admin");
    }

    public boolean isLoggedIn() {
        return authenticatedAccount.isPresent();
    }

    public boolean isProjectMaintainer(HProject project) {
        return isProjectRole(project, Maintainer);
    }

    public boolean isProjectTranslationMaintainer(HProject project) {
        return isProjectRole(project, TranslationMaintainer);
    }

    /* Check whether the authenticated person has the given role in the given
     * project.
     */
    private boolean isProjectRole(HProject project, ProjectRole role) {
        if (isLoggedIn()) {
            HPerson person = authenticatedAccount.get().getPerson();

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

    private boolean userHasProjectLanguageRole(HProject project,
            HLocale lang,
            LocaleRole role) {

        if (isLoggedIn()) {
            HPerson person = authenticatedAccount.get().getPerson();
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
    public boolean canCreateAccount(String target) {
        return target.equals("seam.account") && isAdmin();
    }

    @GrantsPermission
    public boolean canManageUsers(String target) {
        return target.equals("seam.user") && isAdmin();
    }

    @GrantsPermission
    public boolean canManageRoles(String target) {
        return target.equals("seam.role") && isAdmin();
    }

    /***************************************************************************
     * Project ownership rules
     **************************************************************************/

    /* 'project-creator' can create project */
    @GrantsPermission(actions = "insert")
    public boolean canCreateProject(HProject target) {
        return identity.hasRole("project-creator");
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
    public boolean canUpdateProjectOrAddIteration(HProject project) {
        if (!identity.isLoggedIn()) {
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
    public boolean canInsertOrUpdateProjectIteration(
            HProjectIteration iteration) {
        return isProjectMaintainer(iteration.getProject());
    }

    @GrantsPermission(actions = "merge-trans")
    public boolean canMergeTrans(HProject project) {
        return isAdmin() || isProjectMaintainer(project);
    }

    /***************************************************************************
     * Project team management rules
     **************************************************************************/

    /* Maintainer can manage all project roles */
    @GrantsPermission(actions = {"manage-members"})
    public boolean canManageProjectMembers(HProject project) {
        return identity.isLoggedIn() && isProjectMaintainer(project);
    }

    /* Translation Maintainer can manage project translation team */
    @GrantsPermission(actions = {"manage-translation-members"})
    public boolean canManageProjectTranslationMembers(HProject project) {
        // TODO add a DAO check for multiple project roles at once (single query
        // instead of two)
        return identity.isLoggedIn() &&
                (isProjectTranslationMaintainer(project) ||
                isProjectMaintainer(project));
    }

    /***************************************************************************
     * Translation rules
     **************************************************************************/

    /* Global Language Team members can add a translation for their language
     * teams, unless global translation is restricted. */
    @GrantsPermission(actions = { "add-translation", "modify-translation" })
    public boolean canTranslate(HProject project, HLocale lang) {
        return project.isAllowGlobalTranslation() &&
                isUserAllowedAccess(project) && isUserTranslatorOfLanguage(lang);
    }

    public boolean isUserTranslatorOfLanguage(HLocale lang) {
        PersonDAO personDAO =
                ServiceLocator.instance().getInstance(PersonDAO.class);

        if (isLoggedIn()) {
            return personDAO.isUserInLanguageTeamWithRoles(
                    authenticatedAccount.get().getPerson(), lang, true, null, null);
        }

        return false; // No authenticated user
    }

    public boolean isUserAllowedAccess(HProject project) {
        if (project.isRestrictedByRoles()) {
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
    public boolean projectTranslatorCanTranslate(HProject project, HLocale lang) {
        return isLoggedIn() &&
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
    public boolean
            canReviewTranslation(HProject project, HLocale locale) {
        return project.isAllowGlobalTranslation() &&
                isUserAllowedAccess(project) && isUserReviewerOfLanguage(locale);
    }

    public boolean isUserReviewerOfLanguage(HLocale lang) {
        PersonDAO personDAO =
                ServiceLocator.instance().getInstance(PersonDAO.class);

        if (isLoggedIn()) {
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
    public boolean canAddOrReviewTranslation(
            HProject project, HLocale locale) {
        return isLoggedIn() && isProjectMaintainer(project);
    }

    /* Project Translation Maintainers can add, modify or review a translation
     * for their projects.
     */
    @GrantsPermission(actions = { "add-translation", "modify-translation",
            "review-translation", "translation-review" })
    public boolean translationMaintainerCanTranslate(HProject project,
                                                            HLocale locale) {
        return isLoggedIn() && isProjectTranslationMaintainer(
                project);
    }

    /* Project Translation Reviewer can perform translation and review for their
     * language in the project, regardless of global translation permission.
     */
    @GrantsPermission(actions = { "add-translation", "modify-translation",
            "review-translation", "translation-review" })
    public boolean projectReviewerCanTranslateAndReview(HProject project, HLocale lang) {
        return isLoggedIn() &&
                userHasProjectLanguageRole(project, lang, LocaleRole.Reviewer);
    }

    /* Project Maintainer can import translation (merge type is IMPORT) */
    @GrantsPermission(actions = "import-translation")
    public boolean canImportTranslation(
            HProjectIteration projectIteration) {
        return isLoggedIn()
                && isProjectMaintainer(projectIteration.getProject());
    }

    /* Project Translation Maintainer can import translation (merge type is IMPORT) */
    @GrantsPermission(actions = "import-translation")
    public boolean translationMaintainerCanImportTranslation(
            HProjectIteration projectIteration) {
        return isLoggedIn()
                && isProjectTranslationMaintainer(projectIteration.getProject());
    }

    /* Membership in global language teams. */
    public boolean isLanguageTeamMember(HLocale lang) {
        PersonDAO personDAO =
                ServiceLocator.instance().getInstance(PersonDAO.class);

        if (isLoggedIn()) {
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
    public boolean canPushGlossary() {
        return identity.hasRole("glossarist");
    }

    /* 'glossarist-admin' can also delete */
    @GrantsPermission(actions = { "glossary-insert", "glossary-update",
            "glossary-delete" })
    public boolean canAdminGlossary() {
        return identity.hasRole("glossary-admin");
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
    public boolean isUserCoordinatorOfLanguage(HLocale lang) {
        PersonDAO personDAO =
                ServiceLocator.instance().getInstance(PersonDAO.class);

        if (isLoggedIn()) {
            return personDAO.isUserInLanguageTeamWithRoles(
                    authenticatedAccount.get().getPerson(), lang, null, null, true);
        } else {
            return false;
        }

    }

    /* 'team coordinator' can insert/update/delete language team members */
    @GrantsPermission(actions = { "insert", "update", "delete" })
    public boolean canModifyLanguageTeamMembers(
            HLocaleMember localeMember) {
        return isUserCoordinatorOfLanguage(localeMember.getSupportedLanguage());
    }

    /***************************************************************************
     * View Obsolete Project and Project Iteration rules
     **************************************************************************/

    // Only admin can view obsolete projects
    @GrantsPermission(actions = "view-obsolete")
    public boolean canViewObsoleteProject(HProject project) {
        return identity.hasRole("admin");
    }

    // Only admin can view obsolete project iterations
    @GrantsPermission(actions = "view-obsolete")
    public boolean canViewObsoleteProjectIteration(
            HProjectIteration projectIteration) {
        return identity.hasRole("admin");
    }

    /***************************************************************************
     * Mark Project and Project Iteration obsolete rules
     **************************************************************************/

    // Project maintainer can archive/delete projects
    @GrantsPermission(actions = "mark-obsolete")
    public boolean canArchiveProject(HProject project) {
        return isProjectMaintainer(project);
    }

    // Project maintainer can archive/delete project iterations
    @GrantsPermission(actions = "mark-obsolete")
    public boolean canArchiveProjectIteration(
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
    public boolean canDownloadFiles(HProjectIteration projectIteration) {
        return identity.isLoggedIn();
    }

    /***************************************************************************
     * Version Group rules
     **************************************************************************/
    @GrantsPermission(actions = "update")
    public boolean canUpdateVersionGroup(HIterationGroup group) {
        return isLoggedIn()
                && authenticatedAccount.get().getPerson().isMaintainer(group);
    }

    @GrantsPermission(actions = "insert")
    public boolean canInsertVersionGroup(HIterationGroup group) {
        return isAdmin();
    }

    @GrantsPermission(actions = "view-obsolete")
    public boolean canViewObsoleteVersionGroup(HIterationGroup group) {
        return isAdmin();
    }

    /***************************************************************************
     * Copy Trans rules
     **************************************************************************/

    /** Admins and Project maintainers can perform copy-trans */
    @GrantsPermission(actions = "copy-trans")
    public boolean canRunCopyTrans(HProjectIteration iteration) {
        return isLoggedIn()
                && isProjectMaintainer(iteration.getProject());
    }

    /*****************************************************************************************
     * Review comment rules
     ******************************************************************************************/

    @GrantsPermission(actions = "review-comment")
    public boolean canCommentOnReview(HLocale locale, HProject project) {
        return project.isAllowGlobalTranslation() &&
                isUserAllowedAccess(project) && isLanguageTeamMember(locale);
    }

    @GrantsPermission(actions = "review-comment")
    public boolean canMaintainerCommentOnReview(HLocale locale,
            HProject project) {
        return isLoggedIn() && isProjectMaintainer(project);
    }

    @GrantsPermission(actions = "review-comment")
    public boolean canTranslationMaintainerCommentOnReview(HLocale locale,
            HProject project) {
        return isLoggedIn()
                && isProjectTranslationMaintainer(project);
    }

    @GrantsPermission(actions = "review-comment")
    public boolean canReviewerCommentOnReview(HLocale locale,
            HProject project) {
        return isLoggedIn() &&
                userHasProjectLanguageRole(project, locale, LocaleRole.Reviewer);
    }

    @GrantsPermission(actions = "review-comment")
    public boolean canTranslatorCommentOnReview(HLocale locale,
            HProject project) {
        return isLoggedIn() &&
                userHasProjectLanguageRole(project, locale, LocaleRole.Translator);
    }

    /*****************************************************************************************
     * TMX rules
     ******************************************************************************************/

    @GrantsPermission(actions = "download-tmx")
    public boolean canDownloadTMX() {
        return isLoggedIn();
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
        // TODO temporary hack while we still check for apiKey or authenticated user
        if (restServicePath.matches(".*/oauth/.*")) {
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
