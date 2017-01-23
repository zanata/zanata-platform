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
package org.zanata.webtrans.server.rpc;

import java.util.List;
import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.ActionException;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.inject.Named;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.zanata.dao.TextFlowDAO;
import org.zanata.exception.ZanataServiceException;
import org.zanata.model.HLocale;
import org.zanata.model.HTextFlow;
import org.zanata.webtrans.shared.search.FilterConstraints;
import org.zanata.security.ZanataIdentity;
import org.zanata.service.LocaleService;
import org.zanata.service.ValidationService;
import org.zanata.webtrans.server.ActionHandlerFor;
import org.zanata.webtrans.shared.model.TransUnit;
import org.zanata.webtrans.shared.rpc.EditorFilter;
import org.zanata.webtrans.shared.rpc.GetTransUnitList;
import org.zanata.webtrans.shared.rpc.GetTransUnitListResult;
import org.zanata.webtrans.shared.rpc.GetTransUnitsNavigation;
import org.zanata.webtrans.shared.rpc.GetTransUnitsNavigationResult;
import org.zanata.webtrans.shared.util.FindByTransUnitIdPredicate;
import com.google.common.base.Function;
import com.google.common.base.Strings;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

@Named("webtrans.gwt.GetTransUnitListHandler")
@RequestScoped
@ActionHandlerFor(GetTransUnitList.class)
public class GetTransUnitListHandler extends
        AbstractActionHandler<GetTransUnitList, GetTransUnitListResult> {
    private static final org.slf4j.Logger log =
            org.slf4j.LoggerFactory.getLogger(GetTransUnitListHandler.class);

    @Inject
    private TransUnitTransformer transUnitTransformer;
    @Inject
    private TextFlowDAO textFlowDAO;
    @Inject
    private LocaleService localeServiceImpl;
    @Inject
    private ZanataIdentity identity;
    @Inject
    private ValidationService validationServiceImpl;
    @Inject
    private GetTransUnitsNavigationService getTransUnitsNavigationService;
    private DateTimeFormatter dateFormatter =
            DateTimeFormat.forPattern("yyyy-MM-dd");

    @Override
    public GetTransUnitListResult execute(GetTransUnitList action,
            ExecutionContext context) throws ActionException {
        identity.checkLoggedIn();
        HLocale hLocale = validateAndGetLocale(action);
        log.info("action: {}", action);
        int targetOffset = action.getOffset();
        int targetPageIndex = targetOffset / action.getCount();
        GetTransUnitsNavigationResult navigationResult = null;
        EditorFilter editorFilter = action.getEditorFilter();
        FilterConstraints constraints = FilterConstraints.builder()
                .filterBy(editorFilter.getTextInContent())
                .lastModifiedBy(editorFilter.getLastModifiedByUser())
                .targetChangedBefore(parseDateIfPresent(
                        editorFilter.getLastModifiedBefore()))
                .targetChangedAfter(
                        parseDateIfPresent(editorFilter.getLastModifiedAfter()))
                .resourceIdIs(editorFilter.getResId())
                .msgContext(editorFilter.getMsgContext())
                .sourceCommentContains(editorFilter.getSourceComment())
                .targetCommentContains(editorFilter.getTransComment())
                .caseSensitive(false).checkInSource(true).checkInTarget(true)
                .includeStates(action.getFilterStates()).build();
        if (action.isNeedReloadIndex()) {
            GetTransUnitsNavigation getTransUnitsNavigation =
                    new GetTransUnitsNavigation(action.getDocumentId(),
                            action.getFilterStates(), action.getEditorFilter(),
                            constraints);
            log.debug("get trans unit navigation action: {}",
                    getTransUnitsNavigation);
            navigationResult = getTransUnitsNavigationService
                    .getNavigationIndexes(getTransUnitsNavigation, hLocale);
            int totalPageIndex =
                    getTotalPageIndex(navigationResult.getIdIndexList().size(),
                            action.getCount());
            if (targetPageIndex > totalPageIndex) {
                targetPageIndex = totalPageIndex;
                targetOffset = action.getCount() * targetPageIndex;
            }
            if (action.getTargetTransUnitId() != null) {
                int targetIndexInDoc = navigationResult.getIdIndexList()
                        .indexOf(action.getTargetTransUnitId());
                targetPageIndex = targetIndexInDoc / action.getCount();
                targetOffset = action.getCount() * targetPageIndex;
            }
        }
        List<HTextFlow> textFlows =
                getTextFlows(action, hLocale, targetOffset, constraints);
        GetTransUnitListResult result = transformToTransUnits(action, hLocale,
                textFlows, targetOffset, targetPageIndex);
        result.setNavigationIndex(navigationResult);
        return result;
    }

    private DateTime parseDateIfPresent(String dateInString) {
        return Strings.isNullOrEmpty(dateInString) ? null
                : dateFormatter.parseDateTime(dateInString);
    }

    private int getTotalPageIndex(int indexListSize, int countPerPage) {
        int totalPageNumber =
                (int) Math.ceil((float) indexListSize / countPerPage);
        return totalPageNumber > 0 ? totalPageNumber - 1 : totalPageNumber;
    }

    private List<HTextFlow> getTextFlows(GetTransUnitList action,
            HLocale hLocale, int offset, FilterConstraints constraints) {
        List<HTextFlow> textFlows;
        if (!hasStatusAndSearchFilter(action)) {
            log.debug("Fetch TransUnits:*");
            if (!hasValidationFilter(action)) {
                // TODO debt: this should use a left join to fetch target (and
                // possibly comments)
                textFlows = textFlowDAO.getTextFlowsByDocumentId(
                        action.getDocumentId().getId(), offset,
                        action.getCount());
            } else {
                // TODO debt: this is not scalable. But we may not have other
                // choice
                // for validation filter. Maybe use scrollable result will help?
                textFlows = textFlowDAO.getTextFlowsByDocumentId(
                        action.getDocumentId().getId(), null, null);
                textFlows =
                        validationServiceImpl.filterHasWarningOrErrorTextFlow(
                                textFlows, action.getValidationIds(),
                                hLocale.getLocaleId(), offset,
                                action.getCount());
            }
        } else {
            // has status and other search field filter
            log.debug("Fetch TransUnits filtered by status and/or search: {}",
                    constraints);
            if (!hasValidationFilter(action)) {
                textFlows = textFlowDAO.getTextFlowByDocumentIdWithConstraints(
                        action.getDocumentId(), hLocale, constraints, offset,
                        action.getCount());
            } else {
                // has validation filter
                textFlows =
                        textFlowDAO.getAllTextFlowByDocumentIdWithConstraints(
                                action.getDocumentId(), hLocale, constraints);
                textFlows =
                        validationServiceImpl.filterHasWarningOrErrorTextFlow(
                                textFlows, action.getValidationIds(),
                                hLocale.getLocaleId(), offset,
                                action.getCount());
            }
        }
        return textFlows;
    }

    private boolean hasStatusAndSearchFilter(GetTransUnitList action) {
        return !action.isAcceptAllStatus()
                || !action.getEditorFilter().isAcceptAll();
    }

    private boolean hasValidationFilter(GetTransUnitList action) {
        return action.isFilterHasError() && action.getValidationIds() != null
                && !action.getValidationIds().isEmpty();
    }

    private HLocale validateAndGetLocale(GetTransUnitList action)
            throws ActionException {
        try {
            return localeServiceImpl.validateLocaleByProjectIteration(
                    action.getWorkspaceId().getLocaleId(),
                    action.getWorkspaceId().getProjectIterationId()
                            .getProjectSlug(),
                    action.getWorkspaceId().getProjectIterationId()
                            .getIterationSlug());
        } catch (ZanataServiceException e) {
            throw new ActionException(e);
        }
    }

    private GetTransUnitListResult transformToTransUnits(
            GetTransUnitList action, HLocale hLocale, List<HTextFlow> textFlows,
            int targetOffset, int targetPage) {
        List<TransUnit> units =
                Lists.transform(textFlows, new HTextFlowToTransUnitFunction(
                        hLocale, transUnitTransformer));
        int gotoRow = 0;
        if (action.getTargetTransUnitId() != null) {
            int row = Iterables.indexOf(units, new FindByTransUnitIdPredicate(
                    action.getTargetTransUnitId()));
            if (row != -1) {
                gotoRow = row;
            }
        }
        // stupid GWT RPC can't handle
        // com.google.common.collect.Lists$TransformingRandomAccessList
        return new GetTransUnitListResult(action.getDocumentId(),
                Lists.newArrayList(units), gotoRow, targetOffset, targetPage);
    }

    @Override
    public void rollback(GetTransUnitList action, GetTransUnitListResult result,
            ExecutionContext context) throws ActionException {
    }

    private static class HTextFlowToTransUnitFunction
            implements Function<HTextFlow, TransUnit> {
        private final HLocale hLocale;
        private final TransUnitTransformer transformer;

        public HTextFlowToTransUnitFunction(HLocale hLocale,
                TransUnitTransformer transformer) {
            this.hLocale = hLocale;
            this.transformer = transformer;
        }

        @Override
        public TransUnit apply(HTextFlow input) {
            return transformer.transform(input, hLocale);
        }
    }
}
