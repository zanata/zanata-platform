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
package org.zanata.action;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import javax.faces.application.FacesMessage;
import javax.faces.event.ValueChangeEvent;
import javax.persistence.EntityManager;
import javax.persistence.EntityNotFoundException;

import lombok.Getter;
import lombok.Setter;

import org.apache.commons.lang.StringUtils;
import org.hibernate.Session;
import org.hibernate.criterion.NaturalIdentifier;
import org.hibernate.criterion.Restrictions;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Transactional;
import org.jboss.seam.annotations.security.Restrict;
import org.jboss.seam.security.management.JpaIdentityStore;
import org.zanata.common.EntityStatus;
import org.zanata.common.LocaleId;
import org.zanata.common.ProjectType;
import org.zanata.dao.AccountRoleDAO;
import org.zanata.dao.WebHookDAO;
import org.zanata.events.ProjectIterationUpdate;
import org.zanata.events.ProjectUpdate;
import org.zanata.i18n.Messages;
import org.zanata.model.HAccount;
import org.zanata.model.HAccountRole;
import org.zanata.model.HLocale;
import org.zanata.model.HPerson;
import org.zanata.model.HProject;
import org.zanata.model.HProjectIteration;
import org.zanata.model.WebHook;
import org.zanata.seam.scope.ConversationScopeMessages;
import org.zanata.security.ZanataIdentity;
import org.zanata.service.LocaleService;
import org.zanata.service.SlugEntityService;
import org.zanata.service.ValidationService;
import org.zanata.ui.AbstractListFilter;
import org.zanata.ui.InMemoryListFilter;
import org.zanata.ui.autocomplete.LocaleAutocomplete;
import org.zanata.ui.autocomplete.MaintainerAutocomplete;
import org.zanata.ui.faces.FacesMessages;
import org.zanata.util.ComparatorUtil;
import org.zanata.util.Event;
import org.zanata.util.UrlUtil;
import org.zanata.webtrans.shared.model.ValidationAction;
import org.zanata.webtrans.shared.model.ValidationId;
import org.zanata.webtrans.shared.validation.ValidationFactory;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import static javax.faces.application.FacesMessage.SEVERITY_ERROR;

@Name("projectHome")
public class ProjectHome extends SlugHome<HProject> {
    private static final long serialVersionUID = 1L;

    public static final String PROJECT_UPDATE = "project.update";

    @Getter
    @Setter
    private String slug;

    @In
    private ZanataIdentity identity;

    @In(required = false, value = JpaIdentityStore.AUTHENTICATED_USER)
    private HAccount authenticatedAccount;

    @In
    private LocaleService localeServiceImpl;

    @In
    private SlugEntityService slugEntityServiceImpl;

    @In
    private ConversationScopeMessages conversationScopeMessages;

    @In
    private EntityManager entityManager;

    @In("jsfMessages")
    private FacesMessages facesMessages;

    @In("event")
    private Event<ProjectUpdate> projectUpdateEvent;

    @In("event")
    private Event<ProjectIterationUpdate> projectIterationUpdateEvent;

    @In
    private Messages msgs;

    @In
    private AccountRoleDAO accountRoleDAO;

    @In
    private WebHookDAO webHookDAO;

    @In
    private ValidationService validationServiceImpl;

    @In
    private CopyTransOptionsModel copyTransOptionsModel;

    private Map<String, Boolean> roleRestrictions;

    private Map<ValidationId, ValidationAction> availableValidations = Maps
            .newHashMap();

    @Getter(lazy = true)
    private final List<HProjectIteration> versions = fetchVersions();

    @Getter
    @Setter
    private String selectedProjectType;

    @Getter
    private ProjectMaintainersAutocomplete maintainerAutocomplete =
            new ProjectMaintainersAutocomplete();

    @Getter
    private ProjectLocaleAutocomplete localeAutocomplete =
            new ProjectLocaleAutocomplete();

    @Getter
    private AbstractListFilter<HPerson> maintainerFilter =
            new InMemoryListFilter<HPerson>() {
                @Override
                protected List<HPerson> fetchAll() {
                    return getInstanceMaintainers();
                }

                @Override
                protected boolean include(HPerson elem, String filter) {
                    return StringUtils.containsIgnoreCase(elem.getName(),
                            filter);
                }
            };

    public void createNew() {
        getInstance().setDefaultProjectType(ProjectType.File);
        selectedProjectType = getInstance().getDefaultProjectType().name();
    }

    public void updateSelectedProjectType(ValueChangeEvent e) {
        selectedProjectType = (String) e.getNewValue();
        updateProjectType();
    }

    public void setSelectedProjectType(String selectedProjectType) {
        if (!StringUtils.isEmpty(selectedProjectType)
                && !selectedProjectType.equals("null")) {
            ProjectType projectType = ProjectType.valueOf(selectedProjectType);
            getInstance().setDefaultProjectType(projectType);
        } else {
            getInstance().setDefaultProjectType(null);
        }
    }

    public List<HLocale> getInstanceActiveLocales() {
        List<HLocale> locales;
        if (StringUtils.isNotEmpty(getSlug())) {
            locales =
                    localeServiceImpl.getSupportedLanguageByProject(getSlug());
        } else {
            locales = localeServiceImpl.getSupportedAndEnabledLocales();
        }
        Collections.sort(locales, ComparatorUtil.LOCALE_COMPARATOR);
        return locales;
    }

    @Restrict("#{s:hasPermission(projectHome.instance, 'update')}")
    public void removeLanguage(LocaleId localeId) {
        HLocale locale = localeServiceImpl.getByLocaleId(localeId);

        if (getInstance().isOverrideLocales()) {
            getInstance().getCustomizedLocales().remove(locale);
        } else {
            getInstance().getCustomizedLocales().clear();
            for (HLocale activeLocale : getInstanceActiveLocales()) {
                if (!activeLocale.equals(locale)) {
                    getInstance().getCustomizedLocales().add(activeLocale);
                }
            }
            getInstance().setOverrideLocales(true);
        }
        update();
        conversationScopeMessages.setMessage(
                FacesMessage.SEVERITY_INFO,
                msgs.format("jsf.project.LanguageRemoved",
                        locale.retrieveDisplayName()));
    }

    @Restrict("#{s:hasPermission(projectHome.instance, 'update')}")
    public void updateLanguagesFromGlobal() {
        getInstance().setOverrideLocales(false);
        update();
        conversationScopeMessages.setMessage(FacesMessage.SEVERITY_INFO,
                msgs.get("jsf.project.LanguageUpdateFromGlobal"));
    }

    @Restrict("#{s:hasPermission(projectHome.instance, 'update')}")
    public void setRestrictedByRole(String key, boolean checked) {
        getInstance().setRestrictedByRoles(checked);
        update();
    }

    @Override
    protected HProject loadInstance() {
        Session session = (Session) getEntityManager().getDelegate();
        return (HProject) session.byNaturalId(HProject.class)
                .using("slug", getSlug()).load();
    }

    public void validateSuppliedId() {
        HProject ip = getInstance(); // this will raise an EntityNotFound
                                     // exception
        // when id is invalid and conversation will not
        // start

        if (ip.getStatus().equals(EntityStatus.OBSOLETE)
                && !checkViewObsolete()) {
            throw new EntityNotFoundException();
        }
    }

    @Transactional
    public void updateCopyTrans(String action, String value) {
        copyTransOptionsModel.setInstance(getInstance()
                .getDefaultCopyTransOpts());
        copyTransOptionsModel.update(action, value);
        copyTransOptionsModel.save();
        getInstance().setDefaultCopyTransOpts(
                copyTransOptionsModel.getInstance());

        update();

        conversationScopeMessages.setMessage(FacesMessage.SEVERITY_INFO,
                msgs.get("jsf.project.CopyTransOpts.updated"));
    }

    public void initialize() {
        initInstance();
        validateSuppliedId();
        if (getInstance().getDefaultCopyTransOpts() != null) {
            copyTransOptionsModel.setInstance(getInstance()
                    .getDefaultCopyTransOpts());
        }
    }

    public void verifySlugAvailable(ValueChangeEvent e) {
        String slug = (String) e.getNewValue();
        validateSlug(slug, e.getComponent().getId());
    }

    public boolean validateSlug(String slug, String componentId) {
        if (!isSlugAvailable(slug)) {
            facesMessages.addToControl(componentId,
                    "This Project ID is not available");
            return false;
        }
        return true;
    }

    public boolean isSlugAvailable(String slug) {
        return slugEntityServiceImpl.isSlugAvailable(slug, HProject.class);
    }

    private void updateProjectType() {
        if (!StringUtils.isEmpty(selectedProjectType)
                && !selectedProjectType.equals("null")) {
            ProjectType projectType = ProjectType.valueOf(selectedProjectType);
            getInstance().setDefaultProjectType(projectType);
        } else {
            getInstance().setDefaultProjectType(null);
        }
    }

    @Override
    @Transactional
    public String persist() {
        String retValue = "";
        if (!validateSlug(getInstance().getSlug(), "slug")) {
            return null;
        }

        if (StringUtils.isEmpty(selectedProjectType)
                || selectedProjectType.equals("null")) {
            facesMessages.addGlobal(SEVERITY_ERROR,
                    "Project type not selected");
            return null;
        }

        if (StringUtils.isEmpty(selectedProjectType)
                || selectedProjectType.equals("null")) {
            facesMessages.addGlobal(SEVERITY_ERROR,
                    "Project type not selected");
            return null;
        }
        updateProjectType();

        if (authenticatedAccount != null) {
            getInstance().addMaintainer(authenticatedAccount.getPerson());
            getInstance().getCustomizedValidations().clear();
            for (ValidationAction validationAction : validationServiceImpl
                    .getValidationActions("")) {
                getInstance().getCustomizedValidations().put(
                        validationAction.getId().name(),
                        validationAction.getState().name());
            }
            retValue = super.persist();
            projectUpdateEvent.fire(new ProjectUpdate(getInstance()));
        }
        return retValue;
    }

    public List<HPerson> getInstanceMaintainers() {
        List<HPerson> list = Lists.newArrayList(getInstance().getMaintainers());
        Collections.sort(list, ComparatorUtil.PERSON_NAME_COMPARATOR);
        return list;
    }

    @Restrict("#{s:hasPermission(projectHome.instance, 'update')}")
    public String removeMaintainer(HPerson person) {
        if (getInstanceMaintainers().size() <= 1) {
            conversationScopeMessages
                    .setMessage(FacesMessage.SEVERITY_INFO,
                            msgs.get("jsf.project.NeedAtLeastOneMaintainer"));
        } else {
            getInstance().getMaintainers().remove(person);
            maintainerFilter.reset();
            update();

            conversationScopeMessages.setMessage(FacesMessage.SEVERITY_INFO,
                    msgs.format("jsf.project.MaintainerRemoved",
                            person.getName()));

            // force page to do url redirect to project page. See pages.xml
            if (person.equals(authenticatedAccount.getPerson())) {
                return "redirect";
            }
        }
        return "";
    }

    @Restrict("#{s:hasPermission(projectHome.instance, 'update')}")
    public void updateRoles(String roleName, boolean isRestricted) {
        getInstance().getAllowedRoles().clear();
        if (getInstance().isRestrictedByRoles()) {
            getRoleRestrictions().put(roleName, isRestricted);

            for (Map.Entry<String, Boolean> entry : getRoleRestrictions()
                    .entrySet()) {
                if (entry.getValue()) {
                    getInstance().getAllowedRoles().add(
                            accountRoleDAO.findByName(entry.getKey()));
                }
            }
        }
        update();
        conversationScopeMessages.setMessage(FacesMessage.SEVERITY_INFO,
                msgs.get("jsf.RolesUpdated"));
    }

    @Restrict("#{s:hasPermission(projectHome.instance, 'update')}")
    public void updateStatus(char initial) {
        getInstance().setStatus(EntityStatus.valueOf(initial));
        if (getInstance().getStatus() == EntityStatus.READONLY) {
            for (HProjectIteration version : getInstance()
                    .getProjectIterations()) {
                if (version.getStatus() == EntityStatus.ACTIVE) {
                    version.setStatus(EntityStatus.READONLY);
                    entityManager.merge(version);
                    projectIterationUpdateEvent.fire(new ProjectIterationUpdate(version));
                }
            }
        } else if (getInstance().getStatus() == EntityStatus.OBSOLETE) {
            for (HProjectIteration version : getInstance()
                    .getProjectIterations()) {
                if (version.getStatus() != EntityStatus.OBSOLETE) {
                    version.setStatus(EntityStatus.OBSOLETE);
                    entityManager.merge(version);
                    projectIterationUpdateEvent.fire(
                            new ProjectIterationUpdate(version));
                }
            }
        }
        update();

        conversationScopeMessages.setMessage(FacesMessage.SEVERITY_INFO,
                msgs.format("jsf.project.status.updated",
                        EntityStatus.valueOf(initial)));
    }

    public Map<String, Boolean> getRoleRestrictions() {
        if (roleRestrictions == null) {
            roleRestrictions = Maps.newHashMap();

            for (HAccountRole role : getInstance().getAllowedRoles()) {
                roleRestrictions.put(role.getName(), true);
            }
        }
        return roleRestrictions;
    }

    public boolean isRoleRestrictionEnabled(String roleName) {
        if (getRoleRestrictions().containsKey(roleName)) {
            return getRoleRestrictions().get(roleName);
        }
        return false;
    }

    public List<HAccountRole> getAvailableRoles() {
        List<HAccountRole> allRoles = accountRoleDAO.findAll();
        Collections.sort(allRoles, ComparatorUtil.ACCOUNT_ROLE_COMPARATOR);
        return allRoles;
    }

    private List<HProjectIteration> fetchVersions() {
        List<HProjectIteration> results = new ArrayList<HProjectIteration>();

        for (HProjectIteration iteration : getInstance().getProjectIterations()) {
            if (iteration.getStatus() == EntityStatus.OBSOLETE
                    && checkViewObsolete()) {
                results.add(iteration);
            } else if (iteration.getStatus() != EntityStatus.OBSOLETE) {
                results.add(iteration);
            }
        }
        Collections.sort(results, new Comparator<HProjectIteration>() {
            @Override
            public int compare(HProjectIteration o1, HProjectIteration o2) {
                EntityStatus fromStatus = o1.getStatus();
                EntityStatus toStatus = o2.getStatus();

                if (fromStatus.equals(toStatus)) {
                    return 0;
                }

                if (fromStatus.equals(EntityStatus.ACTIVE)) {
                    return -1;
                }

                if (fromStatus.equals(EntityStatus.READONLY)) {
                    if (toStatus.equals(EntityStatus.ACTIVE)) {
                        return 1;
                    }
                    return -1;
                }

                if (fromStatus.equals(EntityStatus.OBSOLETE)) {
                    return 1;
                }

                return 0;
            }
        });
        return results;
    }

    @Override
    public boolean isIdDefined() {
        return slug != null;
    }

    @Override
    public NaturalIdentifier getNaturalId() {
        return Restrictions.naturalId().set("slug", slug);
    }

    @Override
    public Object getId() {
        return slug;
    }

    private Map<ValidationId, ValidationAction> getValidations() {
        if (availableValidations.isEmpty()) {
            Collection<ValidationAction> validationList =
                    validationServiceImpl.getValidationActions(slug);

            for (ValidationAction validationAction : validationList) {
                availableValidations.put(validationAction.getId(),
                        validationAction);
            }
        }

        return availableValidations;
    }

    @Restrict("#{s:hasPermission(projectHome.instance, 'update')}")
    public void updateValidationOption(String name, String state) {
        ValidationId validatationId = ValidationId.valueOf(name);

        for (Map.Entry<ValidationId, ValidationAction> entry : getValidations()
                .entrySet()) {
            if (entry.getKey().name().equals(name)) {
                getValidations().get(validatationId).setState(
                        ValidationAction.State.valueOf(state));
                getInstance().getCustomizedValidations().put(
                        entry.getKey().name(),
                        entry.getValue().getState().name());
                ensureMutualExclusivity(getValidations().get(validatationId));
                break;
            }
        }
        update();

        conversationScopeMessages.setMessage(FacesMessage.SEVERITY_INFO,
                msgs.format("jsf.validation.updated",
                        validatationId.getDisplayName(), state));
    }

    public List<ValidationAction> getValidationList() {
        List<ValidationAction> sortedList =
                Lists.newArrayList(getValidations().values());
        Collections.sort(sortedList,
                ValidationFactory.ValidationActionComparator);
        return sortedList;
    }

    @Restrict("#{s:hasPermission(projectHome.instance, 'update')}")
    public void addWebHook(String url) {
        if (isValidUrl(url)) {
            WebHook webHook = new WebHook(this.getInstance(), url);
            getInstance().getWebHooks().add(webHook);
            update();
            facesMessages.addGlobal(
                msgs.format("jsf.project.AddNewWebhook", webHook.getUrl()));
        }
    }

    @Restrict("#{s:hasPermission(projectHome.instance, 'update')}")
    public void removeWebHook(WebHook webHook) {
        getInstance().getWebHooks().remove(webHook);
        webHookDAO.makeTransient(webHook);
        facesMessages.addGlobal(
            msgs.format("jsf.project.RemoveWebhook", webHook.getUrl()));
    }

    private boolean isValidUrl(String url) {
        if (!UrlUtil.isValidUrl(url)) {
            facesMessages.addGlobal(SEVERITY_ERROR,
                    msgs.format("jsf.project.InvalidUrl", url));
            return false;
        }
        for(WebHook webHook: getInstance().getWebHooks()) {
            if(StringUtils.equalsIgnoreCase(webHook.getUrl(), url)) {
                facesMessages.addGlobal(SEVERITY_ERROR,
                        msgs.format("jsf.project.DuplicateUrl", url));
                return false;
            }
        }
        return true;
    }

    /**
     * If this action is enabled(Warning or Error), then it's exclusive
     * validation will be turn off
     *
     * @param selectedValidationAction
     */
    private void ensureMutualExclusivity(
            ValidationAction selectedValidationAction) {
        if (selectedValidationAction.getState() != ValidationAction.State.Off) {
            for (ValidationAction exclusiveValAction : selectedValidationAction
                    .getExclusiveValidations()) {
                getInstance().getCustomizedValidations().put(
                        exclusiveValAction.getId().name(),
                        ValidationAction.State.Off.name());
                getValidations().get(exclusiveValAction.getId()).setState(
                        ValidationAction.State.Off);
            }
        }
    }

    public List<ValidationAction.State> getValidationStates() {
        return Arrays.asList(ValidationAction.State.values());
    }


    /**
     * This is for autocomplete components of which ConversationScopeMessages
     * will be null
     *
     * @param conversationScopeMessages
     */
    private String update(ConversationScopeMessages conversationScopeMessages) {
        if (this.conversationScopeMessages == null) {
            this.conversationScopeMessages = conversationScopeMessages;
        }
        return update();
    }

    private boolean checkViewObsolete() {
        return identity != null
                && identity.hasPermission("HProject", "view-obsolete");
    }

    private class ProjectMaintainersAutocomplete extends MaintainerAutocomplete {

        @Override
        protected List<HPerson> getMaintainers() {
            return getInstanceMaintainers();
        }

        /**
         * Action when an item is selected
         */
        @Override
        public void onSelectItemAction() {
            if (StringUtils.isEmpty(getSelectedItem())) {
                return;
        }


            HPerson maintainer = personDAO.findByUsername(getSelectedItem());
            getInstance().addMaintainer(maintainer);
            update(conversationScopeMessages);
            reset();

            conversationScopeMessages.setMessage(FacesMessage.SEVERITY_INFO,
                    msgs.format("jsf.project.MaintainerAdded",
                            maintainer.getName()));
        }
    }

    private class ProjectLocaleAutocomplete extends LocaleAutocomplete {

        @Override
        protected Collection<HLocale> getLocales() {
            return localeServiceImpl.getSupportedLanguageByProject(getSlug());
        }

        /**
         * Action when an item is selected
         */
        @Override
        public void onSelectItemAction() {
            if (StringUtils.isEmpty(getSelectedItem())) {
                return;
            }

            HLocale locale = localeServiceImpl.getByLocaleId(getSelectedItem());

            if (!getInstance().isOverrideLocales()) {
                getInstance().setOverrideLocales(true);
                getInstance().getCustomizedLocales().clear();
                getInstance().getCustomizedLocales().addAll(supportedLocales);
            }
            getInstance().getCustomizedLocales().add(locale);

            update(conversationScopeMessages);
            reset();
            conversationScopeMessages.setMessage(FacesMessage.SEVERITY_INFO,
                    msgs.format("jsf.project.LanguageAdded",
                            locale.retrieveDisplayName()));
        }
    }

    public List<ProjectType> getProjectTypeList() {
        List<ProjectType> projectTypes = Arrays.asList(ProjectType.values());
        Collections.sort(projectTypes, ComparatorUtil.PROJECT_TYPE_COMPARATOR);
        return projectTypes;
    }
}
