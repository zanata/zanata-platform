/*
 * Copyright 2015, Red Hat, Inc. and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.zanata.action;

import static org.zanata.webhook.events.ProjectMaintainerChangedEvent.ChangeType;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Lists;
import org.apache.commons.lang.StringUtils;
import javax.enterprise.inject.Model;
import javax.faces.bean.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import org.apache.deltaspike.jpa.api.transaction.Transactional;
import org.zanata.dao.PersonDAO;
import org.zanata.dao.ProjectDAO;
import org.zanata.exception.AuthorizationException;
import org.zanata.i18n.Messages;
import org.zanata.model.HLocale;
import org.zanata.model.HPerson;
import org.zanata.model.HProject;
import org.zanata.model.HProjectLocaleMember;
import org.zanata.model.HProjectMember;
import org.zanata.model.LocaleRole;
import org.zanata.model.PersonProjectMemberships;
import org.zanata.model.ProjectRole;
import org.zanata.model.WebHook;
import org.zanata.security.ZanataIdentity;
import org.zanata.service.LocaleService;
import org.zanata.service.ProjectService;
import org.zanata.service.impl.LocaleServiceImpl;
import org.zanata.service.impl.ProjectServiceImpl;
import org.zanata.service.impl.WebhookServiceImpl;
import org.zanata.ui.AbstractAutocomplete;
import org.zanata.ui.faces.FacesMessages;
import org.zanata.util.ServiceLocator;
import javax.faces.application.FacesMessage;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
/*
 * Backing bean for project permissions dialog.
 *
 * Template is person-permissions-modal.xhtml
 */

@Named("projectPermissionDialog")
@ViewScoped
@Model
@Transactional
public class ProjectPermissionDialog extends AbstractAutocomplete<HPerson>
        implements Serializable {
    private static final org.slf4j.Logger log =
            org.slf4j.LoggerFactory.getLogger(ProjectPermissionDialog.class);

    @Inject
    private FacesMessages facesMessages;
    @Inject
    private Messages msgs;
    @Inject
    private ZanataIdentity identity;
    @Inject
    private LocaleService localeServiceImpl;
    @Inject
    private WebhookServiceImpl webhookServiceImpl;
    @Inject
    private PersonDAO personDAO;
    @Inject
    private ProjectDAO projectDAO;
    @Inject
    private ProjectService projectServiceImpl;
    private PersonProjectMemberships data;
    private HProject project;

    public void setData(HProject project, HPerson person) {
        this.project = project;
        setPerson(person);
        // reset autocomplete data
        this.reset();
    }

    /**
     * Prepare the permission dialog to update permissions for the given person.
     *
     * @param person
     *            to show in permission dialog
     */
    private void setPerson(HPerson person) {
        final List<ProjectRole> projectRoles = new ArrayList<>();
        for (HProjectMember membership : getProject().getMembers()) {
            if (membership.getPerson().equals(person)) {
                projectRoles.add(membership.getRole());
            }
        }
        final ListMultimap<HLocale, LocaleRole> localeRoles =
                ArrayListMultimap.create();
        for (HProjectLocaleMember membership : getProject()
                .getLocaleMembers()) {
            if (membership.getPerson().equals(person)) {
                localeRoles.put(membership.getLocale(), membership.getRole());
            }
        }
        data = new PersonProjectMemberships(person, projectRoles, localeRoles);
        LocaleService localeServiceImpl =
                ServiceLocator.instance().getInstance(LocaleServiceImpl.class);
        List<HLocale> locales = localeServiceImpl
                .getSupportedLanguageByProject(getProject().getSlug());
        data.ensureLocalesPresent(locales);
    }

    /**
     * Ensure that the permission dialog has no person selected.
     */
    public void clearPerson() {
        data = null;
    }

    /**
     * Check whether the selected person is the last maintainer of the project.
     */
    public boolean lastMaintainerSelected() {
        if (data == null || data.getPerson() == null) {
            return false;
        }
        project = projectDAO.findById(getProject().getId());
        return project.getMaintainers().size() <= 1
                && project.getMaintainers().contains(data.getPerson());
    }

    /**
     * Indicate whether any of the permissions are selected in the person
     * permission dialog.
     *
     * @return false if no permissions are selected in the dialog, otherwise
     *         true
     */
    public boolean anySelected() {
        return data != null && data.hasAnyPermissions();
    }

    /**
     * Update role membership for a project-specific role based on the current
     * checkbox value.
     *
     * @param role
     *            the role to assign or remove for the current person
     * @param checked
     *            the current checkbox value
     */
    public void bindProjectRole(String role, boolean checked) {
        if (StringUtils.equalsIgnoreCase(role,
                ProjectRole.TranslationMaintainer.name())) {
            data.setTranslationMaintainer(checked);
        } else if (StringUtils.equalsIgnoreCase(role,
                ProjectRole.Maintainer.name())) {
            data.setMaintainer(checked);
        }
    }

    /**
     * Update role membership for a locale-specific role based on the current
     * checkbox value.
     *
     * @param localeRole
     *            represents both the locale and the role format:
     *            {localeId}:{role}. e.g en-US:Reviewer
     * @param checked
     *            the current checkbox value
     */
    public void bindTranslationRole(String localeRole, boolean checked) {
        String[] localeRoleList = StringUtils.split(localeRole, ':');
        final HLocale hLocale =
                localeServiceImpl.getByLocaleId(localeRoleList[0]);
        String role = localeRoleList[1];
        final Optional<PersonProjectMemberships.LocaleRoles> matchingLocaleRoles =
                Iterables.tryFind(data.getLocaleRoles(),
                        localeEqualsPredicate(hLocale));
        if (matchingLocaleRoles.isPresent()) {
            PersonProjectMemberships.LocaleRoles localeRoles =
                    matchingLocaleRoles.get();
            if (StringUtils.equalsIgnoreCase(role,
                    LocaleRole.Translator.name())) {
                localeRoles.setTranslator(checked);
            } else if (StringUtils.equalsIgnoreCase(role,
                    LocaleRole.Reviewer.name())) {
                localeRoles.setReviewer(checked);
            } else if (StringUtils.equalsIgnoreCase(role,
                    LocaleRole.Coordinator.name())) {
                localeRoles.setCoordinator(checked);
            } else if (StringUtils.equalsIgnoreCase(role,
                    LocaleRole.Glossarist.name())) {
                localeRoles.setGlossarist(checked);
            }
        } else {
            // No LocaleRoles for the given locale, so create a new one.
            List<LocaleRole> roleList =
                    Lists.newArrayList(LocaleRole.valueOf(role));
            data.addLocaleRoles(hLocale, roleList);
        }
    }

    /**
     * Get a predicate that checks if a LocaleRoles.getLocale() is the given
     * locale.
     */
    private Predicate<PersonProjectMemberships.LocaleRoles>
            localeEqualsPredicate(final HLocale hLocale) {
        return new Predicate<PersonProjectMemberships.LocaleRoles>() {

            @Override
            public boolean apply(PersonProjectMemberships.LocaleRoles input) {
                return input.getLocale().equals(hLocale);
            }
        };
    }

    /**
     * Save the permissions selections from permissionDialogData to the
     * database.
     */
    @Transactional
    public void saveSelections() {
        if (data == null) {
            log.error("Tried to save permissionDialogData but it is null");
            return;
        }
        project = projectDAO.findById(getProject().getId());
        // Hibernate will have problems working with detached HPerson and
        // HLocale
        // so they are all attached before any persistence is attempted.
        HPerson person = personDAO.findById(data.getPerson().getId());
        data.setPerson(person);
        for (PersonProjectMemberships.LocaleRoles roles : data
                .getLocaleRoles()) {
            roles.setLocale(localeServiceImpl
                    .getByLocaleId(roles.getLocale().getLocaleId()));
        }
        final boolean canManageMembers =
                identity.hasPermission(project, "manage-members");
        final boolean canManageTransMembers =
                identity.hasPermission(project, "manage-translation-members");
        final boolean canChangeAnyMembers =
                canManageMembers || canManageTransMembers;
        List<ProjectServiceImpl.UpdatedRole> updatedRoles = null;
        if (canManageMembers) {
            // generate a warning if trying to remove last maintainer
            // business rule in project will prevent actual removal
            if (!data.isMaintainer() && project.getMaintainers().size() <= 1
                    && project.getMaintainers().contains(data.getPerson())) {
                facesMessages.addGlobal(FacesMessage.SEVERITY_INFO,
                        msgs.get("jsf.project.NeedAtLeastOneMaintainer"));
            }
            updatedRoles =
                    projectServiceImpl.updateProjectPermissions(project, data);
        }
        if (canManageTransMembers) {
            projectServiceImpl.updateLocalePermissions(project, data);
        }
        if (canChangeAnyMembers) {
            projectDAO.makePersistent(project);
            if (!updatedRoles.isEmpty()) {
                List<WebHook> webHooks = project.getWebHooks();
                for (ProjectServiceImpl.UpdatedRole updatedRole : updatedRoles) {
                    ChangeType changeType = updatedRole.isAdded()
                            ? ChangeType.ADD : ChangeType.REMOVE;
                    webhookServiceImpl.processWebhookMaintainerChanged(
                            project.getSlug(), updatedRole.getUsername(),
                            updatedRole.getRole(), webHooks, changeType);
                }
            }
        } else {
            throw new AuthorizationException(
                    "You are not authorized to manage permissions for this project.");
        }
    }

    @Override
    public List<HPerson> suggest() {
        return getPersonDAO().findAllContainingName(getQuery());
    }

    private PersonDAO getPersonDAO() {
        return ServiceLocator.instance().getInstance(PersonDAO.class);
    }

    @Override
    public void onSelectItemAction() {
        String selected = getSelectedItem();
        HPerson selectedPerson = getPersonDAO().findByUsername(selected);
        if (selectedPerson != null) {
            setPerson(selectedPerson);
        }
    }

    public PersonProjectMemberships getData() {
        return this.data;
    }

    public HProject getProject() {
        return this.project;
    }
}
