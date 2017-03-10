/*
 * Copyright 2016, Red Hat, Inc. and individual contributors as indicated by the
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
package org.zanata.service.impl;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import org.apache.commons.lang.StringUtils;
import org.apache.deltaspike.jpa.api.transaction.Transactional;
import org.zanata.dao.ProjectDAO;
import org.zanata.dao.WebHookDAO;
import org.zanata.model.HLocale;
import org.zanata.model.HPerson;
import org.zanata.model.HProject;
import org.zanata.model.HProjectLocaleMember;
import org.zanata.model.HProjectMember;
import org.zanata.model.LocaleRole;
import org.zanata.model.PersonProjectMemberships;
import org.zanata.model.ProjectRole;
import org.zanata.model.WebHook;
import org.zanata.model.type.WebhookType;
import org.zanata.service.ProjectService;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import static org.zanata.model.LocaleRole.Coordinator;
import static org.zanata.model.LocaleRole.Reviewer;
import static org.zanata.model.LocaleRole.Translator;
import static org.zanata.model.ProjectRole.Maintainer;
import static org.zanata.model.ProjectRole.TranslationMaintainer;

/**
 * @author Alex Eng<a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
@Named("projectServiceImpl")
@RequestScoped
public class ProjectServiceImpl implements ProjectService {
    private static final org.slf4j.Logger log =
            org.slf4j.LoggerFactory.getLogger(ProjectServiceImpl.class);

    @Inject
    private ProjectDAO projectDAO;
    @Inject
    private WebHookDAO webHookDAO;

    @Override
    public List<UpdatedRole> updateProjectPermissions(HProject project,
            PersonProjectMemberships memberships) {
        HPerson person = memberships.getPerson();
        boolean wasMaintainer =
                project.getMaintainers().contains(memberships.getPerson());
        boolean isLastMaintainer =
                wasMaintainer && project.getMaintainers().size() <= 1;
        // business rule: every project must have at least one maintainer
        boolean isMaintainer = isLastMaintainer || memberships.isMaintainer();
        List<UpdatedRole> updatedRoles = Lists.newArrayList();
        Optional<UpdatedRole> updatedMaintainerRole = ensureMembership(project,
                isMaintainer, asMember(project, person, Maintainer));
        if (updatedMaintainerRole.isPresent()) {
            updatedRoles.add(updatedMaintainerRole.get());
        }
        // business rule: if someone is a Maintainer, they must also be a
        // TranslationMaintainer
        boolean isTranslationMaintainer = memberships.isMaintainer()
                || memberships.isTranslationMaintainer();
        Optional<UpdatedRole> updatedTranslationMaintainer =
                ensureMembership(project, isTranslationMaintainer,
                        asMember(project, person, TranslationMaintainer));
        if (updatedTranslationMaintainer.isPresent()) {
            updatedRoles.add(updatedTranslationMaintainer.get());
        }
        return updatedRoles;
    }

    @Transactional
    @Override
    public boolean updateWebhook(HProject project, Long webhookId, String url,
            String secret, String name, Set<WebhookType> types) {
        if (types.isEmpty()) {
            return false;
        }
        if (project == null) {
            return false;
        }
        WebHook webHook = webHookDAO.findById(webhookId);
        if (webHook == null) {
            return false;
        }
        secret = StringUtils.isBlank(secret) ? null : secret;
        webHook.update(url, Strings.emptyToNull(name), types, secret);
        webHookDAO.makePersistent(webHook);
        return true;
    }

    @Transactional
    @Override
    public boolean addWebhook(HProject project, String url, String secret,
            String name, Set<WebhookType> types) {
        if (types.isEmpty()) {
            return false;
        }
        if (project == null) {
            return false;
        }
        secret = StringUtils.isBlank(secret) ? null : secret;
        WebHook webHook = new WebHook(project, url, Strings.emptyToNull(name),
                types, secret);
        project.getWebHooks().add(webHook);
        projectDAO.makePersistent(project);
        return true;
    }

    @Override
    public boolean isDuplicateWebhookUrl(HProject project, String url) {
        for (WebHook webHook : project.getWebHooks()) {
            if (StringUtils.equalsIgnoreCase(webHook.getUrl(), url)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isDuplicateWebhookUrl(HProject project, String url,
            Long webhookId) {
        for (WebHook webHook : project.getWebHooks()) {
            if (!webhookId.equals(webHook.getId())
                    && StringUtils.equalsIgnoreCase(webHook.getUrl(), url)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void updateLocalePermissions(HProject project,
            PersonProjectMemberships memberships) {
        HPerson person = memberships.getPerson();
        for (PersonProjectMemberships.LocaleRoles localeRoles : memberships
                .getLocaleRoles()) {
            HLocale locale = localeRoles.getLocale();
            ensureMembership(project, localeRoles.isTranslator(),
                    asMember(project, locale, person, Translator));
            ensureMembership(project, localeRoles.isReviewer(),
                    asMember(project, locale, person, Reviewer));
            ensureMembership(project, localeRoles.isCoordinator(),
                    asMember(project, locale, person, Coordinator));
        }
    }

    public class UpdatedRole {
        private String username;
        private ProjectRole role;
        private boolean added;

        public String getUsername() {
            return this.username;
        }

        public ProjectRole getRole() {
            return this.role;
        }

        public boolean isAdded() {
            return this.added;
        }

        @java.beans.ConstructorProperties({ "username", "role", "added" })
        public UpdatedRole(final String username, final ProjectRole role,
                final boolean added) {
            this.username = username;
            this.role = role;
            this.added = added;
        }
    }

    /**
     * Get a person as a member object in this project for a role.
     */
    private HProjectMember asMember(HProject project, HPerson person,
            ProjectRole role) {
        return new HProjectMember(project, person, role);
    }

    /**
     * Get a person as a member object in this project for a locale-specific
     * role.
     */
    private HProjectLocaleMember asMember(HProject project, HLocale locale,
            HPerson person, LocaleRole role) {
        return new HProjectLocaleMember(project, locale, person, role);
    }

    /**
     * Ensure the given membership is present or absent.
     */
    private Optional<UpdatedRole> ensureMembership(HProject project,
            boolean shouldBePresent, HProjectMember membership) {
        UpdatedRole updatedRole = null;
        final Set<HProjectMember> members = project.getMembers();
        final boolean isPresent = members.contains(membership);
        if (isPresent != shouldBePresent) {
            if (shouldBePresent) {
                members.add(membership);
                updatedRole = new UpdatedRole(
                        membership.getPerson().getAccount().getUsername(),
                        membership.getRole(), true);
            } else {
                members.remove(membership);
                updatedRole = new UpdatedRole(
                        membership.getPerson().getAccount().getUsername(),
                        membership.getRole(), false);
            }
        }
        return Optional.ofNullable(updatedRole);
    }

    /**
     * Ensure the given locale membership is present or absent.
     */
    private void ensureMembership(HProject project, boolean shouldBePresent,
            HProjectLocaleMember membership) {
        final Set<HProjectLocaleMember> members = project.getLocaleMembers();
        final boolean isPresent = members.contains(membership);
        if (isPresent != shouldBePresent) {
            if (shouldBePresent) {
                members.add(membership);
            } else {
                members.remove(membership);
            }
        }
    }
}
