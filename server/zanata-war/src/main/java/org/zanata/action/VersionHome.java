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

import static com.google.common.base.Strings.isNullOrEmpty;
import static javax.faces.application.FacesMessage.SEVERITY_INFO;
import java.util.ArrayList;
import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.lang.StringUtils;
import org.apache.deltaspike.jpa.api.transaction.Transactional;
import org.hibernate.Session;
import org.hibernate.criterion.NaturalIdentifier;
import org.hibernate.criterion.Restrictions;
import javax.annotation.Nullable;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Model;
import javax.faces.bean.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import org.zanata.common.DocumentType;
import org.zanata.common.EntityStatus;
import org.zanata.common.LocaleId;
import org.zanata.common.ProjectType;
import org.zanata.dao.ProjectDAO;
import org.zanata.dao.ProjectIterationDAO;
import org.zanata.dao.LocaleDAO;
import org.zanata.exception.ProjectNotFoundException;
import org.zanata.exception.VersionNotFoundException;
import org.zanata.i18n.Messages;
import org.zanata.model.HLocale;
import org.zanata.model.HProject;
import org.zanata.model.HProjectIteration;
import org.zanata.model.validator.SlugValidator;
import org.zanata.seam.scope.ConversationScopeMessages;
import org.zanata.security.ZanataIdentity;
import org.zanata.service.LocaleService;
import org.zanata.service.SlugEntityService;
import org.zanata.service.ValidationService;
import org.zanata.service.impl.LocaleServiceImpl;
import org.zanata.service.impl.WebhookServiceImpl;
import org.zanata.ui.faces.FacesMessages;
import org.zanata.util.ComparatorUtil;
import org.zanata.util.UrlUtil;
import org.zanata.webhook.events.VersionChangedEvent;
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
import java.util.ResourceBundle;

@Named("versionHome")
@ViewScoped
@Model
@Transactional
public class VersionHome extends SlugHome<HProjectIteration>
        implements HasLanguageSettings, Serializable {
    private static final org.slf4j.Logger log =
            org.slf4j.LoggerFactory.getLogger(VersionHome.class);
    private static final long serialVersionUID = 1L;

    /**
     * This field is set from form input which can differ from original slug
     */
    @Nullable
    private String inputSlugValue;
    private Long versionId;
    @Inject
    @Any
    private ProjectAndVersionSlug projectAndVersionSlug;
    @Inject
    private FacesMessages facesMessages;
    @Inject
    private ProjectIterationDAO projectIterationDAO;
    @Inject
    private LocaleDAO localeDAO;
    @Inject
    private ConversationScopeMessages conversationScopeMessages;
    @Inject
    private LocaleService localeServiceImpl;
    @Inject
    private ValidationService validationServiceImpl;
    @Inject
    private SlugEntityService slugEntityServiceImpl;
    @Inject
    private ProjectDAO projectDAO;
    @Inject
    private Messages msgs;
    @Inject
    private CopyVersionManager copyVersionManager;
    @Inject
    private UrlUtil urlUtil;
    @Inject
    private ZanataIdentity identity;
    @Inject
    private WebhookServiceImpl webhookServiceImpl;
    private Map<ValidationId, ValidationAction> availableValidations =
            Maps.newHashMap();
    private boolean isNewInstance = false;
    private String selectedProjectType;
    private boolean copyFromVersion = true;
    private String copyFromVersionSlug;
    private final Function<HProjectIteration, VersionItem> VERSION_ITEM_FN =
            new Function<HProjectIteration, VersionItem>() {

                @Override
                public VersionItem apply(HProjectIteration input) {
                    boolean selected = StringUtils
                            .isNotEmpty(copyFromVersionSlug)
                            && copyFromVersionSlug.equals(input.getSlug());
                    return new VersionItem(selected, input);
                }
            };

    private void setDefaultCopyFromVersion() {
        List<VersionItem> otherVersions = getOtherVersions();
        if (!otherVersions.isEmpty()
                && StringUtils.isEmpty(copyFromVersionSlug)) {
            this.copyFromVersionSlug =
                    otherVersions.get(0).getVersion().getSlug();
            copyFromVersion = true;
        } else {
            copyFromVersion = false;
        }
    }

    public VersionHome() {
        setEntityClass(HProjectIteration.class);
    }

    public void init(boolean isNewInstance) {
        this.isNewInstance = isNewInstance;
        if (isNewInstance) {
            String projectSlug = getProjectSlug();
            clearSlugs();
            setProjectSlug(projectSlug);
            identity.checkPermission(getProject(), "insert");
            ProjectType projectType = getProject().getDefaultProjectType();
            if (projectType != null) {
                selectedProjectType = projectType.name();
            }
            if (StringUtils.isEmpty(copyFromVersionSlug)) {
                setDefaultCopyFromVersion();
            }
        } else {
            copyFromVersion = false;
            ProjectType versionProjectType = getInstance().getProjectType();
            if (versionProjectType != null) {
                selectedProjectType = versionProjectType.name();
            }
            copyFromVersionSlug = "";
            enteredLocaleAliases.putAll(getLocaleAliases());
        }
    }

    public HProject getProject() {
        return projectDAO.getBySlug(getProjectSlug());
    }

    public String getProjectSlug() {
        return projectAndVersionSlug.getProjectSlug();
    }

    public void setProjectSlug(String slug) {
        projectAndVersionSlug.setProjectSlug(slug);
    }

    public String getSlug() {
        return projectAndVersionSlug.getVersionSlug();
    }

    public List<VersionItem> getOtherVersions() {
        HProject project = getProject();
        if (project != null) {
            List<HProjectIteration> versionList =
                    projectIterationDAO.getByProjectSlug(getProjectSlug(),
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
        return Collections.emptyList();
    }

    public class VersionItem implements Serializable {
        private boolean selected;
        private HProjectIteration version;

        public boolean isSelected() {
            return this.selected;
        }

        public HProjectIteration getVersion() {
            return this.version;
        }

        public void setSelected(final boolean selected) {
            this.selected = selected;
        }

        public void setVersion(final HProjectIteration version) {
            this.version = version;
        }

        @java.beans.ConstructorProperties({ "selected", "version" })
        public VersionItem(final boolean selected,
                final HProjectIteration version) {
            this.selected = selected;
            this.version = version;
        }
    }

    @Override
    protected HProjectIteration loadInstance() {
        Session session = (Session) getEntityManager().getDelegate();
        HProject project = (HProject) session.byNaturalId(HProject.class)
                .using("slug", getProjectSlug()).load();
        validateProjectState(project);
        if (versionId == null) {
            HProjectIteration iteration = (HProjectIteration) session
                    .byNaturalId(HProjectIteration.class)
                    .using("slug", getSlug())
                    .using("project", projectDAO.getBySlug(getProjectSlug()))
                    .load();
            validateIterationState(iteration);
            versionId = iteration.getId();
            return iteration;
        } else {
            HProjectIteration iteration = (HProjectIteration) session
                    .load(HProjectIteration.class, versionId);
            validateIterationState(iteration);
            return iteration;
        }
    }

    private void validateIterationState(HProjectIteration iteration) {
        if (iteration == null
                || iteration.getStatus() == EntityStatus.OBSOLETE) {
            log.warn(
                    "Project version [id={}, slug={}], does not exist or is soft deleted: {}",
                    versionId, getSlug(), iteration);
            throw new VersionNotFoundException(getProjectSlug(), getSlug());
        }
    }

    private void validateProjectState(HProject project) {
        if (project == null || project.getStatus() == EntityStatus.OBSOLETE) {
            log.warn("Project [slug={}], does not exist or is soft deleted: {}",
                    getProjectSlug(), project);
            throw new ProjectNotFoundException(getProjectSlug());
        }
    }

    @Transactional
    public void updateRequireTranslationReview(String key, boolean checked) {
        identity.checkPermission(getInstance(), "update");
        getInstance().setRequireTranslationReview(checked);
        update();
        if (checked) {
            conversationScopeMessages.setMessage(FacesMessage.SEVERITY_INFO,
                    msgs.get("jsf.iteration.requireReview.enabled"));
        } else {
            conversationScopeMessages.setMessage(FacesMessage.SEVERITY_INFO,
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
                    validationServiceImpl.getValidationActions(getProjectSlug(),
                            getInstance().getSlug());
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

    @Transactional
    public void setProjectType(ProjectType projectType) {
        getInstance().setProjectType(projectType);
    }

    public void validateProjectSlug() {
        if (projectDAO.getBySlug(getProjectSlug()) == null) {
            throw new EntityNotFoundException(
                    "no entity with slug " + getProjectSlug());
        }
    }

    public void verifySlugAvailable(ValueChangeEvent e) {
        String slug = (String) e.getNewValue();
        validateSlug(slug, e.getComponent().getId());
    }

    public boolean validateSlug(String slug, String componentId) {
        if (!isSlugAvailable(slug)) {
            facesMessages.addToControl(componentId,
                    "This Version ID has been used in this project");
            return false;
        }
        boolean valid = new SlugValidator().isValid(slug, null);
        if (!valid) {
            String validationMessages =
                    ResourceBundle.getBundle("ValidationMessages").getString(
                            "javax.validation.constraints.Slug.message");
            facesMessages.addToControl(componentId, validationMessages);
            return false;
        }
        return true;
    }

    public boolean isSlugAvailable(String slug) {
        return slugEntityServiceImpl.isProjectIterationSlugAvailable(slug,
                getProjectSlug());
    }

    @Transactional
    public String createVersion() {
        if (!validateSlug(inputSlugValue, "slug"))
            return "invalid-slug";
        if (copyFromVersion) {
            copyVersion();
            return "copy-version";
        } else {
            return persist();
        }
    }

    @Transactional
    public void copyVersion() {
        copyVersionManager.startCopyVersion(getProjectSlug(),
                copyFromVersionSlug, inputSlugValue);
        conversationScopeMessages.setMessage(FacesMessage.SEVERITY_INFO,
                msgs.format("jsf.copyVersion.started", inputSlugValue,
                        copyFromVersionSlug));
    }

    public void setSlug(String slug) {
        projectAndVersionSlug.setVersionSlug(slug);
        this.inputSlugValue = slug;
    }

    @Override
    @Transactional
    public String persist() {
        String slug = getInputSlugValue();
        if (!validateSlug(slug, "slug")) {
            return null;
        }
        getInstance().setSlug(slug);
        updateProjectType();
        HProject project = getProject();
        project.addIteration(getInstance());
        // FIXME this looks only to be used when copying a version.
        // so it should copy the setting for isOverrideLocales,
        // and all enabled locales and locale alias data if it is
        // overriding.
        List<HLocale> projectLocales = localeServiceImpl
                .getSupportedLanguageByProject(getProjectSlug());
        getInstance().getCustomizedLocales().addAll(projectLocales);
        getInstance().getCustomizedValidations()
                .putAll(project.getCustomizedValidations());
        String result = super.persist();
        webhookServiceImpl.processWebhookVersionChanged(getProjectSlug(), slug,
                getProject().getWebHooks(),
                VersionChangedEvent.ChangeType.CREATE);
        return result;
    }

    @Override
    public Object getId() {
        return getProjectSlug() + "/" + getSlug();
    }

    @Override
    public NaturalIdentifier getNaturalId() {
        return Restrictions.naturalId().set("slug", getSlug()).set("project",
                projectDAO.getBySlug(getProjectSlug()));
    }

    @Override
    public boolean isIdDefined() {
        return getSlug() != null && getProjectSlug() != null;
    }

    public boolean isValidationsSameAsProject() {
        Collection<ValidationAction> versionValidations = validationServiceImpl
                .getValidationActions(getProjectSlug(), getSlug());
        Collection<ValidationAction> projectValidations =
                validationServiceImpl.getValidationActions(getProjectSlug());
        return versionValidations.equals(projectValidations);
    }

    @Transactional
    public void copyValidationFromProject() {
        getInstance().getCustomizedValidations().clear();
        getInstance().getCustomizedValidations()
                .putAll(getInstance().getProject().getCustomizedValidations());
        availableValidations.clear();
        update();
        conversationScopeMessages.setMessage(FacesMessage.SEVERITY_INFO,
                msgs.get("jsf.iteration.CopyProjectValidations.message"));
    }

    /**
     * Flush changes to the database, and raise an event to communicate that the
     * version has been changed.
     *
     * @return the String "updated"
     */
    @Override
    @Transactional
    public String update() {
        identity.checkPermission(getInstance(), "update");
        if (!getSlug().equals(getInputSlugValue())
                && !validateSlug(getInputSlugValue(), "slug")) {
            return null;
        }
        if (getInputSlugValue() != null
                && !getSlug().equals(getInputSlugValue())) {
            getInstance().setSlug(getInputSlugValue());
        }
        boolean softDeleted = false;
        if (getInstance().getStatus() == EntityStatus.OBSOLETE) {
            // if we offer delete in REST, we need to move this to hibernate
            // listener
            String newSlug = getInstance().changeToDeletedSlug();
            getInstance().setSlug(newSlug);
            softDeleted = true;
        }
        String state = super.update();
        if (softDeleted) {
            String url = urlUtil.projectUrl(getProjectSlug());
            urlUtil.redirectToInternal(url);
            return state;
        }
        facesMessages.addGlobal(SEVERITY_INFO,
                msgs.get("jsf.version.settings.updated"));
        if (!getSlug().equals(getInstance().getSlug())) {
            projectAndVersionSlug.setVersionSlug(getInstance().getSlug());
            return "version-slug-updated";
        }
        return state;
    }

    @Override
    protected void updatedMessage() {
        // Disable the default message from Seam
    }

    @Transactional
    public void updateStatus(char initial) {
        identity.checkPermission(getInstance(), "update");
        String message = msgs.format("jsf.iteration.status.updated",
                EntityStatus.valueOf(initial));
        getInstance().setStatus(EntityStatus.valueOf(initial));
        if (getInstance().getStatus() == EntityStatus.OBSOLETE) {
            message = msgs.get("jsf.iteration.deleted");
        }
        update();
        facesMessages.addGlobal(FacesMessage.SEVERITY_INFO, message);
    }

    @Transactional
    public void deleteSelf() {
        String slug = getInstance().getSlug();
        updateStatus('O');
        webhookServiceImpl.processWebhookVersionChanged(getProjectSlug(), slug,
                getProject().getWebHooks(),
                VersionChangedEvent.ChangeType.DELETE);
    }

    @Transactional
    public void updateSelectedProjectType(ValueChangeEvent e) {
        selectedProjectType = (String) e.getNewValue();
        updateProjectType();
    }

    @Transactional
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
    public String getAcceptedSourceFileExtensions() {
        List<String> supportedTypes = Lists.transform(
                ProjectType.getSupportedSourceFileTypes(getProjectType()),
                new Function<DocumentType, String>() {

                    @Override
                    public String apply(DocumentType docType) {
                        return Joiner.on(",")
                                .join(docType.getSourceExtensions());
                    }
                });
        return Joiner.on(", ").join(supportedTypes);
    }

    public String getAcceptedSourceFile() {
        List<String> supportedTypes = Lists.transform(
                ProjectType.getSupportedSourceFileTypes(getProjectType()),
                new Function<DocumentType, String>() {

                    @Override
                    public String apply(DocumentType docType) {
                        return docType.name() + "[" + Joiner.on(",")
                                .join(docType.getSourceExtensions()) + "]";
                    }
                });
        return Joiner.on(", ").join(supportedTypes);
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

    @Transactional
    public void updateValidationOption(String name, String state) {
        identity.checkPermission(getInstance(), "update");
        ValidationId validationId = ValidationId.valueOf(name);
        for (Map.Entry<ValidationId, ValidationAction> entry : getValidations()
                .entrySet()) {
            if (entry.getKey().name().equals(name)) {
                getValidations().get(validationId)
                        .setState(ValidationAction.State.valueOf(state));
                getInstance().getCustomizedValidations().put(
                        entry.getKey().name(),
                        entry.getValue().getState().name());
                ensureMutualExclusivity(getValidations().get(validationId));
                break;
            }
        }
        update();
        conversationScopeMessages.setMessage(FacesMessage.SEVERITY_INFO,
                msgs.format("jsf.validation.updated",
                        validationId.getDisplayName(), state));
    }

    /**
     * If this action is enabled(Warning or Error), then it's exclusive
     * validation will be turn off
     */
    private void
            ensureMutualExclusivity(ValidationAction selectedValidationAction) {
        if (selectedValidationAction.getState() != ValidationAction.State.Off) {
            for (ValidationAction exclusiveValAction : selectedValidationAction
                    .getExclusiveValidations()) {
                getInstance().getCustomizedValidations().put(
                        exclusiveValAction.getId().name(),
                        ValidationAction.State.Off.name());
                getValidations().get(exclusiveValAction.getId())
                        .setState(ValidationAction.State.Off);
            }
        }
    }

    public List<ProjectType> getProjectTypeList() {
        List<ProjectType> projectTypes = Arrays.asList(ProjectType.values());
        Collections.sort(projectTypes, ComparatorUtil.PROJECT_TYPE_COMPARATOR);
        return projectTypes;
    }

    public boolean isOverrideLocales() {
        return getInstance().isOverrideLocales();
    }

    @Transactional
    public void setOverrideLocales(boolean overrideLocales) {
        identity.checkPermission(getInstance(), "update");
        getInstance().setOverrideLocales(overrideLocales);
    }

    public Map<LocaleId, String> getLocaleAliases() {
        return LocaleServiceImpl.getLocaleAliasesByIteration(getInstance());
    }

    @Transactional
    public void removeAllLocaleAliases() {
        identity.checkPermission(getInstance(), "update");
        List<LocaleId> removed = new ArrayList<>();
        List<LocaleId> aliasedLocales =
                new ArrayList<>(getLocaleAliases().keySet());
        if (!aliasedLocales.isEmpty()) {
            ensureOverridingLocales();
            for (LocaleId aliasedLocale : aliasedLocales) {
                boolean hadAlias = removeAliasSilently(aliasedLocale);
                if (hadAlias) {
                    removed.add(aliasedLocale);
                }
            }
        }
        // else no locales to remove, nothing to do.
        showRemovedAliasesMessage(removed);
    }

    /**
     * Ensure that isOverrideLocales is true, and copy data if necessary.
     */
    private void ensureOverridingLocales() {
        if (!isOverrideLocales()) {
            startOverridingLocales();
        }
    }

    /**
     * Copy locale data from project and set overrideLocales, in preparation for
     * making customizations to the locales.
     */
    private void startOverridingLocales() {
        // Copied before setOverrideLocales(true) so that the currently returned
        // values will be used as the basis for any customization.
        List<HLocale> enabledLocales = getEnabledLocales();
        Map<LocaleId, String> localeAliases = getLocaleAliases();
        setOverrideLocales(true);
        // Replace contents rather than entire collections to avoid confusion
        // with reference to the collections that are bound before this runs.
        getInstance().getCustomizedLocales().clear();
        getInstance().getCustomizedLocales().addAll(enabledLocales);
        getInstance().getLocaleAliases().clear();
        getInstance().getLocaleAliases().putAll(localeAliases);
        enteredLocaleAliases.clear();
        enteredLocaleAliases.putAll(localeAliases);
        refreshDisabledLocales();
    }

    /**
     * Update disabled locales to be consistent with enabled locales.
     */
    private void refreshDisabledLocales() {
        // will be re-generated with correct values next time it is fetched.
        disabledLocales = null;
    }

    @Transactional
    public void removeSelectedLocaleAliases() {
        identity.checkPermission(getInstance(), "update");
        List<LocaleId> removed = new ArrayList<>();
        for (Map.Entry<LocaleId, Boolean> entry : getSelectedEnabledLocales()
                .entrySet()) {
            if (entry.getValue()) {
                boolean hadAlias = removeAliasSilently(entry.getKey());
                if (hadAlias) {
                    removed.add(entry.getKey());
                }
            }
        }
        showRemovedAliasesMessage(removed);
    }

    /**
     * Show an appropriate message for the removal of aliases from locales with
     * the given IDs.
     *
     * @param removed
     *            ids of locales that had aliases removed
     */
    private void showRemovedAliasesMessage(List<LocaleId> removed) {
        if (removed.isEmpty()) {
            facesMessages.addGlobal(FacesMessage.SEVERITY_INFO,
                    msgs.get("jsf.LocaleAlias.NoAliasesToRemove"));
        } else if (removed.size() == 1) {
            facesMessages.addGlobal(FacesMessage.SEVERITY_INFO, msgs
                    .format("jsf.LocaleAlias.AliasRemoved", removed.get(0)));
        } else {
            facesMessages.addGlobal(FacesMessage.SEVERITY_INFO,
                    msgs.format("jsf.LocaleAlias.AliasesRemoved",
                            StringUtils.join(removed, ", ")));
        }
    }

    private boolean removeAliasSilently(LocaleId localeId) {
        return setLocaleAliasSilently(localeId, "");
    }

    public String getLocaleAlias(HLocale locale) {
        return getLocaleAliases().get(locale.getLocaleId());
    }

    public boolean hasLocaleAlias(HLocale locale) {
        return getLocaleAliases().containsKey(locale.getLocaleId());
    }

    /**
     * A separate map is used, rather than binding the alias map from the
     * project directly. This is done so that empty values are not added to the
     * map in every form submission, and so that a value entered in the field
     * for a row is not automatically updated when a different row is submitted.
     */
    private Map<LocaleId, String> enteredLocaleAliases = Maps.newHashMap();

    @Transactional
    public void updateToEnteredLocaleAlias(LocaleId localeId) {
        identity.checkPermission(getInstance(), "update");
        String enteredAlias = enteredLocaleAliases.get(localeId);
        setLocaleAlias(localeId, enteredAlias);
    }

    private void setLocaleAlias(LocaleId localeId, String alias) {
        boolean hadAlias = setLocaleAliasSilently(localeId, alias);
        if (isNullOrEmpty(alias)) {
            if (hadAlias) {
                facesMessages.addGlobal(FacesMessage.SEVERITY_INFO,
                        msgs.format("jsf.LocaleAlias.AliasRemoved", localeId));
            } else {
                facesMessages.addGlobal(FacesMessage.SEVERITY_INFO, msgs
                        .format("jsf.LocaleAlias.NoAliasToRemove", localeId));
            }
        } else {
            facesMessages.addGlobal(FacesMessage.SEVERITY_INFO,
                    msgs.format("jsf.LocaleAlias.AliasSet", localeId, alias));
        }
    }

    /**
     * Set or remove a locale alias without showing any message.
     *
     * @param localeId
     *            for which to set alias
     * @param alias
     *            new alias to use. Use empty string to remove alias.
     * @return true if there was already an alias, otherwise false.
     */
    private boolean setLocaleAliasSilently(LocaleId localeId, String alias) {
        HProjectIteration instance = getInstance();
        Map<LocaleId, String> aliases = instance.getLocaleAliases();
        boolean hadAlias = aliases.containsKey(localeId);
        if (isNullOrEmpty(alias)) {
            if (hadAlias) {
                ensureOverridingLocales();
                aliases.remove(localeId);
            }
        } else {
            final boolean sameAlias =
                    hadAlias && alias.equals(aliases.get(localeId));
            if (!sameAlias) {
                ensureOverridingLocales();
                aliases.put(localeId, alias);
            }
        }
        update();
        return hadAlias;
    }

    @Transactional
    public void useDefaultLocales() {
        identity.checkPermission(getInstance(), "update");
        setOverrideLocales(false);
        refreshDisabledLocales();
    }

    private String enabledLocalesFilter = "";
    private String disabledLocalesFilter;

    public List<HLocale> getEnabledLocales() {
        if (StringUtils.isNotEmpty(getProjectSlug())
                && StringUtils.isNotEmpty(getSlug())) {
            List<HLocale> locales =
                    localeServiceImpl.getSupportedLanguageByProjectIteration(
                            getProjectSlug(), getSlug());
            Collections.sort(locales, ComparatorUtil.LOCALE_COMPARATOR);
            return locales;
        }
        return Lists.newArrayList();
    }

    private Map<LocaleId, Boolean> selectedEnabledLocales = Maps.newHashMap();
    // Not sure if this is necessary, seems to work ok on selected disabled
    // locales without this.

    public Map<LocaleId, Boolean> getSelectedEnabledLocales() {
        if (selectedEnabledLocales == null) {
            selectedEnabledLocales = Maps.newHashMap();
            for (HLocale locale : getEnabledLocales()) {
                selectedEnabledLocales.put(locale.getLocaleId(), Boolean.FALSE);
            }
        }
        return selectedEnabledLocales;
    }

    @Transactional
    public void disableSelectedLocales() {
        identity.checkPermission(getInstance(), "update");
        List<LocaleId> toRemove = Lists.newArrayList();
        for (Map.Entry<LocaleId, Boolean> entry : getSelectedEnabledLocales()
                .entrySet()) {
            if (entry.getValue()) {
                toRemove.add(entry.getKey());
            }
        }
        List<LocaleId> removed = Lists.newArrayList();
        for (LocaleId localeId : toRemove) {
            boolean wasEnabled = disableLocaleSilently(localeId);
            if (wasEnabled) {
                removed.add(localeId);
            }
        }
        update();
        if (removed.isEmpty()) {
            // This should not be possible in the UI, but maybe if multiple
            // users are editing it.
        } else if (removed.size() == 1) {
            facesMessages.addGlobal(FacesMessage.SEVERITY_INFO, msgs.format(
                    "jsf.languageSettings.LanguageDisabled", removed.get(0)));
        } else {
            facesMessages.addGlobal(FacesMessage.SEVERITY_INFO,
                    msgs.format("jsf.languageSettings.LanguagesDisabled",
                            StringUtils.join(removed, ", ")));
        }
    }

    private boolean disableLocaleSilently(LocaleId localeId) {
        HLocale locale = localeServiceImpl.getByLocaleId(localeId);
        return disableLocaleSilently(locale);
    }

    @Transactional
    public void disableLocale(HLocale locale) {
        identity.checkPermission(getInstance(), "update");
        boolean wasEnabled = disableLocaleSilently(locale);
        update();
        if (wasEnabled) {
            facesMessages.addGlobal(FacesMessage.SEVERITY_INFO,
                    msgs.format("jsf.languageSettings.LanguageDisabled",
                            locale.getLocaleId()));
        }
        // TODO consider showing a message like "Locale {0} was already
        // disabled."
    }

    /**
     * Disable a locale without printing any message.
     *
     * @param locale
     *            to disable
     * @return true if the locale was enabled before this call, false otherwise.
     */
    private boolean disableLocaleSilently(HLocale locale) {
        boolean wasEnabled;
        if (getEnabledLocales().contains(locale)) {
            ensureOverridingLocales();
            wasEnabled = getInstance().getCustomizedLocales().remove(locale);
            refreshDisabledLocales();
            // TODO consider using alias from project as default rather than
            // none.
            getLocaleAliases().remove(locale.getLocaleId());
            getSelectedEnabledLocales().remove(locale.getLocaleId());
        } else {
            wasEnabled = false;
        }
        return wasEnabled;
    }

    private List<HLocale> disabledLocales;

    public List<HLocale> getDisabledLocales() {
        if (disabledLocales == null) {
            disabledLocales = findActiveNotEnabledLocales();
        }
        return disabledLocales;
    }

    /**
     * Populate the list of available locales after filtering out the locales
     * already in the project.
     */
    private List<HLocale> findActiveNotEnabledLocales() {
        Collection<HLocale> filtered = Collections2
                .filter(localeDAO.findAllActive(), new Predicate<HLocale>() {

                    @Override
                    public boolean apply(HLocale input) {
                        // only include those not already in the project
                        return !getEnabledLocales().contains(input);
                    }
                });
        return Lists.newArrayList(filtered);
    }

    private Map<LocaleId, Boolean> selectedDisabledLocales = Maps.newHashMap();

    @Transactional
    public void enableSelectedLocales() {
        identity.checkPermission(getInstance(), "update");
        List<LocaleId> enabled = new ArrayList<>();
        for (Map.Entry<LocaleId, Boolean> entry : selectedDisabledLocales
                .entrySet()) {
            if (entry.getValue()) {
                boolean wasDisabled = enableLocaleSilently(entry.getKey());
                if (wasDisabled) {
                    enabled.add(entry.getKey());
                }
            }
        }
        selectedDisabledLocales.clear();
        update();
        if (enabled.isEmpty()) {
            // This should not be possible in the UI, but maybe if multiple
            // users are editing it.
        } else if (enabled.size() == 1) {
            facesMessages.addGlobal(FacesMessage.SEVERITY_INFO, msgs.format(
                    "jsf.languageSettings.LanguageEnabled", enabled.get(0)));
        } else {
            facesMessages.addGlobal(FacesMessage.SEVERITY_INFO,
                    msgs.format("jsf.languageSettings.LanguagesEnabled",
                            StringUtils.join(enabled, ", ")));
        }
    }

    @Transactional
    public void enableLocale(HLocale locale) {
        identity.checkPermission(getInstance(), "update");
        boolean wasDisabled = enableLocaleSilently(locale);
        if (wasDisabled) {
            LocaleId localeId = locale.getLocaleId();
            facesMessages.addGlobal(FacesMessage.SEVERITY_INFO, msgs
                    .format("jsf.languageSettings.LanguageEnabled", localeId));
        }
        // TODO consider printing message like "Locale {0} was already enabled"
    }

    private boolean enableLocaleSilently(LocaleId localeId) {
        HLocale locale = localeServiceImpl.getByLocaleId(localeId);
        return enableLocaleSilently(locale);
    }

    private boolean enableLocaleSilently(HLocale locale) {
        final boolean wasDisabled = getDisabledLocales().contains(locale);
        if (wasDisabled) {
            ensureOverridingLocales();
            getInstance().getCustomizedLocales().add(locale);
            getSelectedEnabledLocales().put(locale.getLocaleId(),
                    Boolean.FALSE);
            refreshDisabledLocales();
        }
        // else locale already enabled, nothing to do.
        return wasDisabled;
    }

    /**
     * This field is set from form input which can differ from original slug
     */
    public void setInputSlugValue(@Nullable final String inputSlugValue) {
        this.inputSlugValue = inputSlugValue;
    }

    /**
     * This field is set from form input which can differ from original slug
     */
    @Nullable
    public String getInputSlugValue() {
        return this.inputSlugValue;
    }

    public boolean isNewInstance() {
        return this.isNewInstance;
    }

    public void setNewInstance(final boolean isNewInstance) {
        this.isNewInstance = isNewInstance;
    }

    public void setSelectedProjectType(final String selectedProjectType) {
        this.selectedProjectType = selectedProjectType;
    }

    public String getSelectedProjectType() {
        return this.selectedProjectType;
    }

    public boolean isCopyFromVersion() {
        return this.copyFromVersion;
    }

    public void setCopyFromVersion(final boolean copyFromVersion) {
        this.copyFromVersion = copyFromVersion;
    }

    public String getCopyFromVersionSlug() {
        return this.copyFromVersionSlug;
    }

    public void setCopyFromVersionSlug(final String copyFromVersionSlug) {
        this.copyFromVersionSlug = copyFromVersionSlug;
    }

    /**
     * A separate map is used, rather than binding the alias map from the
     * project directly. This is done so that empty values are not added to the
     * map in every form submission, and so that a value entered in the field
     * for a row is not automatically updated when a different row is submitted.
     */
    public Map<LocaleId, String> getEnteredLocaleAliases() {
        return this.enteredLocaleAliases;
    }

    /**
     * A separate map is used, rather than binding the alias map from the
     * project directly. This is done so that empty values are not added to the
     * map in every form submission, and so that a value entered in the field
     * for a row is not automatically updated when a different row is submitted.
     */
    public void setEnteredLocaleAliases(
            final Map<LocaleId, String> enteredLocaleAliases) {
        this.enteredLocaleAliases = enteredLocaleAliases;
    }

    public String getEnabledLocalesFilter() {
        return this.enabledLocalesFilter;
    }

    public void setEnabledLocalesFilter(final String enabledLocalesFilter) {
        this.enabledLocalesFilter = enabledLocalesFilter;
    }

    public String getDisabledLocalesFilter() {
        return this.disabledLocalesFilter;
    }

    public void setDisabledLocalesFilter(final String disabledLocalesFilter) {
        this.disabledLocalesFilter = disabledLocalesFilter;
    }

    public void setSelectedEnabledLocales(
            final Map<LocaleId, Boolean> selectedEnabledLocales) {
        this.selectedEnabledLocales = selectedEnabledLocales;
    }

    public Map<LocaleId, Boolean> getSelectedDisabledLocales() {
        return this.selectedDisabledLocales;
    }

    public void setSelectedDisabledLocales(
            final Map<LocaleId, Boolean> selectedDisabledLocales) {
        this.selectedDisabledLocales = selectedDisabledLocales;
    }
}
