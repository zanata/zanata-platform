/**
 *
 */
package org.zanata.service.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang.StringUtils;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.inject.Named;
import org.apache.deltaspike.jpa.api.transaction.Transactional;
import org.zanata.common.LocaleId;
import org.zanata.dao.DocumentDAO;
import org.zanata.dao.ProjectDAO;
import org.zanata.dao.ProjectIterationDAO;
import org.zanata.dao.TextFlowTargetDAO;
import org.zanata.model.HDocument;
import org.zanata.model.HProject;
import org.zanata.model.HProjectIteration;
import org.zanata.model.HTextFlow;
import org.zanata.model.HTextFlowTarget;
import org.zanata.service.TranslationStateCache;
import org.zanata.service.ValidationFactoryProvider;
import org.zanata.service.ValidationService;
import org.zanata.webtrans.shared.model.ValidationAction;
import org.zanata.webtrans.shared.model.ValidationAction.State;
import org.zanata.webtrans.shared.model.ValidationId;
import org.zanata.webtrans.shared.validation.ValidationFactory;
import com.google.common.base.Stopwatch;
import com.google.common.collect.Lists;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
@Named("validationServiceImpl")
@RequestScoped
@Transactional
public class ValidationServiceImpl implements ValidationService {
    private static final org.slf4j.Logger log =
            org.slf4j.LoggerFactory.getLogger(ValidationServiceImpl.class);

    @Inject
    private ProjectDAO projectDAO;
    @Inject
    private ProjectIterationDAO projectIterationDAO;
    @Inject
    private TextFlowTargetDAO textFlowTargetDAO;
    @Inject
    private DocumentDAO documentDAO;
    @Inject
    private TranslationStateCache translationStateCacheImpl;
    private ValidationFactory validationFactory;

    private ValidationFactory getValidationFactory() {
        if (validationFactory == null) {
            validationFactory = ValidationFactoryProvider.getFactoryInstance();
        }
        return validationFactory;
    }

    @Override
    public Collection<ValidationAction>
            getValidationActions(String projectSlug) {
        if (!StringUtils.isEmpty(projectSlug)) {
            HProject project = projectDAO.getBySlug(projectSlug);
            return getValidationActions(project);
        }
        return getValidationFactory().getAllValidationActions().values();
    }

    private Collection<ValidationAction> getValidationActions(HProject project,
            State... includeStates) {
        Map<String, String> customizedValidations =
                project.getCustomizedValidations();
        Collection<ValidationAction> mergedList =
                mergeCustomisedStateToAllValidations(customizedValidations);
        return filterList(mergedList, includeStates);
    }

    @Override
    public Collection<ValidationAction> getValidationActions(String projectSlug,
            String versionSlug) {
        if (!StringUtils.isEmpty(projectSlug)
                && !StringUtils.isEmpty(versionSlug)) {
            HProjectIteration version =
                    projectIterationDAO.getBySlug(projectSlug, versionSlug);
            return getValidationActions(version);
        } else if (!StringUtils.isEmpty(projectSlug)) {
            return getValidationActions(projectSlug);
        }
        return getValidationFactory().getAllValidationActions().values();
    }

    private Collection<ValidationAction> getValidationActions(
            HProjectIteration projectVersion, State... includeStates) {
        Map<String, String> customizedValidations =
                projectVersion.getCustomizedValidations();
        if (customizedValidations.isEmpty()) {
            return getValidationActions(projectVersion.getProject(),
                    includeStates);
        }
        Collection<ValidationAction> mergedList =
                mergeCustomisedStateToAllValidations(customizedValidations);
        return filterList(mergedList, includeStates);
    }

    /**
     * Inherits validations from project if version has no defined validations
     */
    private Collection<ValidationAction> filterList(
            Collection<ValidationAction> list, State... includeStates) {
        if (includeStates == null || includeStates.length == 0) {
            return list;
        }
        List<State> includeStateList = Arrays.asList(includeStates);
        Collection<ValidationAction> filteredList = Lists.newArrayList();
        for (ValidationAction action : list) {
            if (includeStateList.contains(action.getState())) {
                filteredList.add(action);
            }
        }
        return filteredList;
    }

    private Collection<ValidationAction> mergeCustomisedStateToAllValidations(
            Map<String, String> customizedValidations) {
        Collection<ValidationAction> allValidations =
                getValidationFactory().getAllValidationActions().values();
        for (ValidationAction valAction : allValidations) {
            String name = valAction.getId().name();
            if (customizedValidations.containsKey(name)) {
                State persistedState =
                        State.valueOf(customizedValidations.get(name));
                valAction.setState(persistedState);
            }
        }
        return allValidations;
    }

    /**
     * Get validation id of the HProjectIteration with includeStates. Leave
     * includeStates empty to get all states
     *
     * @param version
     * @param includeStates
     * @return list of validation ids for that version
     */
    private List<ValidationId> getValidationIds(HProjectIteration version,
            State... includeStates) {
        List<ValidationId> validationIds = Lists.newArrayList();
        Collection<ValidationAction> mergedList = Lists.newArrayList();
        if (version != null) {
            mergedList = getValidationActions(version, includeStates);
        }
        for (ValidationAction action : mergedList) {
            validationIds.add(action.getId());
        }
        return validationIds;
    }

    @Override
    public boolean runDocValidations(Long hDocId,
            List<ValidationId> validationIds, LocaleId localeId) {
        log.debug("Start runDocValidations {}", hDocId);
        Stopwatch stopwatch = Stopwatch.createStarted();
        HDocument hDoc = documentDAO.findById(hDocId, false);
        boolean hasError =
                documentHasWarningOrError(hDoc, validationIds, localeId);
        log.debug("Finished runDocValidations in " + stopwatch);
        return hasError;
    }

    @Override
    public boolean runDocValidationsWithServerRules(HDocument hDoc,
            LocaleId localeId) {
        log.debug("Start runDocValidationsWithServerRules {}", hDoc.getId());
        Stopwatch stopwatch = Stopwatch.createStarted();
        List<ValidationId> validationIds = getValidationIds(
                hDoc.getProjectIteration(), State.Warning, State.Error);
        boolean hasError =
                documentHasWarningOrError(hDoc, validationIds, localeId);
        log.debug("Finished runDocValidationsWithServerRules in " + stopwatch);
        return hasError;
    }

    private boolean documentHasWarningOrError(HDocument hDoc,
            List<ValidationId> validationIds, LocaleId localeId) {
        for (HTextFlow textFlow : hDoc.getTextFlows()) {
            boolean hasError = textFlowTargetHasWarningOrError(textFlow.getId(),
                    validationIds, localeId);
            if (hasError) {
                // return true if error found, else continue
                return true;
            }
        }
        return false;
    }

    @Override
    public List<HTextFlow> filterHasWarningOrErrorTextFlow(
            List<HTextFlow> textFlows, List<ValidationId> validationIds,
            LocaleId localeId, int startIndex, int maxSize) {
        log.debug("Start filter {} textFlows", textFlows.size());
        Stopwatch stopwatch = Stopwatch.createStarted();
        List<HTextFlow> result = new ArrayList<HTextFlow>();
        for (HTextFlow textFlow : textFlows) {
            boolean hasWarningOrError = textFlowTargetHasWarningOrError(
                    textFlow.getId(), validationIds, localeId);
            if (hasWarningOrError) {
                result.add(textFlow);
            }
        }
        log.debug("Finished filter textFlows in " + stopwatch);
        if (result.size() <= maxSize) {
            return result;
        }
        int toIndex = startIndex + maxSize;
        toIndex = toIndex > result.size() ? result.size() : toIndex;
        startIndex = startIndex > toIndex ? toIndex - maxSize : startIndex;
        startIndex = startIndex < 0 ? 0 : startIndex;
        return result.subList(startIndex, toIndex);
    }

    private boolean textFlowTargetHasWarningOrError(Long textFlowId,
            List<ValidationId> validationIds, LocaleId localeId) {
        HTextFlowTarget target =
                textFlowTargetDAO.getTextFlowTarget(textFlowId, localeId);
        if (target != null) {
            for (ValidationId validationId : validationIds) {
                Boolean value = translationStateCacheImpl
                        .textFlowTargetHasWarningOrError(target.getId(),
                                validationId);
                if (value != null && value.booleanValue()) {
                    return value.booleanValue();
                }
            }
        }
        return false;
    }

    @Override
    public List<String> validateWithServerRules(
            HProjectIteration projectVersion, List<String> sources,
            List<String> translations, State... actionStates) {
        Collection<ValidationAction> validationActions =
                getValidationActions(projectVersion, actionStates);
        List<String> errorList = Lists.newArrayList();
        String tf_content0 = sources.get(0);
        String tft_content0 = translations.get(0);
        for (ValidationAction action : validationActions) {
            errorList.addAll(action.validate(tf_content0, tft_content0));
        }
        return errorList;
    }
}
