package org.zanata.webtrans.client.service;

import org.zanata.webtrans.client.presenter.UserConfigHolder;
import org.zanata.webtrans.shared.model.DocumentInfo;
import org.zanata.webtrans.shared.model.TransUnitId;
import org.zanata.webtrans.shared.rpc.EditorFilter;

import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 * @author Patrick Huang <a
 *         href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@Singleton
public class GetTransUnitActionContextHolder {
    private GetTransUnitActionContext context;
    private UserConfigHolder configHolder;

    @Inject
    public GetTransUnitActionContextHolder(UserConfigHolder configHolder) {
        this.configHolder = configHolder;
    }

    protected boolean isContextInitialized() {
        return context != null;
    }

    protected GetTransUnitActionContext initContext(DocumentInfo document,
            TransUnitId targetTransUnitId,
            EditorFilter editorFilter) {
        // @formatter:off
        context = new GetTransUnitActionContext(document)
            .withCount(configHolder.getState().getEditorPageSize())
            .withEditorFilter(editorFilter)
            .withFilterFuzzy(configHolder.getState().isFilterByFuzzy())
            .withFilterTranslated(
                    configHolder.getState().isFilterByTranslated())
            .withFilterUntranslated(
                    configHolder.getState().isFilterByUntranslated())
            .withFilterApproved(configHolder.getState().isFilterByApproved())
            .withFilterRejected(configHolder.getState().isFilterByRejected())
            .withFilterHasError(configHolder.getState().isFilterByHasError())
            .withValidationIds(
                    configHolder.getState().getEnabledValidationIds())
            .withTargetTransUnitId(targetTransUnitId);
        // @formatter:on

        return context;
    }

    public GetTransUnitActionContext getContext() {
        return context;
    }

    public GetTransUnitActionContext changeOffset(int targetOffset) {
        context = context.withOffset(targetOffset);
        return context;
    }

    public GetTransUnitActionContext changeTargetTransUnitId(
            TransUnitId transUnitId) {
        context = context.withTargetTransUnitId(transUnitId);
        return context;
    }

    public GetTransUnitActionContext updateContext(
            GetTransUnitActionContext newContext) {
        context = newContext;
        return context;
    }
}
