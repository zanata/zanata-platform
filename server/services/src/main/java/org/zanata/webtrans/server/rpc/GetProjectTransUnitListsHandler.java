/*
 * Copyright 2012, Red Hat, Inc. and individual contributors as indicated by the
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.inject.Named;
import org.zanata.exception.ZanataServiceException;
import org.zanata.model.HLocale;
import org.zanata.model.HTextFlow;
import org.zanata.webtrans.shared.search.FilterConstraints;
import org.zanata.security.ZanataIdentity;
import org.zanata.service.LocaleService;
import org.zanata.service.TextFlowSearchService;
import org.zanata.webtrans.server.ActionHandlerFor;
import org.zanata.webtrans.shared.model.TransUnit;
import org.zanata.webtrans.shared.rpc.GetProjectTransUnitLists;
import org.zanata.webtrans.shared.rpc.GetProjectTransUnitListsResult;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.base.Strings;
import com.google.common.collect.Iterables;
import com.ibm.icu.lang.UCharacter;
import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.ActionException;

/**
 * @see GetProjectTransUnitLists
 * @author David Mason, damason@redhat.com
 */
@Named("webtrans.gwt.GetProjectTransUnitListsHandler")
@RequestScoped
@ActionHandlerFor(GetProjectTransUnitLists.class)
public class GetProjectTransUnitListsHandler extends
        AbstractActionHandler<GetProjectTransUnitLists, GetProjectTransUnitListsResult> {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory
            .getLogger(GetProjectTransUnitListsHandler.class);

    @Inject
    private LocaleService localeServiceImpl;
    @Inject
    private TextFlowSearchService textFlowSearchServiceImpl;
    @Inject
    private TransUnitTransformer transUnitTransformer;
    @Inject
    private ZanataIdentity identity;

    @Override
    public GetProjectTransUnitListsResult
            execute(GetProjectTransUnitLists action, ExecutionContext context)
                    throws ActionException {
        identity.checkLoggedIn();
        log.info("Searching all targets for workspace {}",
                action.getWorkspaceId());
        HLocale hLocale;
        try {
            hLocale = localeServiceImpl.validateLocaleByProjectIteration(
                    action.getWorkspaceId().getLocaleId(),
                    action.getWorkspaceId().getProjectIterationId()
                            .getProjectSlug(),
                    action.getWorkspaceId().getProjectIterationId()
                            .getIterationSlug());
        } catch (ZanataServiceException e) {
            throw new ActionException(e);
        }
        HashMap<Long, List<TransUnit>> matchingTUs =
                new HashMap<Long, List<TransUnit>>();
        HashMap<Long, String> docPaths = new HashMap<Long, String>();
        if (Strings.isNullOrEmpty(action.getSearchString())) {
            // TODO empty searches shouldn't be requested, consider replacing
            // this
            // with an error, or making behaviour return all targets for the
            // project (consider performance).
            return new GetProjectTransUnitListsResult(action, docPaths,
                    matchingTUs);
        }
        FilterConstraints filterConstraints =
                FilterConstraints.builder().filterBy(action.getSearchString())
                        .caseSensitive(action.isCaseSensitive())
                        .checkInSource(action.isSearchInSource())
                        .checkInTarget(action.isSearchInTarget()).build();
        List<HTextFlow> matchingFlows =
                textFlowSearchServiceImpl.findTextFlows(action.getWorkspaceId(),
                        action.getDocumentPaths(), filterConstraints);
        log.info("Returned {} results for search", matchingFlows.size());
        // FIXME remove when analyzer handles leading & trailing whitespace
        boolean needsWhitespaceCheck = !action.getSearchString()
                .equals(action.getSearchString().trim());
        Iterable<HTextFlow> result = matchingFlows;
        if (needsWhitespaceCheck) {
            // FIXME temporary check for leading and trailing whitespace to
            // compensate
            // for NGramAnalyzer trimming strings before tokenization. This
            // should
            // be removed when updating to a lucene version with the whitespace
            // issue resolved.
            result = Iterables.filter(matchingFlows,
                    new WhitespaceMatchPredicate(action, hLocale.getId()));
        }
        for (HTextFlow textFlow : result) {
            List<TransUnit> listForDoc =
                    matchingTUs.get(textFlow.getDocument().getId());
            if (listForDoc == null) {
                listForDoc = new ArrayList<TransUnit>();
            }
            TransUnit transUnit =
                    transUnitTransformer.transform(textFlow, hLocale);
            listForDoc.add(transUnit);
            matchingTUs.put(textFlow.getDocument().getId(), listForDoc);
            docPaths.put(textFlow.getDocument().getId(),
                    textFlow.getDocument().getDocId());
        }
        return new GetProjectTransUnitListsResult(action, docPaths,
                matchingTUs);
    }

    private static String foldCase(String original) {
        char[] buffer = original.toCharArray();
        for (int i = 0; i < buffer.length; i++) {
            buffer[i] = (char) UCharacter.foldCase(buffer[i], true);
        }
        return new String(buffer);
    }

    @Override
    public void rollback(GetProjectTransUnitLists action,
            GetProjectTransUnitListsResult result, ExecutionContext context)
            throws ActionException {
    }

    private static class WhitespaceMatchPredicate
            implements Predicate<HTextFlow> {
        private final GetProjectTransUnitLists action;
        private final Long localeId;
        private ContainSearchTermPredicate containSearchTermPredicate;

        private WhitespaceMatchPredicate(GetProjectTransUnitLists action,
                Long localeId) {
            this.action = action;
            this.localeId = localeId;
            containSearchTermPredicate = new ContainSearchTermPredicate(action);
        }

        @Override
        public boolean apply(HTextFlow textFlow) {
            if (action.isSearchInSource()) {
                Optional<String> optional = Iterables.tryFind(
                        textFlow.getContents(), containSearchTermPredicate);
                if (optional.isPresent()) {
                    return true;
                }
            }
            if (action.isSearchInTarget()) {
                // FIXME hibernate n + 1 select happening here
                List<String> targetContents =
                        textFlow.getTargets().get(localeId).getContents();
                Optional<String> optional = Iterables.tryFind(targetContents,
                        containSearchTermPredicate);
                return optional.isPresent();
            }
            return false;
        }
    }

    private static class ContainSearchTermPredicate
            implements Predicate<String> {
        private final String searchTerm;
        private final boolean caseSensitive;

        private ContainSearchTermPredicate(GetProjectTransUnitLists action) {
            caseSensitive = action.isCaseSensitive();
            searchTerm = !caseSensitive ? foldCase(action.getSearchString())
                    : action.getSearchString();
        }

        @Override
        public boolean apply(String input) {
            String contentStr = !caseSensitive ? foldCase(input) : input;
            return contentStr.contains(searchTerm);
        }
    }
}
