/*
 *
 *  * Copyright 2014, Red Hat, Inc. and individual contributors as indicated by the
 *  * @author tags. See the copyright.txt file in the distribution for a full
 *  * listing of individual contributors.
 *  *
 *  * This is free software; you can redistribute it and/or modify it under the
 *  * terms of the GNU Lesser General Public License as published by the Free
 *  * Software Foundation; either version 2.1 of the License, or (at your option)
 *  * any later version.
 *  *
 *  * This software is distributed in the hope that it will be useful, but WITHOUT
 *  * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 *  * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 *  * details.
 *  *
 *  * You should have received a copy of the GNU Lesser General Public License
 *  * along with this software; if not, write to the Free Software Foundation,
 *  * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
 *  * site: http://www.fsf.org.
 */
package org.zanata.action;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.hibernate.Session;
import org.hibernate.criterion.NaturalIdentifier;
import org.hibernate.criterion.Restrictions;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.security.Restrict;
import org.jboss.seam.faces.FacesMessages;
import org.zanata.common.EntityStatus;
import org.zanata.common.LocaleId;
import org.zanata.common.ProjectType;
import org.zanata.dao.ProjectDAO;
import org.zanata.dao.ProjectIterationDAO;
import org.zanata.events.ProjectIterationUpdate;
import org.zanata.i18n.Messages;
import org.zanata.model.HLocale;
import org.zanata.model.HProject;
import org.zanata.model.HProjectIteration;
import org.zanata.seam.scope.ConversationScopeMessages;
import org.zanata.service.LocaleService;
import org.zanata.service.SlugEntityService;
import org.zanata.service.ValidationService;
import org.zanata.ui.autocomplete.LocaleAutocomplete;
import org.zanata.util.ComparatorUtil;
import org.zanata.util.Event;
import org.zanata.webtrans.shared.model.ValidationAction;
import org.zanata.webtrans.shared.model.ValidationId;
import org.zanata.webtrans.shared.validation.ValidationFactory;

import javax.faces.application.FacesMessage;
import javax.faces.event.ValueChangeEvent;
import javax.persistence.EntityNotFoundException;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Name("versionHome")
@Slf4j
public class VersionHome extends SlugHome<HProjectIteration> {

    private static final long serialVersionUID = 1L;

    @Getter
    @Setter
    private String slug;

    @Getter
    @Setter
    private String projectSlug;

    @In
    private ProjectIterationDAO projectIterationDAO;

    @In
    private ConversationScopeMessages conversationScopeMessages;

    @In
    private LocaleService localeServiceImpl;

    @In
    private ValidationService validationServiceImpl;

    @In
    private SlugEntityService slugEntityServiceImpl;

    @In(create = true)
    private ProjectDAO projectDAO;

    @In
    private Messages msgs;

    @In
    private CopyVersionManager copyVersionManager;

    @In("event")
    private Event<ProjectIterationUpdate> projectIterationUpdateEvent;

    private Map<ValidationId, ValidationAction> availableValidations = Maps
            .newHashMap();

    @Getter
    @Setter
    private boolean isNewInstance = false;

    @Setter
    @Getter
    private String selectedProjectType;

    @Getter
    private VersionLocaleAutocomplete localeAutocomplete =
            new VersionLocaleAutocomplete();


    @Getter
    @Setter
    private boolean copyFromVersion = true;

    @Getter
    @Setter
    private String copyFromVersionSlug;

    private final Function<HProjectIteration, VersionItem> VERSION_ITEM_FN =
            new Function<HProjectIteration, VersionItem>() {
                @Override
                public VersionItem apply(HProjectIteration input) {
                    boolean selected = StringUtils.isNotEmpty(
                            copyFromVersionSlug) && copyFromVersionSlug
                            .equals(input.getSlug()) ? true : false;
                    return new VersionItem(selected, input);
                }
            };

    private void setDefaultCopyFromVersion() {
        List<VersionItem> otherVersions = getOtherVersions();
        if (!otherVersions.isEmpty()
                && StringUtils.isEmpty(copyFromVersionSlug)) {
            this.copyFromVersionSlug =
                    otherVersions.get(0).getVersion().getSlug();

        }
        copyFromVersion = true;
    }

    public void init(boolean isNewInstance) {
        this.isNewInstance = isNewInstance;
        if (isNewInstance) {
            ProjectType projectType = getProject().getDefaultProjectType();
            if (projectType != null) {
                selectedProjectType = projectType.name();
            }
            if(StringUtils.isEmpty(copyFromVersionSlug)) {
                setDefaultCopyFromVersion();
            }
        } else {
            copyFromVersion = false;
            ProjectType versionProjectType = getInstance().getProjectType();
            if (versionProjectType != null) {
                selectedProjectType = versionProjectType.name();
            }
            copyFromVersionSlug = "";
        }
    }

    public HProject getProject() {
        return projectDAO.getBySlug(projectSlug);
    }

    public List<VersionItem> getOtherVersions() {
        HProject project = getProject();
        if (project != null) {
            List<HProjectIteration> versionList =
                    projectIterationDAO.getByProjectSlug(projectSlug,
                            EntityStatus.ACTIVE, EntityStatus.READONLY);

            Collections.sort(versionList,
                    ComparatorUtil.VERSION_CREATION_DATE_COMPARATOR);

            List<VersionItem> versionItems =
                    Lists.transform(versionList, VERSION_ITEM_FN);

            if (StringUtils.isEmpty(copyFromVersionSlug)
                    && !versionItems.isEmpty()) {
                versionItems.get(0).setSelected(true);
            }
            return versionItems;
        }
        return Collections.EMPTY_LIST;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    public class VersionItem implements Serializable {
        private boolean selected;
        private HProjectIteration version;
    }

    @Restrict("#{s:hasPermission(versionHome.instance, 'update')}")
    public void updateCustomisedLocales(String key, boolean checked) {
        getInstance().setOverrideLocales(!checked);
        update();
    }

    @Override
    protected HProjectIteration loadInstance() {
        Session session = (Session) getEntityManager().getDelegate();
        return (HProjectIteration) session.byNaturalId(HProjectIteration.class)
                .using("slug", getSlug())
                .using("project", projectDAO.getBySlug(projectSlug)).load();
    }

    @Restrict("#{s:hasPermission(versionHome.instance, 'update')}")
    public void updateRequireTranslationReview(String key, boolean checked) {
        getInstance().setRequireTranslationReview(checked);
        update();
        if (checked) {
            conversationScopeMessages.setMessage(FacesMessage.SEVERITY_INFO,
                    msgs.get("jsf.iteration.requireReview.enabled"));
        } else {
            conversationScopeMessages
                    .setMessage(FacesMessage.SEVERITY_INFO,
                            msgs.get("jsf.iteration.requireReview.disabled"));
        }
    }

    public List<ValidationAction> getValidationList() {
        List<ValidationAction> sortedList =
                Lists.newArrayList(getValidations().values());
        Collections.sort(sortedList,
                ValidationFactory.ValidationActionComparator);
        return sortedList;
    }

    private Map<ValidationId, ValidationAction> getValidations() {
        if (availableValidations.isEmpty()) {
            Collection<ValidationAction> validationList =
                    validationServiceImpl.getValidationActions(projectSlug,
                            slug);

            for (ValidationAction validationAction : validationList) {
                availableValidations.put(validationAction.getId(),
                        validationAction);
            }
        }

        return availableValidations;
    }

    public void validateSuppliedId() {
        getInstance(); // this will raise an EntityNotFound exception
        // when id is invalid and conversation will not
        // start
    }

    public ProjectType getProjectType() {
        if (getInstance().getProjectType() == null) {
            getInstance().setProjectType(
                    getInstance().getProject().getDefaultProjectType());
        }
        return getInstance().getProjectType();
    }

    public void setProjectType(ProjectType projectType) {
        getInstance().setProjectType(projectType);
    }

    public void validateProjectSlug() {
        if (projectDAO.getBySlug(projectSlug) == null) {
            throw new EntityNotFoundException("no entity with slug "
                    + projectSlug);
        }
    }

    public void verifySlugAvailable(ValueChangeEvent e) {
        String slug = (String) e.getNewValue();
        validateSlug(slug, e.getComponent().getId());
    }

    public boolean validateSlug(String slug, String componentId) {
        if (!isSlugAvailable(slug)) {
            FacesMessages.instance().addToControl(componentId,
                    "This Version ID has been used in this project");
            return false;
        }
        return true;
    }

    public boolean isSlugAvailable(String slug) {
        return slugEntityServiceImpl.isProjectIterationSlugAvailable(slug,
                projectSlug);
    }

    public String createVersion() {
        if (!validateSlug(getInstance().getSlug(), "slug"))
            return "invalid-slug";

        if (copyFromVersion) {
            copyVersion();
            return "copy-version";
        } else {
            return persist();
        }
    }

    public void copyVersion() {
        getInstance().setStatus(EntityStatus.READONLY);

        // create basic version here
        HProject project = getProject();
        project.addIteration(getInstance());
        super.persist();

        copyVersionManager.startCopyVersion(projectSlug,
                copyFromVersionSlug, getInstance().getSlug());

        conversationScopeMessages
                .setMessage(FacesMessage.SEVERITY_INFO, msgs.
                        format("jsf.copyVersion.started",
                                getInstance().getSlug(), copyFromVersionSlug));
    }

    @Override
    public String persist() {
        updateProjectType();

        HProject project = getProject();
        project.addIteration(getInstance());

        List<HLocale> projectLocales =
                localeServiceImpl
                        .getSupportedLanguageByProject(projectSlug);

        getInstance().getCustomizedLocales().addAll(projectLocales);

        getInstance().getCustomizedValidations().putAll(
                project.getCustomizedValidations());
        return super.persist();
    }

    @Override
    public Object getId() {
        return projectSlug + "/" + slug;
    }

    @Override
    public NaturalIdentifier getNaturalId() {
        return Restrictions.naturalId().set("slug", slug)
                .set("project", projectDAO.getBySlug(projectSlug));
    }

    @Override
    public boolean isIdDefined() {
        return slug != null && projectSlug != null;
    }

    public List<HLocale> getInstanceActiveLocales() {
        if (StringUtils.isNotEmpty(projectSlug) && StringUtils.isNotEmpty(slug)) {
            List<HLocale> locales =
                    localeServiceImpl.getSupportedLanguageByProjectIteration(
                            projectSlug, slug);
            Collections.sort(locales, ComparatorUtil.LOCALE_COMPARATOR);
            return locales;
        }
        return localeServiceImpl.getSupportedAndEnabledLocales();
    }

    public boolean isValidationsSameAsProject() {

        Collection<ValidationAction> versionValidations =
                validationServiceImpl.getValidationActions(projectSlug,
                        slug);
        Collection<ValidationAction> projectValidations =
                validationServiceImpl.getValidationActions(projectSlug);
        return versionValidations.equals(projectValidations);
    }

    public void copyValidationFromProject() {
        getInstance().getCustomizedValidations().clear();
        getInstance().getCustomizedValidations().putAll(
                getInstance().getProject().getCustomizedValidations());
        availableValidations.clear();
        update();

        conversationScopeMessages
                .setMessage(
                        FacesMessage.SEVERITY_INFO,
                        msgs.get(
                                "jsf.iteration.CopyProjectValidations.message"));
    }

    @Override
    @Restrict("#{s:hasPermission(versionHome.instance, 'update')}")
    public String update() {
        String state = super.update();
        projectIterationUpdateEvent.fire(
                new ProjectIterationUpdate(getInstance()));
        return state;
    }

    /**
     * This is for autocomplete components of which ConversationScopeMessages
     * will be null
     *
     * @param conversationScopeMessages
     * @return
     */
    private String update(ConversationScopeMessages conversationScopeMessages) {
        if (this.conversationScopeMessages == null) {
            this.conversationScopeMessages = conversationScopeMessages;
        }
        return update();
    }

    @Restrict("#{s:hasPermission(versionHome.instance, 'update')}")
    public void removeLanguage(LocaleId localeId) {
        HLocale locale = localeServiceImpl.getByLocaleId(localeId);
        getInstance().getCustomizedLocales().remove(locale);

        update();
        conversationScopeMessages.setMessage(FacesMessage.SEVERITY_INFO,
                msgs.format("jsf.iteration.LanguageRemoved",
                        locale.retrieveDisplayName()));
    }

    @Restrict("#{s:hasPermission(versionHome.instance, 'update')}")
    public void updateStatus(char initial) {
        getInstance().setStatus(EntityStatus.valueOf(initial));
        update();
        conversationScopeMessages.setMessage(FacesMessage.SEVERITY_INFO,
                msgs.format("jsf.iteration.status.updated",
                        EntityStatus.valueOf(initial)));
    }

    public void updateSelectedProjectType(ValueChangeEvent e) {
        selectedProjectType = (String) e.getNewValue();
        updateProjectType();
    }

    public void copyProjectTypeFromProject() {
        getInstance().setProjectType(
                getInstance().getProject().getDefaultProjectType());
        update();
        conversationScopeMessages.setMessage(FacesMessage.SEVERITY_INFO,
                msgs.get("jsf.iteration.CopyProjectType.message"));
    }

    /**
     * @return comma-separated list of accepted file extensions. May be an empty
     *         string
     */
    public String getAcceptedSourceFileTypes() {
        return Joiner
                .on(", ")
                .join(ProjectType.getSupportedSourceFileTypes(getProjectType()));
    }

    private void updateProjectType() {
        if (!StringUtils.isEmpty(selectedProjectType)
                && !selectedProjectType.equals("null")) {
            ProjectType projectType = ProjectType.valueOf(selectedProjectType);
            getInstance().setProjectType(projectType);
        } else {
            getInstance().setProjectType(null);
        }
    }

    public List<ValidationAction.State> getValidationStates() {
        return Arrays.asList(ValidationAction.State.values());
    }

    @Restrict("#{s:hasPermission(versionHome.instance, 'update')}")
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

    /**
     * If this action is enabled(Warning or Error), then it's exclusive
     * validation will be turn off
     *
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

    private class VersionLocaleAutocomplete extends LocaleAutocomplete {

        @Override
        protected Collection<HLocale> getLocales() {
            if (StringUtils.isNotEmpty(projectSlug)
                    && StringUtils.isNotEmpty(slug)) {
                return localeServiceImpl
                        .getSupportedLanguageByProjectIteration(projectSlug,
                                slug);
            }
            return Collections.EMPTY_LIST;
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

            getInstance().getCustomizedLocales().add(locale);

            update(conversationScopeMessages);
            reset();
            conversationScopeMessages.setMessage(FacesMessage.SEVERITY_INFO,
                    msgs.format("jsf.iteration.LanguageAdded",
                            locale.retrieveDisplayName()));

        }
    }

    public List<ProjectType> getProjectTypeList() {
        List<ProjectType> projectTypes = Arrays.asList(ProjectType.values());
        Collections.sort(projectTypes, ComparatorUtil.PROJECT_TYPE_COMPARATOR);
        return projectTypes;
    }
}
